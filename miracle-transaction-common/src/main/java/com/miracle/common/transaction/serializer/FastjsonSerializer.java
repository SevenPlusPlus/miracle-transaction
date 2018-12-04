package com.miracle.common.transaction.serializer;

import com.miracle.common.transaction.exception.TccException;
import lombok.extern.slf4j.Slf4j;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

@Slf4j
public class FastjsonSerializer implements ObjectSerializer{

	@Override
	public byte[] serialize(Object obj) throws TccException {
		byte[] buf = JSON.toJSONBytes(obj, SerializerFeature.WriteClassName);
    	return buf;
	}

	@Override
	public <T> T deSerialize(byte[] param, Class<T> clazz) throws TccException {
		try{
    		@SuppressWarnings("unchecked")
			T message = (T) JSON.parse(param);
    		return message;
    	} catch (Exception e) {
    		log.error("Fastjson deserialize message failed.", e);
            throw new TccException(e.getMessage(), e);
    	}
	}

}
