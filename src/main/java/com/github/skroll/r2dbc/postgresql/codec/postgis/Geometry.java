package com.github.skroll.r2dbc.postgresql.codec.postgis;

public abstract class Geometry {
  public static final int LINEARRING = 0;
  public static final int POINT = 1;
  public static final int LINESTRING = 2;
  public static final int POLYGON = 3;
  public static final int MULTIPOINT = 4;
  public static final int MULTILINESTRING = 5;
  public static final int MULTIPOLYGON = 6;
  public static final int GEOMETRYCOLLECTION = 7;

  public final static int UNKNOWN_SRID = 0;

  private final int dimension;
  private final boolean hasMeasure;
  private final int srid;

  public Geometry(int dimension, boolean hasMeasure, int srid) {
    this.dimension = dimension;
    this.hasMeasure = hasMeasure;
    this.srid = srid;
  }

  public abstract int getType();

  public int getDimension() {
    return dimension;
  }

  public boolean isMeasured() {
    return hasMeasure;
  }

  public int getSrid() {
    return srid;
  }
}
