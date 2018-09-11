package jblock.utils;

import java.util.HashMap;
import java.util.Map;

public class ActorUtils {
    public static Map<String, String> getIpAndPort(String clusterPath){
        //java中没有元祖类型，不能返回多个值，这里使用map来实现,key:ip,value:port
        Map<String, String> ipAndPort = new HashMap<String, String>();
        String str = clusterPath.substring(clusterPath.indexOf("@")+1);
        str = str.substring(0,str.indexOf("/"));
        String[] re = str.split(":");
        ipAndPort.put(re[0], re[1]);
        return ipAndPort;
    }

    public Boolean isHelper(String path) {
        return path.contains("helper");
    }

    public Boolean isAPI(String path) {
        return path.contains("api");
    }
}
