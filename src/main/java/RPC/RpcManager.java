package RPC;

import Interface.RpcService;
import RPC.Handler.HeartBeatHandler;
import RPC.Protocol.ProtocolUtils;
import RPC.proto.Peer;
import redis.clients.jedis.Jedis;
import utils.redis.RedisUtils;
import utils.redis.ServiceStoreType;

import java.io.File;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RpcManager {

    public static Map<String,Method> servicesMethod = new ConcurrentHashMap<>();

    public static Map<Method,Class<?>> methodClazz = new ConcurrentHashMap<>();

    public static Map<String,byte[]> serviceStoreTypeMap = new ConcurrentHashMap<>();
    /**
     * 1.扫描当前服务器包下的信息
     * 2.记录主机号和连接信息 根据服务名进行划分
     * 3.发送给注册中心 Redis
     * 4.然后写个定时任务，定时更新到本地缓存
     */
    //服务发现和服务注册 本地调用 应该不存在并发问题
    public static void init(String packageName) throws Exception{
        //到时从配置文件读吧
        getServices(packageName);
        new Thread(new HeartBeatHandler()).start();
    }
    //Test用的
    public static void getObj(){
        String method = "WorkReport";
        Jedis jedis = RedisUtils.getConnect();

        Map<byte[], byte[]> map = jedis.hgetAll(method.getBytes(StandardCharsets.UTF_8));
        Set<byte[]> bytes = map.keySet();
        for(byte[] val : bytes){
            byte[] bytes1 = map.get(val);
            Long time = Long.parseLong(new String(bytes1));
            ServiceStoreType deserialize = ProtocolUtils.deserialize(val, ServiceStoreType.class);
        }
    }

    public static void main(String[] args) throws Exception{
        init("Master");
    }

    //扫描包下所有的rpc服务
    public static void getServices(String clazz){
        try {
            String[] clazzes = clazz.split(",");
            List<Class<?>> classes = new ArrayList<Class<?>>();
            for(String cl : clazzes){
                List<Class<?>> classList = getClasses(cl);
                classes.addAll(classList);
            }
            for(Class<?> cla : classes){
                Object obj = cla.newInstance();
            }
            return ;
        }catch (Exception e){
            e.printStackTrace();
        }
        return ;
    }

    public static List<Class<?>> getClasses(String pckgname) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        File directory = null;
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null)
                throw new ClassNotFoundException("Can't get class loader.");
            String path = pckgname.replace('.', '/');
            URL resource = cld.getResource(path);
            if (resource == null)
                throw new ClassNotFoundException("No resource for " + path);
            directory = new File(resource.getFile());
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " (" + directory + ") does not appear to be a valid package a");
        }
        if (directory.exists()) {
            //获取所有文件
            String[] files = directory.list();
            File[] fileList = directory.listFiles();
            for (int i = 0;fileList != null && i < fileList.length; i++) {
                File file = fileList[i];
                //判断是否是Class文件
                if (file.isFile() && file.getName().endsWith(".class")) {
                    //得到类文件
                    Class<?> clazz = Class.forName(pckgname + '.' + files[i].substring(0, files[i].length() - 6));
                    Method[] methods = clazz.getMethods();
                    for(Method method : methods){
                        if(method.getAnnotation(RpcService.class) != null) {
                            servicesMethod.put(method.getName(),method);
                            methodClazz.put(method,clazz);
                            ServiceStoreType serviceStoreType = new ServiceStoreType();
                            serviceStoreType.setaClass(clazz);
                            serviceStoreType.setClazz(clazz.getName());
                            serviceStoreType.setMethod(method);
                            serviceStoreType.setMethodName(method.getName());
                            try {
                                serviceStoreType.setPeer(new Peer(Inet4Address.getLocalHost().getHostAddress(),2595));
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            byte[] serialize = ProtocolUtils.serialize(serviceStoreType);
                            serviceStoreTypeMap.put(method.getName(),serialize);
                        }
                    }
                    if(clazz.getAnnotation(RpcService.class) != null){
                        classes.add(clazz);
                    }
                }else if(file.isDirectory()){ //如果是目录，递归查找
                    List<Class<?>> result = getClasses(pckgname+"."+file.getName());
                    if(result != null && result.size() != 0){
                        classes.addAll(result);
                    }
                }
            }
        } else{
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package b");
        }
        return classes;
    }


}
