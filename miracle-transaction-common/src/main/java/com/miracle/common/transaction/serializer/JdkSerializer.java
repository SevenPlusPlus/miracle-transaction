package com.miracle.common.transaction.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import com.miracle.common.transaction.exception.TccException;

public class JdkSerializer implements ObjectSerializer{

	@Override
    public byte[] serialize(Object obj) throws TccException {
        try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()) {
            ObjectOutput objectOutput = new ObjectOutputStream(arrayOutputStream);
            objectOutput.writeObject(obj);
            objectOutput.flush();
            objectOutput.close();
            return arrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new TccException("JAVA serialize error " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T deSerialize(byte[] param, Class<T> clazz) throws TccException {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(param);
        try {
            ObjectInput input = new ObjectInputStream(arrayInputStream);
            return (T) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new TccException("JAVA deSerialize error " + e.getMessage());
        }
    }
}
