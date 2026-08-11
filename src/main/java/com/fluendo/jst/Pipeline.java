/* Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.fluendo.jst;

import java.util.*;
import com.fluendo.utils.*;

public class Pipeline extends com.fluendo.jst.Element implements BusSyncHandler
{
    protected final List<Element> elements = new ArrayList<>();

    protected Clock defClock;
    protected Clock fixedClock = null;
    protected Element clockProvider;
    protected final List<Message> messages = new ArrayList<>();

    protected Bus internalBus;
    private BusThread busThread;

    private StateThread stateThread;
    private boolean stateDirty = false;
    private boolean polling = false;

    protected long streamTime;

    private class BusThread extends Thread
    {
        private final Bus bus;
        private boolean stopping;

        public BusThread (Bus bus)
        {
            super("cortado-BusThread-"+Debug.genId());
            this.bus = bus;
            stopping = false;
        }
        public void run() {
            while (!stopping) {
                bus.waitAndDispatch ();
            }
        }
        public void shutDown() {
            stopping = true;
            bus.setFlushing(true);
        }
    }

    private class StateThread extends Thread
    {
        private boolean stopping;
        private boolean stateDirty;

        public StateThread ()
        {
            super("cortado-StateThread-"+Debug.genId());
            stopping = false;
            stateDirty = false;
        }
        public void run() {
            while (!stopping) {
                synchronized (this) {
                    while (!stateDirty && !stopping) {
                        try {
                            wait();
                        } catch (InterruptedException e) {}
                    }
                    stateDirty = false;
                }
                if (!stopping) {
                    synchronized (stateLock) {
                        reCalcState(false);
                    }
                }
            }
        }
        public synchronized void stateDirty() {
            stateDirty = true;
            notifyAll ();
        }
        public synchronized void shutDown() {
            stopping = true;
            notifyAll ();
        }
    }

    public Pipeline() {
        this (null);
    }

    public String getFactoryName () {
        return "pipeline";
    }

    public Pipeline(String name) {
        super (name);

        defClock = new SystemClock(); 
        clockProvider = null;

        internalBus = new Bus();
        internalBus.setSyncHandler (this);
        bus = new Bus();
        busThread = new BusThread(bus);
        busThread.start();
        stateThread = new StateThread();
        stateThread.start();
    }

    public synchronized void shutDown() {
        if (stateThread != null) {
            stateThread.shutDown();
            stateThread = null;
        }
        if (busThread != null) {
            busThread.shutDown();
            busThread = null;
        }
    }

    public void useClock(Clock clock) {
        fixedClock = clock;
    }

    public synchronized boolean add(Element elem) {
        if (elem == null)
            return false;

        if (elem instanceof ClockProvider) {
            defClock = ((ClockProvider)elem).provideClock();
            clockProvider = elem;
        }

        elements.add (elem);
        elem.baseTime = baseTime;
        elem.setBus (internalBus);
        return true;
    }
    public synchronized boolean remove(Element elem) {
        boolean res;

        if (elem == null)
            return false;

        if ((res = elements.remove (elem))) {
            if (elem == clockProvider) {
                defClock = new SystemClock(); 
                clockProvider = null;
            }
            elem.setBus (null);
            elem.setClock (null);
            synchronized (this) {
                stateDirty = true;
            }
        }
        return res;
    }

    public synchronized Enumeration<Element> enumElements()
    {
        return Collections.enumeration(new ArrayList<>(elements));
    }

    private class SortedEnumerator implements Enumeration<Element>
    {
        private final List<Element> queue;
        private final Map<Element, Integer> hash;
        private Element next;
        private int mode;

        private void addToQueue (Element elem)
        {
            queue.add (elem);
            hash.put (elem, Integer.valueOf(-1));
        }

        private void updateDegree (Element elem)
        {
            for (Enumeration<Pad> p = elem.enumPads(); p.hasMoreElements();) {
                Pad pad = p.nextElement();

                if (pad.direction == Pad.SINK) {
                    Pad peer;
                    Element peerParent;
                    int oldDeg, newDeg;

                    peer = pad.peer;
                    if (peer == null)
                        continue;

                    peerParent = (Element) peer.parent;
                    if (peerParent == null)
                        continue;

                    Integer degVal = hash.get(peerParent);
                    if (degVal == null)
                        continue;
                    oldDeg = degVal;
                    newDeg = oldDeg + mode;

                    if (newDeg == 0) {
                        addToQueue (peerParent);
                    }
                    else {
                        hash.put (peerParent, Integer.valueOf (newDeg));
                    }
                }
            }
        }

        public SortedEnumerator() {
            queue = new ArrayList<>();
            hash = new HashMap<>();

            /* reset all degrees, add sinks to queue */
            for (Enumeration<Element> e = enumElements(); e.hasMoreElements();) {
                Element elem = e.nextElement();

                if (elem.isFlagSet (Element.FLAG_IS_SINK)) {
                    addToQueue (elem);
                }
                else {
                    hash.put (elem, Integer.valueOf(0));
                }
            }
            mode = 1;
            /* update all degrees */
            for (Enumeration<Element> e = enumElements(); e.hasMoreElements();) {
                updateDegree (e.nextElement());
            }
            mode = -1;
            queueNextElement();
        }
        private void queueNextElement () 
        {
            if (queue.isEmpty()) {
                int bestDeg = Integer.MAX_VALUE;
                Element bestElem = null;

                for (Enumeration<Element> e = enumElements(); e.hasMoreElements();) {
                    Element elem = e.nextElement();
                    int deg;

                    Integer degVal = hash.get (elem);
                    if (degVal == null)
                        continue;
                    deg = degVal;
                    if (deg < 0)
                        continue;

                    if (bestElem == null || bestDeg > deg) {
                        bestElem = elem;
                        bestDeg = deg;
                    }
                }
                if (bestElem != null) {
                    if (bestDeg != 0) {
                        System.out.println (this+" loop detected in pipeline!!");
                    }
                    next = bestElem;
                    hash.put (next, Integer.valueOf(-1));
                }
                else {
                    next = null;
                }
            }
            else {
                next = queue.remove (0);
            }
            if (next != null)
                updateDegree (next);
        }

        public boolean hasMoreElements()
        {
            return next != null;
        }
        public Element nextElement() throws NoSuchElementException
        {
            Element result = next;

            if (result == null)
                throw new NoSuchElementException();
            
            queueNextElement ();

            return result;
        }
    }

    public Enumeration<Element> enumSorted()
    {
        return new SortedEnumerator();
    }
    
    private class SinkEnumerator implements Enumeration<Element>
    {
        private final Enumeration<Element> e;
        private Element next;

        public SinkEnumerator() {
            e = enumElements();
            queueNextElement();
        }
        private void queueNextElement()
        {
            next = null;
            while (e.hasMoreElements()) {
                Element elem = e.nextElement();

                if (elem.isFlagSet (Element.FLAG_IS_SINK)) {
                    next = elem;
                    break;
                }
            }
        }

        public boolean hasMoreElements() 
        {
            return next != null;
        }
        public Element nextElement() throws NoSuchElementException
        {
            Element result = next;

            if (result == null)
                throw new NoSuchElementException();
            
            queueNextElement ();

            return result;
        }
    }

    public Enumeration<Element> enumSinks()
    {
        return new SinkEnumerator();
    }

    private synchronized void replaceMessage (Message message, int type)
    {
        int len = messages.size();
        Message msg;
        com.fluendo.jst.Object src = message.getSrc();

        for (int i=0; i<len; i++) {
            msg = messages.get(i);

            if (msg.getType() == type && msg.getSrc() == src) {
                messages.set(i, message);
                return;
            }
        }
        messages.add(message);
    }

    private synchronized boolean findMessage (com.fluendo.jst.Object obj, int type)
    {
        int len = messages.size();
        Message msg;

        for (int i=0; i<len; i++) {
            msg = messages.get(i);

            if (msg.getType() == type && msg.getSrc() == obj)
                return true;
        }
        return false;
    }

    protected boolean isEOS ()
    {
        com.fluendo.jst.Object obj;

        for (Enumeration<Element> e = enumSinks(); e.hasMoreElements();) {
            obj = e.nextElement();

            if (!findMessage (obj, Message.EOS))
                return false;
        }
        return true;
    }

    public int handleSyncMessage (Message message) 
    {
        switch (message.getType()) {
            case Message.EOS:
            {
                boolean isEOS;

                synchronized (this) {
                    Debug.log(Debug.INFO, this+" got EOS from sink: "+message.getSrc());
                    replaceMessage (message, Message.EOS);
                    isEOS = isEOS();
                }
                if (isEOS) {
                    Debug.log(Debug.INFO, "all sinks posted EOS "+this);
                    postMessage (Message.newEOS (this));
                }
                break;
            }
            case Message.STATE_DIRTY:
                scheduleReCalcState ();
                break;
            default:
                /* post to app */
                postMessage (message);
                break;
        }
        return BusSyncHandler.DROP;
    }

    public int getState(int[] resState, int[] resPending, long timeout) {
        reCalcState (false);
        return super.getState (resState, resPending, timeout);
    }

    protected void scheduleReCalcState() {
        synchronized (this) {
            stateDirty = true;
            stateThread.stateDirty();
        }
    }

    private void reCalcState(boolean force) 
    {
        boolean haveAsync, haveNoPreroll;
        int res = SUCCESS;

        Debug.debug("Pipeline.reCalcState");

        synchronized (this) {
            if (force)
                stateDirty = true;
            
            if (!stateDirty)
                return;

            if (polling)
                return;
 
            polling = true;
            stateDirty = false;
            haveAsync = false;
            haveNoPreroll = false;
        }
        for (Enumeration<Element> e = enumElements(); e.hasMoreElements();) {
            Element elem = e.nextElement();

            res = elem.getState(null, null, 1);
            switch (res) {
                case ASYNC:
                    haveAsync = true;
                    break;
                case NO_PREROLL:
                    haveNoPreroll = true;
                    break;
            }
            if (res == FAILURE)
                break;
        }

        if (res != FAILURE) {
            if (haveNoPreroll)
                res = NO_PREROLL;
            if (haveAsync)
                res = ASYNC;
        }

        synchronized (this) {
            polling = false;
        }

        switch (res) {
            case SUCCESS:
            case NO_PREROLL:
                res = continueState(res);
                break;
            case ASYNC:
                lostState();
                break;
            case FAILURE:
                abortState();
                break;
            default:
                break;
        }
    }

    protected int doChildStateChange(int transition)
    {
        int next;
        int result;
        boolean haveAsync, haveNoPreroll;

        next = getTransitionNext (transition);

        haveAsync = false;
        haveNoPreroll = false;

        for (Enumeration<Element> e = enumSorted(); e.hasMoreElements();) {
            Element elem = e.nextElement();

            elem.setBus (internalBus);
            elem.setClock (defClock);
            elem.baseTime = baseTime;

            Debug.log(Debug.DEBUG, this+" setting state " + getStateName(next) + " on "+elem);
            result = elem.setState (next);
            Debug.log(Debug.DEBUG, this+" "+elem+" changed state, result = " + getStateReturnName(result));

            switch (result) {
                case ASYNC:
                    haveAsync = true;
                    break;
                case NO_PREROLL:
                    haveNoPreroll = true;
                    break;
                case FAILURE:
                    return result;
            }
        }

        result = super.changeState(transition);
        if (result == FAILURE)
            return result;

        if (haveNoPreroll)
            result = NO_PREROLL;
        else if (haveAsync)
            result = ASYNC;

        return result;
    }

    protected synchronized int changeState(int transition)
    {
        int result;

        switch (transition) {
            case STOP_PAUSE:
                messages.clear();
                break;
            case PAUSE_PLAY:
                long now = defClock.getTime();
                baseTime = now - streamTime;
                break;
            default:
                break;
        }

        result = doChildStateChange(transition);

        switch (transition) {
            case STOP_PAUSE:
                streamTime = 0;
                break;
            case PLAY_PAUSE:
                long now = defClock.getTime();
                streamTime = now - baseTime;
                messages.clear();
                break;
            case PAUSE_STOP:
                messages.clear();
                break;
            default:
                break;
        }

        return result;
    }

    protected boolean doSendEvent(Event event)
    {
        boolean res = true;

        for (Enumeration<Element> e = enumSinks(); e.hasMoreElements();) {
            Element elem = e.nextElement();

            res &= elem.sendEvent (event);
        }
        return res;
    }

    private boolean doSeek(Event event)
    {
        boolean ret;
        int[] state = new int[1];
        boolean wasPlaying;

        getState(state, null, 0);
        wasPlaying = (state[0] == Element.PLAY);

        if (wasPlaying)
            setState (Element.PAUSE);

        ret = doSendEvent (event);
        if (ret)
            streamTime = 0;

        if (wasPlaying)
            setState (Element.PLAY);

        return ret;
    }

    public boolean sendEvent(Event event)
    {
        switch (event.getType()) {
            case Event.SEEK:
                return doSeek (event);
            default:
                return doSendEvent (event);
        }
    }

    public boolean query(Query query)
    {
        boolean res = true;

        for (Enumeration<Element> e = enumSinks(); e.hasMoreElements();) {
            Element elem = e.nextElement();

            if ((res = elem.query (query)))
                break;
        }
        return res;
    }
}
