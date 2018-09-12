package jblock.crypto;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Sha256 {

    public static byte[] hash(byte[] input){
        byte[] bytes = null;
        try {
            bytes = MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    @Test
    public void pringbyte(){
        byte[] b1 = {1,2,3,4,5,6,7};
        for (byte b : hash(b1)) {
            System.out.println(b);
        }

    }
}
