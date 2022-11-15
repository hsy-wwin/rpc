package Work.Ribbon;

import RPC.Protocol.ProtocolUtils;
import RPC.proto.Peer;
import Work.Work;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import utils.redis.RedisUtils;
import utils.redis.ServiceStoreType;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoundedChooseServer {

    public static int Count = 0;

    public static ServiceStoreType GetService(String serviceName){
        Work.lock.lock();
        if(!Work.servicesMap.containsKey(serviceName)){

            List<ServiceStoreType> serviceList = new ArrayList<>();

            Jedis connect = RedisUtils.getConnect();

            Map<byte[], byte[]> map = connect.hgetAll(serviceName.getBytes(StandardCharsets.UTF_8));

            Set<byte[]> vals = map.keySet();

            long time = System.currentTimeMillis();

            Map<Peer,ServiceStoreType> values = new ConcurrentHashMap<>();
            //解析不同服务器提供的相同服务
            for(byte[] val : vals){
                ServiceStoreType serviceDetail = ProtocolUtils.deserialize(val, ServiceStoreType.class);
                Peer peer = serviceDetail.getPeer();
                long preTime = Long.parseLong(new String(map.get(val)));
                //先判断是否过期
                if(time - preTime <= 10000){
                    //可以将他放到客户端缓存中
                    values.put(serviceDetail.getPeer(),serviceDetail);
                    //然后将当前这个放到servicelist,准备选举服务器
                    serviceList.add(serviceDetail);
                }
            }
            //把结果缓存起来
            Work.servicesMap.put(serviceName,values);

            if(serviceList.size() == 0){
                Work.lock.unlock();
                return null;
            }
            else {
                Count++;
                Work.lock.unlock();
                return serviceList.get(Count % serviceList.size());
            }
        }
        else{
            Map<Peer, ServiceStoreType> peerServiceStoreTypeMap = Work.servicesMap.get(serviceName);
            Collection<ServiceStoreType> values = peerServiceStoreTypeMap.values();
            ServiceStoreType[] serviceStoreTypes = (ServiceStoreType[]) values.toArray();
            Count++;
            Work.lock.unlock();
            return serviceStoreTypes[Count % serviceStoreTypes.length];
        }
    }
}
