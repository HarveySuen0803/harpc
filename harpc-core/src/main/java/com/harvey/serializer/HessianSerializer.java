package com.harvey.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Harvey Suen
 */
public class HessianSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HessianOutput ho = new HessianOutput(bos);
            ho.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            HessianInput hi = new HessianInput(bis);
            return (T) hi.readObject(clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
