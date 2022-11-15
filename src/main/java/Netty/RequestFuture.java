package Netty;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class RequestFuture {
    public static Map<Long,RequestFuture> futures = new ConcurrentHashMap<Long,RequestFuture>();
    //请求id
    private long id;
    //请求参数
    private Object request;
    //响应结果
    private Object result;
    //超时时间
    private long timeOut = 5000;

    public static void addFuture(RequestFuture result){
        futures.put(result.getId(),result);
    }
    //同步获取响应结果
    public Object get(){
        synchronized (this){
            while (this.result == null){
                try {
                    this.wait(timeOut);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.result;
    }

    public static void received(Response result){
        RequestFuture remove = futures.remove(result.getId());
        if(remove != null){
            remove.setResult(result.getResult());
        }
        synchronized (remove){
            remove.notify();
        }

    }
}
