package com.github.skroll.r2dbc.postgresql.codec.postgis.binary;

import com.github.skroll.r2dbc.postgresql.codec.postgis.Geometry;
import com.github.skroll.r2dbc.postgresql.codec.postgis.Point;

public class BinaryParser {
  public static Geometry parseGeometry(final ValueGetter data) {
    final int typeWord = data.readInt();
    final int realType = typeWord & 0x1FFFFFFF; // cut off high flag bits
    final boolean haveZ = (typeWord & 0x80000000) != 0;
    final boolean haveM = (typeWord & 0x40000000) != 0;
    final boolean haveS = (typeWord & 0x20000000) != 0;
    final int srid;

    if (!haveS) {
      srid = Geometry.UNKNOWN_SRID;
    } else {
      srid = data.readInt();
    }

    if (realType == 1) {
      return parsePoint(data, srid, haveZ, haveM);
    }

    return null;
  }

  private static Geometry parsePoint(final ValueGetter data, final int srid, final boolean haveZ, final boolean haveM) {
    final double x = data.readDouble();
    final double y = data.readDouble();

    final double z;

    if (haveZ) {
      z = data.readDouble();
    } else {
      z = 0.0;
    }

    final double m;

    if (haveM) {
      m = data.readDouble();
    } else {
      m = 0.0;
    }

    if (!haveZ) {
      return Point.of2D(x, y, m, haveM, srid);
    } else {
      return Point.of3D(x, y, z, m, haveM, srid);
    }
  }
}
