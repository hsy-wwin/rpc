package utils.redis;

import RPC.Protocol.ProtocolUtils;
import Work.Work;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import java.nio.charset.StandardCharsets;

public class Subscriber extends BinaryJedisPubSub implements Runnable {

    public String channelName;

    public Subscriber(String channelName){
        this.channelName = channelName;
    }

    @Override
    public void onMessage(byte[] channel, byte[] message) {
        //这个是消息推送过来嘛,对于共享的map需要加锁
        Work.lock.lock();
        String serviceName = new String(channel);
        System.out.println("服务:" + serviceName + "收到订阅通知");
        String service = new String(message);
        System.out.println("服务内容:" + service);
        ServiceStoreType serviceDetail = ProtocolUtils.deserialize(message, ServiceStoreType.class);
        Work.servicesMap.get(serviceName).replace(serviceDetail.peer,serviceDetail);
        Work.lock.unlock();
    }

    @Override
    public void run() {
        Jedis jedis = RedisUtils.getConnect();

        jedis.subscribe(this,channelName.getBytes(StandardCharsets.UTF_8));
    }
}
