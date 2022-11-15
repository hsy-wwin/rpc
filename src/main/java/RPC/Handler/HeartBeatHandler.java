package RPC.Handler;

import RPC.Encode.Encoder;
import RPC.Encode.Impl.JSONEncoder;
import RPC.Protocol.ProtocolUtils;
import RPC.RpcManager;
import RPC.proto.Peer;
import io.protostuff.Rpc;
import lombok.SneakyThrows;
import redis.clients.jedis.Jedis;
import utils.redis.RedisUtils;
import utils.redis.ServiceStoreType;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class HeartBeatHandler implements Runnable {

    @Override
    public void run() {

        Map<String, Method> servicesMethod = RpcManager.servicesMethod;

        Map<Method, Class<?>> methodClazz = RpcManager.methodClazz;

        Map<String, byte[]> serviceStoreTypeMap = RpcManager.serviceStoreTypeMap;

        while (true) {
            //创建过期key,将信息封装放到redis中
            Jedis connect = RedisUtils.getConnect();
            //将当前的服务注册到远程redis注册中心中
            Set<String> methodName = servicesMethod.keySet();

            for (String s : methodName) {

                byte[] serialize = serviceStoreTypeMap.get(s);

                Long curTime = System.currentTimeMillis();
                //应该是先判断key是否过期
                byte[] expire = connect.hget(s.getBytes(StandardCharsets.UTF_8), serialize);

                if(expire == null){
                    System.out.println("enter");
                    connect.hset(s.getBytes(StandardCharsets.UTF_8), serialize, String.valueOf(curTime).getBytes(StandardCharsets.UTF_8));
                    connect.publish(s.getBytes(StandardCharsets.UTF_8), serialize);
                }
                else {
                    long preTime = Long.parseLong(new String(expire));
                    //证明过期(可能是服务提供者重启了,然后重新注册服务)，只是常规的通知，那么就不需要发布通知
                    if (curTime - preTime > 10000) {
                        connect.publish(s.getBytes(StandardCharsets.UTF_8), serialize);
                    }
                    connect.hset(s.getBytes(StandardCharsets.UTF_8), serialize, String.valueOf(curTime).getBytes(StandardCharsets.UTF_8));
                }
            }

            //Test
//            RpcManager.getObj();

            try {
                Thread.sleep(4500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
