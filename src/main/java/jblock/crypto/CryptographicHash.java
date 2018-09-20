package jblock.crypto;

public abstract class CryptographicHash {

    public byte[] apply(byte[] input){
        return hash(input);
    }

    public byte[] apply(String input ){
        return hash(input.getBytes());
    }

    public abstract byte[] hash(byte[] input);

    public byte[] hash(String input){
        return hash(input.getBytes());
    }
}
