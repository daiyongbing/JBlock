package jblock.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public interface SignFunc {
    byte[] sign(PrivateKey privateKey, byte[] message);
    Boolean verify(byte[] signature, byte[] message, PublicKey publicKey);
    Certificate getCertWithCheck(String certAddr, String certKey, String sysTag);
}
