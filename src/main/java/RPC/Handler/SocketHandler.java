package RPC.Handler;

import RPC.Decode.Decoder;
import RPC.Decode.Impl.JSONDecoder;
import RPC.Encode.Encoder;
import RPC.Encode.Impl.JSONEncoder;
import RPC.RpcManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import utils.Request.ReportTaskState;
import utils.Request.RequestArgs;
import utils.Request.ServiceDescriptor;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

public class SocketHandler implements Runnable{
    public Socket socket;

    public SocketHandler(Socket socket){
        this.socket = socket;
        InputStream is = null;
        OutputStream os = null;
        try {
            //1.拿到写入流
            is = socket.getInputStream();
            //2.构造解码器
            Decoder decoder = new JSONDecoder();
            //3.读取写入流的内容
            byte[] bytes = new byte[1024];
            is.read(bytes);
            //4.得到请求参数
            RequestArgs args = decoder.decode(bytes, RequestArgs.class);
            ServiceDescriptor service = args.getService();
            Class requestClass = service.getRequestClass();
            String methodName = service.getMethod();
            Method method = RpcManager.servicesMethod.get(methodName);
            Class<?> targetClass = RpcManager.methodClazz.get(method);
            Object invoke = null;
            //这里可以优化为HashMap存储返回的类型
            if(methodName.equals("PushTask")){
                //解析参数
                Object parameters = args.getParameters();//是个JSONObj
                Object requestArgs = JSON.parseObject(JSON.toJSONString(parameters), requestClass);
                invoke = method.invoke(targetClass.newInstance(), requestArgs);
            }
            else{
                Object parameters = args.getParameters();//是个JSONObj
                Object requestArgs = JSON.parseObject(JSON.toJSONString(parameters), requestClass);
                invoke = method.invoke(targetClass.newInstance(), requestArgs);
            }
            //5.将invoke对象写出
            Encoder encoder = new JSONEncoder();
            byte[] response = encoder.encode(invoke);
            os = socket.getOutputStream();
            os.write(response);
            os.flush();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (is != null)
                    is.close();
                if( os != null)
                    os.close();
            }catch (Exception ee){
                ee.printStackTrace();
            }
        }
    }

    @Override
    public void run() {

    }
}
