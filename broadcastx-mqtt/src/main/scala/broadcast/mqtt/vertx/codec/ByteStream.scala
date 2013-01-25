package broadcast.mqtt.vertx.codec

import org.vertx.java.core.buffer.Buffer
import org.jboss.netty.buffer.ChannelBuffers
import java.io.{DataInput, DataInputStream}

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 * @see org.jboss.netty.buffer.ChannelBuffer#readableBytes
 */
class ByteStream(initialSizeHint: Int = 64) {

  // All data values are in big-endian order:
  // higher order bytes precede lower order bytes
  // (MQTT V3.1 Protocol Specification - section 2.1)
  private val channelBuffer = ChannelBuffers.dynamicBuffer(ChannelBuffers.BIG_ENDIAN, initialSizeHint)

  /**
   * Appends the specified {@code Buffer} to the end of this Buffer. The buffer will expand as
   * necessary to accomodate any bytes written.<p>
   * Returns a reference to {@code this} so multiple operations can be appended together.
   */
  def appendBuffer(buff: Buffer): ByteStream = {
    // copied from org.vertx.java.core.buffer.Buffer#appendBuffer
    val cb = buff.getChannelBuffer
    channelBuffer.writeBytes(cb)
    cb.readerIndex(0) // Need to reset readerindex since Netty write modifies readerIndex of source!
    this
  }

  /**
   * Discards the bytes between the 0th index and {@code readerIndex}.
   * It moves the bytes between {@code readerIndex} and {@code writerIndex}
   * to the 0th index, and sets {@code readerIndex} and {@code writerIndex}
   * to {@code 0} and {@code oldWriterIndex - oldReaderIndex} respectively.
   * <p>
   * Please refer to the class documentation for more detailed explanation.
   */
  def discardReadBytes() {
    channelBuffer.discardReadBytes()
  }

  /**
   *
   * @see org.jboss.netty.buffer.ChannelBuffer#readableBytes
   */
  def readableBytes():Int = channelBuffer.readableBytes()

  def markReaderIndex(): ByteStream = {
    channelBuffer.markReaderIndex()
    this
  }

  def resetReaderIndex(): ByteStream = {
    channelBuffer.resetReaderIndex()
    this
  }

  def readerIndex():Int = channelBuffer.readerIndex()

  def readByte(): Byte = channelBuffer.readByte()

  def readUnsignedByte(): Short = channelBuffer.readUnsignedByte()

  def readShort(): Short = channelBuffer.readShort()

  def readUnsignedShort(): Int = channelBuffer.readUnsignedShort()

  def readInt(): Int = channelBuffer.readInt()

  def readUnsignedInt(): Long = channelBuffer.readUnsignedInt()

  def readLong(): Long = channelBuffer.readLong()

  def readBytes(b: Array[Byte], off: Int, len: Int) {
    channelBuffer.readBytes(b, off, len)
  }

  def readBytes(b: Array[Byte]) {
    channelBuffer.readBytes(b, 0, b.length)
  }

  /**
   * A 16-bit word is presented on the wire as Most Significant Byte (MSB),
   * followed by Least Significant Byte (LSB).
   * @see #readUnsignedShort()
   */
  def read16bitInt(): Int =
  // Note buffer is supposed to be in BigEndian (according to the spec. and the server conf')
  // it could be replaced by `buffer.readShort()`
  //    val msb = (buffer.readByte() & 0x00FF).asInstanceOf[Int]
  //    val lsb = (buffer.readByte() & 0x00FF).asInstanceOf[Int]
  //    (msb << 8) | lsb
    channelBuffer.readUnsignedShort()

  /**
   * In MQTT, strings are prefixed with two bytes to denote the length.
   * String Length is the number of bytes of encoded string characters, not the number of characters.
   * The Java writeUTF() and readUTF() data stream methods use this format.
   *
   * (MQTT V3.1 Protocol Specification - section 2.5)
   */
  def readUTF(): String =
  // The Java writeUTF() and readUTF() data stream methods use this format.
  // (MQTT V3.1 Protocol Specification - section 2.5)

  // so just rely on it, it would be safer
    DataInputStream.readUTF(new BufferAsDataInput(this))
}


/**
 * Minimal type conversion, most methods will throw an exception.
 * thus it is kept private...
 */
private class BufferAsDataInput(stream: ByteStream) extends DataInput {

  private def notImplemented[T](): T = {
    throw new UnsupportedOperationException
  }

  def readByte():Byte = stream.readByte()

  def readUnsignedByte():Int = stream.readUnsignedByte()

  def readShort():Short = stream.readShort()

  def readUnsignedShort():Int = stream.readUnsignedShort()

  def readInt():Int = stream.readInt()

  def readLong() = notImplemented()

  def readChar() = notImplemented()

  def readFloat() = notImplemented()

  def readDouble() = notImplemented()

  def readUTF() = notImplemented()

  def readLine() = notImplemented()

  def readBoolean() = notImplemented()

  def skipBytes(n: Int) = notImplemented()

  def readFully(b: Array[Byte], off: Int, len: Int) {
    stream.readBytes(b, off, len)
  }

  def readFully(b: Array[Byte]) {
    stream.readBytes(b)
  }
}
