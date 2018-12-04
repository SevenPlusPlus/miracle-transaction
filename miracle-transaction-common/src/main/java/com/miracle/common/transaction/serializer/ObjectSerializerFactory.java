package com.miracle.common.transaction.serializer;

import com.miracle.common.transaction.api.SerializeType;

public class ObjectSerializerFactory {
	public static ObjectSerializer createSerializerByName(String name)
	{
		ObjectSerializer retSerializer;
		SerializeType type = SerializeType.fromString(name);
		switch(type)
		{
		case JDK:
			retSerializer = new JdkSerializer();
			break;
		case FASTJSON:
			retSerializer = new FastjsonSerializer();
			break;
		case HESSIAN:
			retSerializer = new HessianSerializer();
			break;
		case PROTOSTUFF:
			retSerializer = new ProtostuffSerializer();
			break;
		default:
			retSerializer = new ProtostuffSerializer();
			break;
		}
		return retSerializer;
	}
}
