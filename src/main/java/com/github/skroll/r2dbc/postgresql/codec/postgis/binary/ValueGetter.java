package com.github.skroll.r2dbc.postgresql.codec.postgis.binary;

public abstract class ValueGetter {
  protected final ByteGetter buffer;

  public ValueGetter(final ByteGetter buffer) {
    this.buffer = buffer;
  }

  public byte readByte() {
    return buffer.readByte();
  }

  public abstract int readInt();
  public abstract long readLong();

  public double readDouble() {
    final long bits = readLong();
    return Double.longBitsToDouble(bits);
  }

  private static final byte BE_BOM = 0;
  private static final byte LE_BOM = 1;

  public static ValueGetter forBom(final byte bom, final ByteGetter buffer) {
    if (bom == BE_BOM) {
      return forBigEndian(buffer);
    } else if (bom == LE_BOM) {
      return forLittleEndian(buffer);
    } else {
      throw new IllegalArgumentException("Unknown bom value: " + bom);
    }
  }

  public static ValueGetter forBigEndian(final ByteGetter buffer) {
    return new BigEndianValueGetter(buffer);
  }

  public static ValueGetter forLittleEndian(final ByteGetter buffer) {
    return new LittleEndianValueGetter(buffer);
  }

  private static class BigEndianValueGetter extends ValueGetter {
    public BigEndianValueGetter(ByteGetter buffer) {
      super(buffer);
    }

    @Override
    public int readInt() {
      return buffer.readInt();
    }

    @Override
    public long readLong() {
      return buffer.readLong();
    }
  }

  private static class LittleEndianValueGetter extends ValueGetter {
    public LittleEndianValueGetter(ByteGetter buffer) {
      super(buffer);
    }

    @Override
    public int readInt() {
      return buffer.readIntLE();
    }

    @Override
    public long readLong() {
      return buffer.readLongLE();
    }
  }
}
