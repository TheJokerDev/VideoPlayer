package me.j0keer.videoplayer.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FriendlyByteBuf extends ByteBuf {
    private final ByteBuf buf;

    public FriendlyByteBuf(ByteBuf byteBuf) {
        this.buf = byteBuf;
    }

    public FriendlyByteBuf() {
        this(Unpooled.buffer());
    }

    public ByteBuf getUnderlyingByteBuf() {
        return this.buf;
    }

    public FriendlyByteBuf writeByteArray(byte[] bs) {
        writeVarInt(bs.length);
        writeBytes(bs);
        return this;
    }

    public byte[] readByteArray() {
        return readByteArray(readableBytes());
    }

    public byte[] readByteArray(int i) {
        int j = readVarInt();
        if (j > i)
            throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + i);
        byte[] bs = new byte[j];
        readBytes(bs);
        return bs;
    }

    public int readVarInt() {
        byte b;
        int i = 0;
        int j = 0;
        do {
            b = readByte();
            i |= (b & Byte.MAX_VALUE) << j++ * 7;
            if (j > 5)
                throw new RuntimeException("VarInt too big");
        } while ((b & 0x80) == 128);
        return i;
    }

    public FriendlyByteBuf writeUUID(UUID uUID) {
        writeLong(uUID.getMostSignificantBits());
        writeLong(uUID.getLeastSignificantBits());
        return this;
    }

    public UUID readUUID() {
        return new UUID(readLong(), readLong());
    }

    public FriendlyByteBuf writeVarInt(int i) {
        while ((i & 0xFFFFFF80) != 0) {
            writeByte(i & 0x7F | 0x80);
            i >>>= 7;
        }
        writeByte(i);
        return this;
    }

    public String readUtf(int i) {
        int j = readVarInt();
        if (j > i * 4)
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + i * 4 + ")");
        if (j < 0)
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        String string = toString(readerIndex(), j, StandardCharsets.UTF_8);
        readerIndex(readerIndex() + j);
        if (string.length() > i)
            throw new DecoderException("The received string length is longer than maximum allowed (" + j + " > " + i + ")");
        return string;
    }

    public FriendlyByteBuf writeUtf(String string, int i) {
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        if (bs.length > i)
            throw new EncoderException("String too big (was " + bs.length + " bytes encoded, max " + i + ")");
        writeVarInt(bs.length);
        writeBytes(bs);
        return this;
    }

    public String readUtf() {
        return readUtf(32767);
    }

    public FriendlyByteBuf writeUtf(String string) {
        return writeUtf(string, 32767);
    }

    public int capacity() {
        return this.buf.capacity();
    }

    public ByteBuf capacity(int i) {
        return this.buf.capacity(i);
    }

    public int maxCapacity() {
        return this.buf.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return this.buf.alloc();
    }

    @SuppressWarnings("deprecation")
    public ByteOrder order() {
        return this.buf.order();
    }

    @SuppressWarnings("deprecation")
    public ByteBuf order(ByteOrder byteOrder) {
        return this.buf.order(byteOrder);
    }

    public ByteBuf unwrap() {
        return this.buf.unwrap();
    }

    public boolean isDirect() {
        return this.buf.isDirect();
    }

    public boolean isReadOnly() {
        return this.buf.isReadOnly();
    }

    public ByteBuf asReadOnly() {
        return this.buf.asReadOnly();
    }

    public int readerIndex() {
        return this.buf.readerIndex();
    }

    public ByteBuf readerIndex(int i) {
        return this.buf.readerIndex(i);
    }

    public int writerIndex() {
        return this.buf.writerIndex();
    }

    public ByteBuf writerIndex(int i) {
        return this.buf.writerIndex(i);
    }

    public ByteBuf setIndex(int i, int j) {
        return this.buf.setIndex(i, j);
    }

    public int readableBytes() {
        return this.buf.readableBytes();
    }

    public int writableBytes() {
        return this.buf.writableBytes();
    }

    public int maxWritableBytes() {
        return this.buf.maxWritableBytes();
    }

    public boolean isReadable() {
        return this.buf.isReadable();
    }

    public boolean isReadable(int i) {
        return this.buf.isReadable(i);
    }

    public boolean isWritable() {
        return this.buf.isWritable();
    }

    public boolean isWritable(int i) {
        return this.buf.isWritable(i);
    }

    public ByteBuf clear() {
        return this.buf.clear();
    }

    public ByteBuf markReaderIndex() {
        return this.buf.markReaderIndex();
    }

    public ByteBuf resetReaderIndex() {
        return this.buf.resetReaderIndex();
    }

    public ByteBuf markWriterIndex() {
        return this.buf.markWriterIndex();
    }

    public ByteBuf resetWriterIndex() {
        return this.buf.resetWriterIndex();
    }

    public ByteBuf discardReadBytes() {
        return this.buf.discardReadBytes();
    }

    public ByteBuf discardSomeReadBytes() {
        return this.buf.discardSomeReadBytes();
    }

    public ByteBuf ensureWritable(int i) {
        return this.buf.ensureWritable(i);
    }

    public int ensureWritable(int i, boolean bl) {
        return this.buf.ensureWritable(i, bl);
    }

    public boolean getBoolean(int i) {
        return this.buf.getBoolean(i);
    }

    public byte getByte(int i) {
        return this.buf.getByte(i);
    }

    public short getUnsignedByte(int i) {
        return this.buf.getUnsignedByte(i);
    }

    public short getShort(int i) {
        return this.buf.getShort(i);
    }

    public short getShortLE(int i) {
        return this.buf.getShortLE(i);
    }

    public int getUnsignedShort(int i) {
        return this.buf.getUnsignedShort(i);
    }

    public int getUnsignedShortLE(int i) {
        return this.buf.getUnsignedShortLE(i);
    }

    public int getMedium(int i) {
        return this.buf.getMedium(i);
    }

    public int getMediumLE(int i) {
        return this.buf.getMediumLE(i);
    }

    public int getUnsignedMedium(int i) {
        return this.buf.getUnsignedMedium(i);
    }

    public int getUnsignedMediumLE(int i) {
        return this.buf.getUnsignedMediumLE(i);
    }

    public int getInt(int i) {
        return this.buf.getInt(i);
    }

    public int getIntLE(int i) {
        return this.buf.getIntLE(i);
    }

    public long getUnsignedInt(int i) {
        return this.buf.getUnsignedInt(i);
    }

    public long getUnsignedIntLE(int i) {
        return this.buf.getUnsignedIntLE(i);
    }

    public long getLong(int i) {
        return this.buf.getLong(i);
    }

    public long getLongLE(int i) {
        return this.buf.getLongLE(i);
    }

    public char getChar(int i) {
        return this.buf.getChar(i);
    }

    public float getFloat(int i) {
        return this.buf.getFloat(i);
    }

    public double getDouble(int i) {
        return this.buf.getDouble(i);
    }

    public ByteBuf getBytes(int i, ByteBuf byteBuf) {
        return this.buf.getBytes(i, byteBuf);
    }

    public ByteBuf getBytes(int i, ByteBuf byteBuf, int j) {
        return this.buf.getBytes(i, byteBuf, j);
    }

    public ByteBuf getBytes(int i, ByteBuf byteBuf, int j, int k) {
        return this.buf.getBytes(i, byteBuf, j, k);
    }

    public ByteBuf getBytes(int i, byte[] bs) {
        return this.buf.getBytes(i, bs);
    }

    public ByteBuf getBytes(int i, byte[] bs, int j, int k) {
        return this.buf.getBytes(i, bs, j, k);
    }

    public ByteBuf getBytes(int i, ByteBuffer byteBuffer) {
        return this.buf.getBytes(i, byteBuffer);
    }

    public ByteBuf getBytes(int i, OutputStream outputStream, int j) throws IOException {
        return this.buf.getBytes(i, outputStream, j);
    }

    public int getBytes(int i, GatheringByteChannel gatheringByteChannel, int j) throws IOException {
        return this.buf.getBytes(i, gatheringByteChannel, j);
    }

    public int getBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
        return this.buf.getBytes(i, fileChannel, l, j);
    }

    public CharSequence getCharSequence(int i, int j, Charset charset) {
        return this.buf.getCharSequence(i, j, charset);
    }

    public ByteBuf setBoolean(int i, boolean bl) {
        return this.buf.setBoolean(i, bl);
    }

    public ByteBuf setByte(int i, int j) {
        return this.buf.setByte(i, j);
    }

    public ByteBuf setShort(int i, int j) {
        return this.buf.setShort(i, j);
    }

    public ByteBuf setShortLE(int i, int j) {
        return this.buf.setShortLE(i, j);
    }

    public ByteBuf setMedium(int i, int j) {
        return this.buf.setMedium(i, j);
    }

    public ByteBuf setMediumLE(int i, int j) {
        return this.buf.setMediumLE(i, j);
    }

    public ByteBuf setInt(int i, int j) {
        return this.buf.setInt(i, j);
    }

    public ByteBuf setIntLE(int i, int j) {
        return this.buf.setIntLE(i, j);
    }

    public ByteBuf setLong(int i, long l) {
        return this.buf.setLong(i, l);
    }

    public ByteBuf setLongLE(int i, long l) {
        return this.buf.setLongLE(i, l);
    }

    public ByteBuf setChar(int i, int j) {
        return this.buf.setChar(i, j);
    }

    public ByteBuf setFloat(int i, float f) {
        return this.buf.setFloat(i, f);
    }

    public ByteBuf setDouble(int i, double d) {
        return this.buf.setDouble(i, d);
    }

    public ByteBuf setBytes(int i, ByteBuf byteBuf) {
        return this.buf.setBytes(i, byteBuf);
    }

    public ByteBuf setBytes(int i, ByteBuf byteBuf, int j) {
        return this.buf.setBytes(i, byteBuf, j);
    }

    public ByteBuf setBytes(int i, ByteBuf byteBuf, int j, int k) {
        return this.buf.setBytes(i, byteBuf, j, k);
    }

    public ByteBuf setBytes(int i, byte[] bs) {
        return this.buf.setBytes(i, bs);
    }

    public ByteBuf setBytes(int i, byte[] bs, int j, int k) {
        return this.buf.setBytes(i, bs, j, k);
    }

    public ByteBuf setBytes(int i, ByteBuffer byteBuffer) {
        return this.buf.setBytes(i, byteBuffer);
    }

    public int setBytes(int i, InputStream inputStream, int j) throws IOException {
        return this.buf.setBytes(i, inputStream, j);
    }

    public int setBytes(int i, ScatteringByteChannel scatteringByteChannel, int j) throws IOException {
        return this.buf.setBytes(i, scatteringByteChannel, j);
    }

    public int setBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
        return this.buf.setBytes(i, fileChannel, l, j);
    }

    public ByteBuf setZero(int i, int j) {
        return this.buf.setZero(i, j);
    }

    public int setCharSequence(int i, CharSequence charSequence, Charset charset) {
        return this.buf.setCharSequence(i, charSequence, charset);
    }

    public boolean readBoolean() {
        return this.buf.readBoolean();
    }

    public byte readByte() {
        return this.buf.readByte();
    }

    public short readUnsignedByte() {
        return this.buf.readUnsignedByte();
    }

    public short readShort() {
        return this.buf.readShort();
    }

    public short readShortLE() {
        return this.buf.readShortLE();
    }

    public int readUnsignedShort() {
        return this.buf.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return this.buf.readUnsignedShortLE();
    }

    public int readMedium() {
        return this.buf.readMedium();
    }

    public int readMediumLE() {
        return this.buf.readMediumLE();
    }

    public int readUnsignedMedium() {
        return this.buf.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return this.buf.readUnsignedMediumLE();
    }

    public int readInt() {
        return this.buf.readInt();
    }

    public int readIntLE() {
        return this.buf.readIntLE();
    }

    public long readUnsignedInt() {
        return this.buf.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return this.buf.readUnsignedIntLE();
    }

    public long readLong() {
        return this.buf.readLong();
    }

    public long readLongLE() {
        return this.buf.readLongLE();
    }

    public char readChar() {
        return this.buf.readChar();
    }

    public float readFloat() {
        return this.buf.readFloat();
    }

    public double readDouble() {
        return this.buf.readDouble();
    }

    public ByteBuf readBytes(int i) {
        return this.buf.readBytes(i);
    }

    public ByteBuf readSlice(int i) {
        return this.buf.readSlice(i);
    }

    public ByteBuf readRetainedSlice(int i) {
        return this.buf.readRetainedSlice(i);
    }

    public ByteBuf readBytes(ByteBuf byteBuf) {
        return this.buf.readBytes(byteBuf);
    }

    public ByteBuf readBytes(ByteBuf byteBuf, int i) {
        return this.buf.readBytes(byteBuf, i);
    }

    public ByteBuf readBytes(ByteBuf byteBuf, int i, int j) {
        return this.buf.readBytes(byteBuf, i, j);
    }

    public ByteBuf readBytes(byte[] bs) {
        return this.buf.readBytes(bs);
    }

    public ByteBuf readBytes(byte[] bs, int i, int j) {
        return this.buf.readBytes(bs, i, j);
    }

    public ByteBuf readBytes(ByteBuffer byteBuffer) {
        return this.buf.readBytes(byteBuffer);
    }

    public ByteBuf readBytes(OutputStream outputStream, int i) throws IOException {
        return this.buf.readBytes(outputStream, i);
    }

    public int readBytes(GatheringByteChannel gatheringByteChannel, int i) throws IOException {
        return this.buf.readBytes(gatheringByteChannel, i);
    }

    public CharSequence readCharSequence(int i, Charset charset) {
        return this.buf.readCharSequence(i, charset);
    }

    public int readBytes(FileChannel fileChannel, long l, int i) throws IOException {
        return this.buf.readBytes(fileChannel, l, i);
    }

    public ByteBuf skipBytes(int i) {
        return this.buf.skipBytes(i);
    }

    public ByteBuf writeBoolean(boolean bl) {
        return this.buf.writeBoolean(bl);
    }

    public ByteBuf writeByte(int i) {
        return this.buf.writeByte(i);
    }

    public ByteBuf writeShort(int i) {
        return this.buf.writeShort(i);
    }

    public ByteBuf writeShortLE(int i) {
        return this.buf.writeShortLE(i);
    }

    public ByteBuf writeMedium(int i) {
        return this.buf.writeMedium(i);
    }

    public ByteBuf writeMediumLE(int i) {
        return this.buf.writeMediumLE(i);
    }

    public ByteBuf writeInt(int i) {
        return this.buf.writeInt(i);
    }

    public ByteBuf writeIntLE(int i) {
        return this.buf.writeIntLE(i);
    }

    public ByteBuf writeLong(long l) {
        return this.buf.writeLong(l);
    }

    public ByteBuf writeLongLE(long l) {
        return this.buf.writeLongLE(l);
    }

    public ByteBuf writeChar(int i) {
        return this.buf.writeChar(i);
    }

    public ByteBuf writeFloat(float f) {
        return this.buf.writeFloat(f);
    }

    public ByteBuf writeDouble(double d) {
        return this.buf.writeDouble(d);
    }

    public ByteBuf writeBytes(ByteBuf byteBuf) {
        return this.buf.writeBytes(byteBuf);
    }

    public ByteBuf writeBytes(ByteBuf byteBuf, int i) {
        return this.buf.writeBytes(byteBuf, i);
    }

    public ByteBuf writeBytes(ByteBuf byteBuf, int i, int j) {
        return this.buf.writeBytes(byteBuf, i, j);
    }

    public ByteBuf writeBytes(byte[] bs) {
        return this.buf.writeBytes(bs);
    }

    public ByteBuf writeBytes(byte[] bs, int i, int j) {
        return this.buf.writeBytes(bs, i, j);
    }

    public ByteBuf writeBytes(ByteBuffer byteBuffer) {
        return this.buf.writeBytes(byteBuffer);
    }

    public int writeBytes(InputStream inputStream, int i) throws IOException {
        return this.buf.writeBytes(inputStream, i);
    }

    public int writeBytes(ScatteringByteChannel scatteringByteChannel, int i) throws IOException {
        return this.buf.writeBytes(scatteringByteChannel, i);
    }

    public int writeBytes(FileChannel fileChannel, long l, int i) throws IOException {
        return this.buf.writeBytes(fileChannel, l, i);
    }

    public ByteBuf writeZero(int i) {
        return this.buf.writeZero(i);
    }

    public int writeCharSequence(CharSequence charSequence, Charset charset) {
        return this.buf.writeCharSequence(charSequence, charset);
    }

    public int indexOf(int i, int j, byte b) {
        return this.buf.indexOf(i, j, b);
    }

    public int bytesBefore(byte b) {
        return this.buf.bytesBefore(b);
    }

    public int bytesBefore(int i, byte b) {
        return this.buf.bytesBefore(i, b);
    }

    public int bytesBefore(int i, int j, byte b) {
        return this.buf.bytesBefore(i, j, b);
    }

    public int forEachByte(ByteProcessor byteProcessor) {
        return this.buf.forEachByte(byteProcessor);
    }

    public int forEachByte(int i, int j, ByteProcessor byteProcessor) {
        return this.buf.forEachByte(i, j, byteProcessor);
    }

    public int forEachByteDesc(ByteProcessor byteProcessor) {
        return this.buf.forEachByteDesc(byteProcessor);
    }

    public int forEachByteDesc(int i, int j, ByteProcessor byteProcessor) {
        return this.buf.forEachByteDesc(i, j, byteProcessor);
    }

    public ByteBuf copy() {
        return this.buf.copy();
    }

    public ByteBuf copy(int i, int j) {
        return this.buf.copy(i, j);
    }

    public ByteBuf slice() {
        return this.buf.slice();
    }

    public ByteBuf retainedSlice() {
        return this.buf.retainedSlice();
    }

    public ByteBuf slice(int i, int j) {
        return this.buf.slice(i, j);
    }

    public ByteBuf retainedSlice(int i, int j) {
        return this.buf.retainedSlice(i, j);
    }

    public ByteBuf duplicate() {
        return this.buf.duplicate();
    }

    public ByteBuf retainedDuplicate() {
        return this.buf.retainedDuplicate();
    }

    public int nioBufferCount() {
        return this.buf.nioBufferCount();
    }

    public ByteBuffer nioBuffer() {
        return this.buf.nioBuffer();
    }

    public ByteBuffer nioBuffer(int i, int j) {
        return this.buf.nioBuffer(i, j);
    }

    public ByteBuffer internalNioBuffer(int i, int j) {
        return this.buf.internalNioBuffer(i, j);
    }

    public ByteBuffer[] nioBuffers() {
        return this.buf.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int i, int j) {
        return this.buf.nioBuffers(i, j);
    }

    public boolean hasArray() {
        return this.buf.hasArray();
    }

    public byte[] array() {
        return this.buf.array();
    }

    public int arrayOffset() {
        return this.buf.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return this.buf.hasMemoryAddress();
    }

    public long memoryAddress() {
        return this.buf.memoryAddress();
    }

    public String toString(Charset charset) {
        return this.buf.toString(charset);
    }

    public String toString(int i, int j, Charset charset) {
        return this.buf.toString(i, j, charset);
    }

    public int hashCode() {
        return this.buf.hashCode();
    }

    public boolean equals(Object object) {
        return this.buf.equals(object);
    }

    public int compareTo(ByteBuf byteBuf) {
        return this.buf.compareTo(byteBuf);
    }

    public String toString() {
        return this.buf.toString();
    }

    public ByteBuf retain(int i) {
        return this.buf.retain(i);
    }

    public ByteBuf retain() {
        return this.buf.retain();
    }

    public ByteBuf touch() {
        return this.buf.touch();
    }

    public ByteBuf touch(Object object) {
        return this.buf.touch(object);
    }

    public int refCnt() {
        return this.buf.refCnt();
    }

    public boolean release() {
        return this.buf.release();
    }

    public boolean release(int i) {
        return this.buf.release(i);
    }
}