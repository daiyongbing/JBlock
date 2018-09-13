package jblock.storage;

import java.security.cert.Certificate;

public class ImpDataAccess {
    private String sysTag;
    private ImpDataAccess(String sysTag){
        this.sysTag = sysTag;
    }
    public static ImpDataAccess GetDataAccess(String sysTag) {
        return new ImpDataAccess(sysTag);
    }

    public Certificate Get(String certKey) {
        return null;
    }
}
