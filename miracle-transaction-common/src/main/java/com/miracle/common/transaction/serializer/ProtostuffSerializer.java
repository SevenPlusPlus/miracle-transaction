package com.miracle.common.transaction.serializer;

import com.miracle.common.transaction.exception.TccException;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;


@Slf4j
public class ProtostuffSerializer implements ObjectSerializer{

	private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();

	private static Objenesis objenesis = new ObjenesisStd(true);
	    
    private static final ThreadLocal<LinkedBuffer> localBuffer = 
    		new ThreadLocal<LinkedBuffer>() {
		    	 @Override protected LinkedBuffer initialValue() {    
		             return LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);   
		         }    
    		};
    		
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Schema getSchema(Class cls) {
        Schema schema = (Schema) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }
	    		
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public byte[] serialize(Object obj) throws TccException {
		Class cls = obj.getClass();
        LinkedBuffer buffer = localBuffer.get();
        buffer.clear();
        try {
            Schema schema = getSchema(cls);
            return ProtobufIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
        	log.error("Protostuff serialize message failed.", e);
            throw new TccException(e.getMessage(), e);
        } 
	}

	@Override
	public <T> T deSerialize(byte[] param, Class<T> clazz) throws TccException {
		try {
            T message = (T) objenesis.newInstance(clazz);
            @SuppressWarnings("unchecked")
			Schema<T> schema = getSchema(clazz);
            ProtobufIOUtil.mergeFrom(param, message, schema);
            return message;
        } catch (Exception e) {
        	log.error("Protostuff deserialize message failed.", e);
            throw new TccException(e.getMessage(), e);
        }
	}

}
