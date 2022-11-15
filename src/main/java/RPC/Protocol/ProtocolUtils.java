package RPC.Protocol;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolUtils {
    private static LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    public static <T> byte[] serialize(T obj){
        Class<T> clazz = (Class<T>) obj.getClass();

        Schema<T> schema = getSchema(clazz);

        byte[] data = null;
        try{
            data = ProtostuffIOUtil.toByteArray(obj,schema,buffer);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            buffer.clear();
        }

        return data;
    }

    public static <T> T deserialize(byte[] data,Class<T> clazz){
        Schema<T> schema = getSchema(clazz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data,obj,schema);
        return obj;
    }


    private static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (Objects.isNull(schema)) {
            //这个schema通过RuntimeSchema进行懒创建并缓存
            //所以可以一直调用RuntimeSchema.getSchema(),这个方法是线程安全的
            schema = RuntimeSchema.getSchema(clazz);
            if (Objects.nonNull(schema)) {
                schemaCache.put(clazz, schema);
            }
        }
        return schema;
    }
}
