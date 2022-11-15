package RPC.Encode.Impl;

import com.alibaba.fastjson.JSON;
import RPC.Encode.Encoder;

public class JSONEncoder implements Encoder {

    @Override
    public byte[] encode(Object obj) {
        return JSON.toJSONBytes(obj);
    }
}
