package Work;

import Master.Master;
import RPC.Decode.Decoder;
import RPC.Decode.Impl.JSONDecoder;
import RPC.Encode.Encoder;
import RPC.Encode.Impl.JSONEncoder;
import RPC.proto.Peer;
import Work.Ribbon.RoundedChooseServer;
import utils.*;
import utils.Request.ReportTaskState;
import utils.Request.RequestArgs;
import utils.Request.ServiceDescriptor;
import utils.Response.PushTaskState;
import utils.Response.ResponseData;
import utils.redis.ServiceStoreType;
import utils.redis.Subscriber;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Work {

    public static Lock lock = new ReentrantLock();

    public static Map<String,byte[]> channelMap = new ConcurrentHashMap<>();

    public static Map<String,Thread> threadMap = new ConcurrentHashMap<>();

    public static Map<String, Map<Peer,ServiceStoreType>> servicesMap = new ConcurrentHashMap<>();

    //需要订阅的服务信息嘛
    public static String[] services = new String[]{"PushTask","WorkReport"};

    //对服务进行订阅
    public void init(){
        //对每个Rpc服务进行订阅注册
        for(String service : services){
            Thread thread = new Thread(new Subscriber(service));
            threadMap.put(service,thread);
            channelMap.put(service,service.getBytes(StandardCharsets.UTF_8));
            thread.start();
        }
    }

    //先从缓存中找，没有的话再从redis注册中心拉
    public PushTaskState GetTask(String serviceName) throws Exception{
        ServiceStoreType serviceStoreType = RoundedChooseServer.GetService(serviceName);
        Peer peer = serviceStoreType.getPeer();
        Socket socket = getConnection(peer);
        OutputStream os = socket.getOutputStream();
        Encoder encoder = new JSONEncoder();
        call(serviceStoreType.methodName,new RequestArgs(),serviceStoreType.aClass,encoder,os);
        InputStream is = socket.getInputStream();
        byte[] bytes = new byte[1024];
        is.read(bytes);
        Decoder decoder = new JSONDecoder();
        Object decode = decoder.decode(bytes, serviceStoreType.method.getReturnType());
        is.close();
        os.close();
        socket.close();
        return (PushTaskState) decode;
    }

    public void doWork() throws Exception{
        Encoder encoder = new JSONEncoder();

        Decoder decoder = new JSONDecoder();

        PushTaskState decode = GetTask("PushTask");

        Socket socket1 = getConnection(new Peer("127.0.0.1",2595));

        OutputStream os1 = socket1.getOutputStream();

        InputStream is1 = socket1.getInputStream();

        Dispatcher(decode,encoder,os1);

        byte[] bytes1 = new byte[1024];

        is1.read(bytes1);

        ResponseData decode1 = decoder.decode(bytes1, ResponseData.class);
        System.out.println(decode1);
    }

    public Socket getConnection(Peer peer) throws Exception{
        Socket socket = new Socket(peer.getHost(),peer.getPort());
        return socket;
    }


    public void Dispatcher(PushTaskState pushTaskState,Encoder encoder,OutputStream os){
        if(pushTaskState.TaskType == Type.Map){
            ReportTaskState reportTaskState = new ReportTaskState();
            reportTaskState.type = Type.Map;
            reportTaskState.TaskName = pushTaskState.getTaskName();
            reportTaskState.FilesName = map(pushTaskState);
            call("WorkReport",reportTaskState,Master.class,encoder,os);
        }else if(pushTaskState.TaskType == Type.Reduce){
            if(reduce(pushTaskState)){
                ReportTaskState reportTaskState = new ReportTaskState();
                reportTaskState.type = Type.Reduce;
                reportTaskState.TaskName = pushTaskState.getTaskName();
                reportTaskState.FilesName = new String[]{"ans.txt"};
                call("WorkReport",reportTaskState,Master.class,encoder,os);
            }
        }
    }

    public String[] map(PushTaskState pushTaskState){
        System.out.println("map 阶段完成");
        return new String[]{"tmp.txt"};
    }

    public boolean reduce(PushTaskState pushTaskState){
        //到时文件去数仓拿
        System.out.println("reduce 阶段完成");
        return true;
    }

    public static void main(String[] args) throws Exception {
        Work work = new Work();
        work.doWork();
    }

    //send an RPC request to master,wait for the response
    public boolean call(String rpcName,Object args,Class<?> clazz,Encoder encoder,OutputStream os){
        System.out.println("enter call");
        RequestArgs request = new RequestArgs();
        ServiceDescriptor service = new ServiceDescriptor();
        service.setMethod(rpcName);
        service.setClazz(clazz.getName());
        request.setService(service);
        request.setParameters(args);
        if(rpcName.equals("PushTask")){
            service.setRequestClass(RequestArgs.class);
        }
        else{
            service.setRequestClass(ReportTaskState.class);
        }

        System.out.println("构造请求" + request);

        try {
            byte[] encode = encoder.encode(request);
            os.write(encode);
            os.flush();
            System.out.println("写入成功");
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return false;
        }
    }
}
