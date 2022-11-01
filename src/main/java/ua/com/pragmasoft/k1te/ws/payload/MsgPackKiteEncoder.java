package ua.com.pragmasoft.k1te.ws.payload;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

public class MsgPackKiteEncoder implements Encoder.Binary<KiteMsg> {

  static final List<BiConsumer<MessagePacker, KiteMsg>> ENCODERS =
      List.of(MsgPackKiteEncoder::encodeConnected, MsgPackKiteEncoder::encodeDisconnected,
          MsgPackKiteEncoder::encodeError, MsgPackKiteEncoder::encodePlaintext);

  @Override
  public void init(EndpointConfig config) {
    // Empty init
  }

  @Override
  public void destroy() {
    // Empty destroy
  }

  @Override
  public ByteBuffer encode(KiteMsg message) throws EncodeException {
    final var type = message.type();
    try (final var packer = MessagePack.newDefaultBufferPacker()) {
      ENCODERS.get(type).accept(packer, message);
      return ByteBuffer.wrap(packer.toByteArray());
    } catch (IndexOutOfBoundsException oob) {
      throw new EncodeException(message, "Unsupported msg type %X".formatted(type));
    } catch (Exception e) {
      throw new EncodeException(message, e.getLocalizedMessage(), e);
    }
  }

  static void encodeConnected(MessagePacker packer, KiteMsg message) {
    final ConnectedMsg m = (ConnectedMsg) message;
    try {
      // @formatter:off
      packer
        .packArrayHeader(3)
        .packShort(m.type());
      ((m.endpoint() == null) ? packer.packNil() : packer.packString(m.endpoint()))
        .packString(m.userId());
      // @formatter:on
    } catch (IOException e) {
      throw new IllegalStateException(e.getLocalizedMessage(), e);
    }
  }

  static void encodeDisconnected(MessagePacker packer, KiteMsg message) {
    final DisconnectedMsg m = (DisconnectedMsg) message;
    try {
      // @formatter:off
      packer
        .packArrayHeader(2)
        .packShort(m.type())
        .packString(m.userId());
      // @formatter:on
    } catch (IOException e) {
      throw new IllegalStateException(e.getLocalizedMessage(), e);
    }
  }

  static void encodeError(MessagePacker packer, KiteMsg message) {
    final ErrorMsg m = (ErrorMsg) message;
    try {
      // @formatter:off
      packer
        .packArrayHeader(3)
        .packShort(m.type())
        .packString(m.reason())
        .packInt(m.code());
      // @formatter:on
    } catch (IOException e) {
      throw new IllegalStateException(e.getLocalizedMessage(), e);
    }
  }

  static void encodePlaintext(MessagePacker packer, KiteMsg message) {
    final var m = (PlaintextMsg) message;
    try {
      // @formatter:off
      packer
        .packArrayHeader(5)
        .packShort(m.type())
        .packString(m.msgId())
        .packTimestamp(m.timestamp())
        .packByte((byte) m.status().ordinal())
        .packString(m.payload());
      // @formatter:on
    } catch (IOException e) {
      throw new IllegalStateException(e.getLocalizedMessage(), e);
    }
  }

}