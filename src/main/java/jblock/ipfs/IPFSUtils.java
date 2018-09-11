package jblock.ipfs;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

import java.io.*;

/**
 * IPFS的工具类
 * @author daiyongbing
 * @version 1.0
 * @since 2018-08-23
 */
public class IPFSUtils {
    /**
     * 文件上传
     * @param ipv4
     * @param filePath
     * @return 文件hash
     */
    public static String uploadIPFS(String ipv4, String filePath){
        IPFS ipfs = new IPFS(ipv4);
        try {
            ipfs.refs.local();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(new File(filePath));
        Multihash addResult = null;

        {
            try {
                addResult = ipfs.add(file).get(0).hash;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return addResult.toString();
    }

    /**
     * 文件下载
     * @param ipv4
     * @param contentsHash
     * @return fileBytes
     */
    public static byte[] downLoadIPFS(String ipv4, String contentsHash) {
        OutputStream os = null;
        IPFS ipfs = new IPFS(ipv4);
            Multihash filePointer =Multihash.fromBase58(contentsHash);

        byte[] fileBytes = null;
        try {
            fileBytes = ipfs.cat( filePointer );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileBytes;
    }

    /**
     * 从IPFS获取文件的byte[]
     * @param ipv4
     * @param fileHash
     * @return ContentsBytes
     */
    public  static byte[] downloadFileBytes(String ipv4, String fileHash){
        byte[] ContentsBytes = null;
        try {
            IPFS ipfs = new IPFS(ipv4);
            ipfs.refs.local();
            Multihash filePointer = Multihash.fromBase58(fileHash);
            ContentsBytes = ipfs.cat(filePointer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ContentsBytes;
    }

    /**
     * 上传文件到IPFS
     * @param ipv4
     * @param byteContents
     * @return MerkleNode
     */
    public static MerkleNode uploadFile2IPFS(String ipv4, byte[] byteContents){
        IPFS ipfs = new IPFS(ipv4);
        MerkleNode addResult = null;
        try {
            ipfs.refs.local();
            NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(byteContents);
            addResult = ipfs.add(file).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addResult;
    }

    public static void byte2File(byte[] buf, String filePath, String fileName)
    {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try
        {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory())
            {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bos != null)
            {
                try
                {
                    bos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] File2byte(String filePath)
    {
        byte[] buffer = null;
        try
        {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }
}
