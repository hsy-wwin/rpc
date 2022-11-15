package RPC.Decode;

public interface Decoder {
    <T>T decode(byte[] bytes,Class<T> clazz);
}
