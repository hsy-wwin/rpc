package RPC.Decode.Impl;

import com.alibaba.fastjson.JSON;
import RPC.Decode.Decoder;

public class JSONDecoder implements Decoder {
    @Override
    public <T> T decode(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes,clazz);
    }
}
