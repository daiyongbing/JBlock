package jblock.utils;

import java.io.*;

public class SerializeUtils {
    /**
     * Java 序列化方法
     * @param value
     * @return
     */
    public static byte[] serialise(Object value) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(stream);
            oos.writeObject(value);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream.toByteArray();
    }

    /**
     * Java 反序列化方法
     * @param bytes
     * @return
     */
    public static Object deserialise(byte[] bytes){
        ObjectInputStream ois = null;
        Object value = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            value = ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return value;
    }
}
