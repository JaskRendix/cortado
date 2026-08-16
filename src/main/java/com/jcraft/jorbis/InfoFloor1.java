package com.jcraft.jorbis;

class InfoFloor1 {
  static final int VIF_POSIT = 63;
  static final int VIF_CLASS = 16;
  static final int VIF_PARTS = 31;

  int partitions; /* 0 to 31 */
  int[] partitionclass = new int[VIF_PARTS]; /* 0 to 15 */

  int[] class_dim = new int[VIF_CLASS]; /* 1 to 8 */
  int[] class_subs = new int[VIF_CLASS]; /* 0,1,2,3 (bits: 1<<n poss) */
  int[] class_book = new int[VIF_CLASS]; /* subs ^ dim entries */
  int[][] class_subbook = new int[VIF_CLASS][]; /* [VIF_CLASS][subs] */

  int mult; /* 1 2 3 or 4 */
  int[] postlist = new int[VIF_POSIT + 2]; /* first two implicit */

  float maxover;
  float maxunder;
  float maxerr;

  int twofitminsize;
  int twofitminused;
  int twofitweight;
  float twofitatten;
  int unusedminsize;
  int unusedmin_n;

  int n;

  InfoFloor1() {
    for (int i = 0; i < class_subbook.length; i++) {
      class_subbook[i] = new int[8];
    }
  }

  void free() {
    partitionclass = null;
    class_dim = null;
    class_subs = null;
    class_book = null;
    class_subbook = null;
    postlist = null;
  }

  Object copy_info() {
    InfoFloor1 info = this;
    InfoFloor1 ret = new InfoFloor1();

    ret.partitions = info.partitions;
    System.arraycopy(info.partitionclass, 0, ret.partitionclass, 0, VIF_PARTS);
    System.arraycopy(info.class_dim, 0, ret.class_dim, 0, VIF_CLASS);
    System.arraycopy(info.class_subs, 0, ret.class_subs, 0, VIF_CLASS);
    System.arraycopy(info.class_book, 0, ret.class_book, 0, VIF_CLASS);

    for (int j = 0; j < VIF_CLASS; j++) {
      System.arraycopy(info.class_subbook[j], 0, ret.class_subbook[j], 0, 8);
    }

    ret.mult = info.mult;
    System.arraycopy(info.postlist, 0, ret.postlist, 0, VIF_POSIT + 2);

    ret.maxover = info.maxover;
    ret.maxunder = info.maxunder;
    ret.maxerr = info.maxerr;

    ret.twofitminsize = info.twofitminsize;
    ret.twofitminused = info.twofitminused;
    ret.twofitweight = info.twofitweight;
    ret.twofitatten = info.twofitatten;
    ret.unusedminsize = info.unusedminsize;
    ret.unusedmin_n = info.unusedmin_n;

    ret.n = info.n;

    return ret;
  }
}
