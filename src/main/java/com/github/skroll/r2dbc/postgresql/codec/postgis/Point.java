package com.github.skroll.r2dbc.postgresql.codec.postgis;

public class Point extends Geometry {
  private final double x;
  private final double y;
  private final double z;
  private final double m;

  private Point(
      final int dimension, final boolean hasMeasure, final int srid,
      final double x, final double y, final double z, final double m
  ) {
    super(dimension, hasMeasure, srid);
    this.x = x;
    this.y = y;
    this.z = z;
    this.m = m;
  }

  @Override
  public int getType() {
    return Geometry.POINT;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getZ() {
    return z;
  }

  public double getMeasure() {
    return m;
  }

  public static Point of2D(final double x, final double y, final double m, final boolean hasMeasure, final int srid) {
    return new Point(2, hasMeasure, srid, x, y, 0.0, m);
  }

  public static Point of3D(final double x, final double y, final double z, final double m, final boolean hasMeasure, final int srid) {
    return new Point(2, hasMeasure, srid, x, y, z, m);
  }
}
