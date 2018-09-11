package jblock.ipfs;

import io.ipfs.api.MerkleNode;

public class IPFSTest {
    public static void main(String args[]){
        String ipv4 = "/ip4/127.0.0.1/tcp/5001";
        IPFSUtils ipfsUtils = new IPFSUtils();

        String hash1 = "QmaBZ6MWnDbjxDUDJWLtbTzCJYsjLvZy2UvpfcmnH8m93n";
        String hash2 = "QmQyNJ6ChkwnVmJGkkWrVmopo9WUUApqS1FiopandtL9eB";

       byte[] bytes =  ipfsUtils.downloadFileBytes(ipv4, hash2);
       ipfsUtils.byte2File(bytes, "C:\\Users\\vic\\Desktop\\", "QmQyNJ6ChkwnVmJGkkWrVmopo9WUUApqS1FiopandtL9eB.jpg");

       byte[] fileBytes = ipfsUtils.File2byte("C:\\Users\\vic\\Desktop\\headPic.jpg");
        MerkleNode addResult = ipfsUtils.uploadFile2IPFS(ipv4, fileBytes);
        System.out.println(addResult.hash.toString());
    }
}
