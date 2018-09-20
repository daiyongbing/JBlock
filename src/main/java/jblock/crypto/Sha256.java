package jblock.crypto;

import com.google.protobuf.ByteString;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Sha256 extends CryptographicHash{
    @Override
    public  byte[] hash(byte[] input){
        byte[] bytes = null;
        try {
            bytes = MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public String hashstr(byte[] input){
       return Byte2Hex.bytes2hex(hash(input));
    }

    public String hashstr(String input){
        ByteString iptb = ByteString.copyFromUtf8(input);
        return Byte2Hex.bytes2hex(hash(iptb.toByteArray()));
    }

    public byte[] hashToBytes(String input){
        ByteString iptb = ByteString.copyFromUtf8(input);
        return hash(iptb.toByteArray());
    }

    @Test
    public void pringbyte(){
        byte[] b1 = {1,2,3,4,5,6,7};
        for (byte b : hash(b1)) {
            System.out.println(b);
        }

    }
}
