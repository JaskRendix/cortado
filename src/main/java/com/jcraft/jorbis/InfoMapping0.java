package com.jcraft.jorbis;

class InfoMapping0 {
  int submaps; // <= 16
  int[] chmuxlist = new int[256]; // up to 256 channels in a Vorbis stream

  int[] timesubmap = new int[16]; // [mux]
  int[] floorsubmap = new int[16]; // [mux] submap to floors
  int[] residuesubmap = new int[16]; // [mux] submap to residue
  int[] psysubmap = new int[16]; // [mux]; encode only

  int couplingSteps;
  int[] couplingMag = new int[256];
  int[] couplingAng = new int[256];

  void free() {
    chmuxlist = null;
    timesubmap = null;
    floorsubmap = null;
    residuesubmap = null;
    psysubmap = null;

    couplingMag = null;
    couplingAng = null;
  }
}
