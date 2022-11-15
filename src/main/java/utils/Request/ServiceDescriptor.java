package utils.Request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Arrays;

/***
 * 请求的服务的描述信息
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDescriptor {
    private Class requestClass;
    private String clazz;//类
    private String method;//方法
    private String returnType;//返回方式
    private String[] parameterTypes;//返回参数

    public static ServiceDescriptor from(Class clazz, Method method) {
        ServiceDescriptor sd = new ServiceDescriptor();
        sd.setClazz(clazz.getName());
        sd.setMethod(method.getName());
        sd.setReturnType(method.getReturnType().getName());
        Class[] parameterClass = method.getParameterTypes();
        String[] parameterArr = new String[parameterClass.length];
        for (int i = 0; i < parameterClass.length; i++) {
            parameterArr[i] = parameterClass[i].getName();
        }
        sd.setParameterTypes(parameterArr);
        return sd;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (null == obj || getClass() != obj.getClass()) {
            return false;
        } else {
            ServiceDescriptor that = (ServiceDescriptor) obj;
            return this.toString().equals(that.toString());
        }
    }

    @Override
    public String toString() {
        return "clazz=" + clazz + ",method=" + method + ",returnType=" + returnType + ",parameterTypes=" + Arrays.toString(parameterTypes);
    }
}
