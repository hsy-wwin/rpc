package utils.redis;

import RPC.proto.Peer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;

public class ServiceStoreType {
    public Peer peer;
    public String methodName;
    public String clazz;
    public Method method = null;
    public Class<?> aClass = null;

    public ServiceStoreType(){
        super();
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?> getaClass() {
        return aClass;
    }

    public void setaClass(Class<?> aClass) {
        this.aClass = aClass;
    }

    @Override
    public String toString() {
        return "ServiceStoreType{" +
                "peer=" + peer +
                ", methodName='" + methodName + '\'' +
                ", clazz='" + clazz + '\'' +
                ", method=" + method +
                ", aClass=" + aClass +
                '}';
    }
}
