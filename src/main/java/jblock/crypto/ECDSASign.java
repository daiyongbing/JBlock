package jblock.crypto;

import akka.protobuf.ByteString;
import jblock.app.conf.SystemProfile;
import jblock.storage.ImpDataAccess;
import jblock.utils.SerializeUtils;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;

/**
 *
 */
public class ECDSASign implements SignFunc{
    //store itsself key and certification
    static Map<String, String> keyStorePath = new HashMap<>();
    static Map<String, String> password = new HashMap<>();

    //store the trust list of other nodes' certification
    static String trustKeyStorePath = "";
    static String passwordTrust = "";

    static Map<String, KeyStore> keyStore = new HashMap<>();
    static KeyStore trustKeyStore;
    {
        try {
            trustKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    static Map<String, Certificate> trustkeysPubAddrMap = new HashMap<>();

    public void apply(String alias, String jksPath, String psw, String jksTrustPath, String passwordTrust){
        keyStorePath.put(alias,jksPath);
        password.put(alias, psw);

        if (trustKeyStorePath == "") {
            trustKeyStorePath = jksTrustPath;
            this.passwordTrust = passwordTrust;
        }
    }

    /**
     * 通过参数获取相关的密钥对、证书（动态加载）
     *
     * @param jks_file
     * @param password
     * @param alias
     * @return
     */
    public static Map<PrivateKey, PublicKey> getKeyPairFromJKS (File jks_file, String password, String alias){
        Map<PrivateKey, PublicKey> keyMap = new HashMap<>();
        KeyStore store = null;
        FileInputStream fis = null;
        Key sk = null;
        Certificate cert = null;
        try {
            store = KeyStore.getInstance(KeyStore.getDefaultType());
            fis = new FileInputStream(jks_file);
            char[] pwd = password.toCharArray();
            store.load(fis, pwd);
            sk = store.getKey(alias, pwd);
            cert = store.getCertificate(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        if (sk.getClass().isInstance(PrivateKey.class)){
            keyMap.put((PrivateKey)sk, cert.getPublicKey()); //强转可能会出错，暂时只能先这样做判断
        }
        return keyMap;
    }

    /**
     * 在信任列表中获取证书（通过alias）
     *
     * @param cert
     * @return
     */
    public static String getAliasByCert (Certificate cert){
        String alias = null;
        try {
            alias = trustKeyStore.getCertificateAlias(cert);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return alias;
    }

    /**
     * 获取证书的Base58地址
     * @param cert
     * @return
     */
    public static String getAddrByCert (Certificate cert){
        return Base58.encode(Sha256.hash(cert.getPublicKey().getEncoded()));
    }

    /**
     * 获取证书的短地址（Bitcoin方法）
     * @param cert 对象
     * @return
     */
    public static String getBitcoinAddrByCert (Certificate cert) {
        return BitcoinUtils.calculateBitcoinAddress(cert.getPublicKey().getEncoded());
    }

    /**
     * 获取证书的短地址
     * @param certByte 字节
     * @return
     */
    public static String getBitcoinAddrByCert (byte[] certByte){
        Certificate cert = (Certificate)SerializeUtils.deserialise(certByte);
        return BitcoinUtils.calculateBitcoinAddress(cert.getPublicKey().getEncoded());
    }

    /**
     * 获取指定alias的证书短地址
     * @param alias
     * @return
     */
    public static String getAddr (String alias) {
        //通过alias获取Certficate
        Certificate certificate = null;
        try {
            certificate = keyStore.get(alias).getCertificate(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return getAddrByCert(certificate);
    }

    /**
     * 根据短地址获取证书
     * @param addr
     * @return
     */
    public static Certificate getCertByBitcoinAddr (String addr) {
        Certificate tmpcert = trustkeysPubAddrMap.get(addr);
        if (tmpcert == null) {
            throw new RuntimeException("证书不存在");
        }
        if (checkCertificate(new Date(), tmpcert)) {
            return tmpcert;
        } else {
            throw new RuntimeException("证书已经过期");
        }
    }

    /**
     * 通过配置信息获取证书（动态加载）
     *
     * @param jks_file
     * @param password
     * @param alias
     * @return
     */
    public static Certificate getCertFromJKS (File jks_file, String password, String alias){
        KeyStore store = null;
        FileInputStream fis = null;
        Key sk = null;
        Certificate cert = null;
        try {
            store = KeyStore.getInstance(KeyStore.getDefaultType());
            fis = new FileInputStream(jks_file);
            char[] pwd = password.toCharArray();
            store.load(fis, pwd);
           sk = store.getKey(alias, pwd);
           cert = store.getCertificate(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return cert;
    }

    /**
     * 读取文件
     * @ author daiyongbing
     * @param fileName
     * @return
     */
    public static String readFromFileToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将pem格式证书字符串转换为certificate
     * @param pemcert
     * @return
     */
    public static Certificate getCertByPem (String pemcert) {
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        Certificate cert = null;
        try {
            cert = cf.generateCertificate(
            new ByteArrayInputStream(pemcert.getBytes()));
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return cert;
    }


    /**
     * 获取alias的密钥对和证书（系统初始化）
     * java没有元组类型，使用Map,key:KeyPair,value:SerializeUtils.serialise(cert)
     * 必须重写
     * @param alias
     * @return
     */
    public static Map<Map<PrivateKey, PublicKey>, byte[]> getKeyPair (String alias){
        Map<Map<PrivateKey, PublicKey>, byte[]> keyPairMap = new HashMap<>();
        Map<PrivateKey, PublicKey> keyPair = new HashMap<>();
        Key key = null;
        try {
            key = keyStore.get(alias).getKey(alias, password.get(alias).toCharArray());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        Certificate cert = null;
        try {
            cert = keyStore.get(alias).getCertificate(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        if (checkCertificate(new java.util.Date(), cert)) {
            //(sk.asInstanceOf[PrivateKey], cert.getPublicKey(), SerializeUtils.serialise(cert))
            keyPair.put((PrivateKey) key, cert.getPublicKey());
            keyPairMap.put(keyPair, SerializeUtils.serialise(cert));
            return keyPairMap;
        } else {
            throw new RuntimeException("证书已经过期");
        }
    }

    /**
     * 获取alias的证书（系统初始化）
     *
     * @param alias
     * @return
     */
    public Certificate getCert (String alias){
        Certificate certificate = null;
        try {
            certificate =  keyStore.get(alias).getCertificate(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return certificate;
    }

    /**
     * 在信任列表中获取alias的证书（系统初始化）
     *
     * @param alias
     * @return
     */
    public static PublicKey getKeyPairTrust (String alias) {
        Key sk = null;
        Certificate cert = null;
        try {
            sk = trustKeyStore.getKey(alias, passwordTrust.toCharArray());
            cert = trustKeyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return cert.getPublicKey();
    }

    /**
     * 判断两个证书是否相同
     *
     * @param alias
     * @param cert
     * @return
     */
    public static Boolean isCertTrust (String alias, byte[] cert) {
        Boolean isEqual = false;
        Key sk = null;
        Certificate certT;
            //寻找方法能够恢复cert？
        try {
            sk = trustKeyStore.getKey(alias, passwordTrust.toCharArray());
            certT = trustKeyStore.getCertificate(alias);
            isEqual = certT.getEncoded().equals(cert);
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return isEqual;
    }

    /**
     * 预加载系统密钥对和信任证书
     *
     * @param alias
     */
    public static void preLoadKey (String alias) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(keyStorePath.get(alias)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        char[] pwd = password.get(alias).toCharArray();
        if (keyStore.containsKey(alias)) {
            try {
                keyStore.get(alias).load(fis, pwd);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            }
        }
        else {
            KeyStore keyS = null;
            try {
                keyS = KeyStore.getInstance(KeyStore.getDefaultType());
                keyS.load(fis, pwd);
                keyStore.put(alias, keyS);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileInputStream fisT = null;
        try {
            fisT = new FileInputStream(new File(trustKeyStorePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        char[] pwdT = passwordTrust.toCharArray();
        try {
            trustKeyStore.load(fisT, pwdT);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        loadTrustkeysPubAddrMap();
    }

    /**
     * 初始化信任证书中对短地址和证书的映射
     */
    public static void loadTrustkeysPubAddrMap() {
        Enumeration<String> enums = null;
        try {
            enums = trustKeyStore.aliases();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        while (enums.hasMoreElements()) {
            String alias = enums.nextElement();
            Certificate cert = null;
            try {
                cert = trustKeyStore.getCertificate(alias);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            trustkeysPubAddrMap.put(getBitcoinAddrByCert(cert), cert);
        }
    }

    /**
     * 获取本地证书，得到证书类和其序列化的字节序列
     *
     * @param certPath
     * @return 字节序列（通过base58进行转化）
     */
    public static Map<Map<Certificate, byte[]>, String> loadCertByPath (String certPath){
        Map<Map<Certificate, byte[]>, String> map = new HashMap<>();
        CertificateFactory certF = null;
        FileInputStream fileInputStream = null;
        X509Certificate x509Cert = null;
        try {
            certF = CertificateFactory.getInstance("X.509");
            fileInputStream = new FileInputStream(certPath);
            x509Cert = (X509Certificate)certF.generateCertificate(fileInputStream);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] arrayCert = SerializeUtils.serialise(x509Cert);
        Map<Certificate, byte[]> hashMap = new HashMap<>();
        hashMap.put(x509Cert, arrayCert);
        map.put(hashMap, Base64.getEncoder().encodeToString(arrayCert));
        return map;
    }

    /**
     * 添加证书到信任列表
     *
     * @param cert 字节数组
     * @param alias
     * @return
     */
    public Boolean loadTrustedCert (byte[] cert, String alias) {
        Certificate certTx = (Certificate)SerializeUtils.deserialise(cert);
        if (getAliasByCert(certTx) == null){
            try {
                trustKeyStore.setCertificateEntry(alias, certTx);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            trustkeysPubAddrMap.put(getBitcoinAddrByCert(certTx), certTx);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(trustKeyStorePath);
                trustKeyStore.store(fileOutputStream, passwordTrust.toCharArray());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }else {
            return false;
        }
    }

    /**
     * 添加证书到信任列表
     *
     * @param cert base64字符串
     * @param alias
     * @return
     */
    public static Boolean loadTrustedCertBase64 (String cert, String alias) {
        Certificate certTx = (Certificate)SerializeUtils.deserialise(Base64.getDecoder().decode(cert));
        FileOutputStream fileOutputStream = null;
        Boolean flag = false;
        if (getAliasByCert(certTx) == null){
            try {
                trustKeyStore.setCertificateEntry(alias, certTx);
                trustkeysPubAddrMap.put(getBitcoinAddrByCert(certTx), certTx);
                fileOutputStream = new FileOutputStream(trustKeyStorePath);
                trustKeyStore.store(fileOutputStream, passwordTrust.toCharArray());
                return true;
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            flag = true;
        }else {
            flag = false;
        }
        return flag;
    }

    public static Certificate getCertByNodeAddr (String addr) {
        Certificate certificate = null;
        if (addr != null) {
            certificate = trustkeysPubAddrMap.get(addr);
        }
        return certificate;
    }


/********************************Override - SignFunc ******************************/
    /**
     * 签名
     * @param privateKey 私钥
     * @param message 带签名信息
     * @return bytes
     */
    @Override
    public byte[] sign(PrivateKey privateKey, byte[] message) {
        Signature s1;
        byte[] bytes = null;
        try {
            s1 = Signature.getInstance("SHA1withECDSA");
            s1.initSign(privateKey);
            s1.update(message);
            bytes = s1.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * 验证
     * @param signature
     * @param message
     * @param publicKey
     * @return
     */
    @Override
    public Boolean verify(byte[] signature, byte[] message, PublicKey publicKey) {
        Signature s2;
        Boolean isVerify = false;
        try {
            s2 = Signature.getInstance("SHA1withECDSA");
            s2.initVerify(publicKey);
            s2.update(message);
            isVerify = s2.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return isVerify;
    }

    @Override
    public Certificate getCertWithCheck(String certAddr, String certKey, String sysTag) {
        Certificate cert = ECDSASign.getCertByNodeAddr(certAddr);
        if(cert != null) {
            if(checkCertificate(new Date(),  cert)){
                return cert;
            }else{
                throw new RuntimeException("证书已经过期");
            }
        }else{
            if(certKey == null || sysTag == null){
                throw new RuntimeException("没有证书");
            }else{
                try{
                    ImpDataAccess sr = ImpDataAccess.GetDataAccess(sysTag);
                    cert = sr.Get(certKey);
                    if (cert != null){
                        if (new String(cert.toString()) == "null") {
                            throw new RuntimeException("用户证书已经注销");
                        }else{
                            Certificate kvcert = (Certificate)SerializeUtils.deserialise(cert.getEncoded());
                            if(kvcert != null){
                                if(checkCertificate(new java.util.Date(), kvcert)){
                                    return kvcert;
                                }else{
                                    throw new RuntimeException("证书已经过期");
                                }
                            }else{
                                throw new RuntimeException("证书内容错误");
                            }
                        }
                    }else{
                        throw new RuntimeException("没有证书");
                    }
                }catch(Exception e){
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
    }

    public static Boolean checkCertificate(Date date, Certificate cert){
        Boolean isValid = false;
        Long start = System.currentTimeMillis();
        try {
            if(cert == null){
                isValid = false;
            }else{
                if(SystemProfile.getCheckCertValidate() == 0){
                    isValid = true;
                }else if(SystemProfile.getCheckCertValidate() == 1){
                    if (cert.getClass().isInstance(X509Certificate.class)) {
                        //X509Certificate x509cert = cert.asInstanceOf[X509Certificate]
                        // 利用向下转型，实际上cert.getClass().newInstance()是Certificate，而X509Certificate是Certificate的子类
                        X509Certificate x509Certificate = (X509Certificate)cert.getClass().newInstance();
                        x509Certificate.checkValidity(date);
                        isValid = true;
                    }
                }else{
                    isValid = true;
                }
            }
        } catch (IllegalAccessException e) {
            isValid = false;
            e.printStackTrace();
        } catch (InstantiationException e) {
            isValid = false;
            e.printStackTrace();
        } catch (CertificateNotYetValidException e) {
            isValid = false;
            e.printStackTrace();
        } catch (CertificateExpiredException e) {
            isValid = false;
            e.printStackTrace();
        }
        /*Long end = System.currentTimeMillis();
        System.out.println("check cert validate,spent time="+(end-start));*/
         return isValid;
    }

    public static void main(String[] args) {
        System.out.println(ByteString.copyFromUtf8(ECDSASign.getBitcoinAddrByCert(ECDSASign.getCertFromJKS(new File("jks/mykeystore_1.jks"), "123", "1"))).toStringUtf8());
        System.out.println(ECDSASign.getBitcoinAddrByCert(ECDSASign.getCertFromJKS(new File("jks/mykeystore_2.jks"), "123", "2")));
        System.out.println(ECDSASign.getBitcoinAddrByCert(ECDSASign.getCertFromJKS(new File("jks/mykeystore_3.jks"), "123", "3")));
        System.out.println(ECDSASign.getBitcoinAddrByCert(ECDSASign.getCertFromJKS(new File("jks/mykeystore_4.jks"), "123", "4")));
        System.out.println(ECDSASign.getBitcoinAddrByCert(ECDSASign.getCertFromJKS(new File("jks/mykeystore_5.jks"), "123", "5")));
    }
}
