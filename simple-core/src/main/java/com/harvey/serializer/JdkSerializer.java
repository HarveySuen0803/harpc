package com.harvey.serializer;

import java.io.*;

/**
 * @author Harvey Suen
 */
public class JdkSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}