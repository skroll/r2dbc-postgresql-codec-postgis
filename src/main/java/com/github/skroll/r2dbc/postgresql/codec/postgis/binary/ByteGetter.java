package com.github.skroll.r2dbc.postgresql.codec.postgis.binary;

import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.message.Format;

public abstract class ByteGetter {
  protected final ByteBuf buffer;

  public ByteGetter(final ByteBuf buffer) {
    this.buffer = buffer;
  }

  public abstract byte readByte();

  public abstract int readInt();
  public abstract int readIntLE();

  public abstract long readLong();
  public abstract long readLongLE();

  public static ByteGetter forFormat(final Format format, final ByteBuf buffer) {
    if (format == Format.FORMAT_TEXT) {
      return forText(buffer);
    } else if (format == Format.FORMAT_BINARY) {
      return forBinary(buffer);
    } else {
      throw new IllegalArgumentException("Unknown format: " + format);
    }
  }

  public static ByteGetter forBinary(final ByteBuf buffer) {
    return new BinaryByteGetter(buffer);
  }

  public static ByteGetter forText(final ByteBuf buffer) {
    return new TextByteGetter(buffer);
  }

  private static class BinaryByteGetter extends ByteGetter {
    public BinaryByteGetter(ByteBuf buffer) {
      super(buffer);
    }

    @Override
    public byte readByte() {
      return buffer.readByte();
    }

    @Override
    public int readInt() {
      return buffer.readInt();
    }

    @Override
    public int readIntLE() {
      return buffer.readIntLE();
    }

    @Override
    public long readLong() {
      return buffer.readLong();
    }

    @Override
    public long readLongLE() {
      return buffer.readLongLE();
    }
  }

  private static class TextByteGetter extends ByteGetter {
    public TextByteGetter(final ByteBuf buffer) {
      super(buffer);
    }

    @Override
    public byte readByte() {
      final int high = unhex(buffer.readByte());
      final int low = unhex(buffer.readByte());
      return (byte) ((high << 4) + low);
    }

    @Override
    public int readInt() {
      final int b0 = readByte() & 0xff;
      final int b1 = readByte() & 0xff;
      final int b2 = readByte() & 0xff;
      final int b3 = readByte() & 0xff;

      return (b0 << 24) + (b1 << 16) + (b2 << 8) + b3;
    }

    @Override
    public int readIntLE() {
      final int b0 = readByte() & 0xff;
      final int b1 = readByte() & 0xff;
      final int b2 = readByte() & 0xff;
      final int b3 = readByte() & 0xff;

      return (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
    }

    @Override
    public long readLong() {
      final long b0 = readByte() & 0xff;
      final long b1 = readByte() & 0xff;
      final long b2 = readByte() & 0xff;
      final long b3 = readByte() & 0xff;
      final long b4 = readByte() & 0xff;
      final long b5 = readByte() & 0xff;
      final long b6 = readByte() & 0xff;
      final long b7 = readByte() & 0xff;

      return (b0 << 56) + (b1 << 48) + (b2 << 40) + (b3 << 32)
          + (b4 << 24) + (b5 << 16) + (b6 << 8) + b7;
    }

    @Override
    public long readLongLE() {
      final long b0 = readByte() & 0xff;
      final long b1 = readByte() & 0xff;
      final long b2 = readByte() & 0xff;
      final long b3 = readByte() & 0xff;
      final long b4 = readByte() & 0xff;
      final long b5 = readByte() & 0xff;
      final long b6 = readByte() & 0xff;
      final long b7 = readByte() & 0xff;

      return (b7 << 56) + (b6 << 48) + (b5 << 40) + (b4 << 32)
          + (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
    }

    static byte unhex(final byte c) {
      if (c >= '0' && c <= '9') {
        return (byte) (c - '0');
      } else if (c >= 'A' && c <= 'F') {
        return (byte) (c - 'A' + 10);
      } else if (c >= 'a' && c <= 'f') {
        return (byte) (c - 'a' + 10);
      } else {
        throw new IllegalArgumentException("No valid Hex char " + c);
      }
    }
  }
}
