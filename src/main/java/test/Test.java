package test;

import RPC.Protocol.ProtocolUtils;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import utils.redis.RedisUtils;
import utils.redis.ServiceStoreType;
import utils.redis.Subscriber;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Test {

    public static void main(String[] args) {
//        Jedis connect = RedisUtils.getConnect();

//        connect.subscribe(new BinaryJedisPubSub() {
//            @Override
//            public void onMessage(byte[] channel, byte[] message) {
//                String method = new String(channel);
//                System.out.println(method);
//                String val = new String(message);
//                System.out.println(message);
//                ServiceStoreType deserialize = ProtocolUtils.deserialize(message, ServiceStoreType.class);
//                System.out.println(deserialize);
//            }
//        },"WorkReport".getBytes(StandardCharsets.UTF_8));
    }
}
