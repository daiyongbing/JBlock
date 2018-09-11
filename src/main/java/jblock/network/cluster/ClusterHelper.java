package jblock.network.cluster;

import akka.actor.Address;
import jblock.protos.Peer;
import jblock.protos.Peer.BlockchainInfo;

import java.util.ArrayList;
import java.util.*;

public class ClusterHelper {
    /**
     * 是否是当前候选人
     * @param Systemname
     * @param candidates
     * @return
     */

    public Boolean isCandidateNow(String Systemname, Set<String> candidates) {
        List list = new ArrayList();
        for (String e : candidates){
            list.add(e);
        }
        return list.contains(Systemname);
    }

    /**
     * 是否是当前出块人
     * @param systemtag
     * @param blockername
     * @return
     */
    public Boolean isBlocker(String systemtag, String blockername) {
        if(systemtag == blockername){
            return true;
        }else{
            return false;
        }
    }

    public Boolean checkBlocker(String myaddress,String sendaddress) {
        Boolean b = false;
        if(myaddress.indexOf("/user")>0){
            String addr = myaddress.substring(0, myaddress.indexOf("/user"));
            b = sendaddress.indexOf(addr) != -1;
        }
        return b;
    }

    /**
     * System唯一性key
     *
     * @param ip
     * @param port
     * @return
     */
    public String getNodeSystemUniqueId(String ip, String port) {
        return ip + ":" + port;
    }

    /**
     * 获得多数一致性节点地址
     * 地址：对象
     * @param nodes
     * @param clusterInfo
     * @return
     */
    public Set<Address> filtWithMajorStatusForClusterNodes(Set<Address> nodes, Map<String, BlockchainInfo> clusterInfo){
        final Set<String> usableNode = getMajorNodes(clusterInfo);
        System.out.println("Filted size :" + usableNode.size());

       /* nodes.filter(node => {
                val nodepath = node.toString
                usableNode.contains(nodepath.substring(nodepath.indexOf("@") + 1))
        })*/
       nodes.removeIf(node -> usableNode.contains(node.toString().substring(node.toString().indexOf("@") + 1)));
       return nodes;
    }

    /**
     * 获得多数一致性节点的地址
     * 地址：Ip - port
     * @param clusterInfo
     * @return
     */
    public Set<String> getMajorNodes(Map<String, BlockchainInfo>  clusterInfo){
        Map<String, Integer> diffInfoMap = new HashMap<String, Integer>();
        int max = 0;
        String maxMerk = "";
        System.out.println("Total info size: " + clusterInfo.size());

        // 下面的scala代码怎么用Java替换？
        /*clusterInfo.foreach(info => {
                String id = info._2.currentWorldStateHash.toStringUtf8;
            if (diffInfoMap.contains(id)) {
                diffInfoMap.put(id, diffInfoMap.get(id).get + 1);
                if (max < diffInfoMap.get(id).get) {
                    max = diffInfoMap.get(id).get;
                    if (!maxMerk.equals(id)) maxMerk = id;
                }
            }
            else diffInfoMap.put(id, 1);
        });*/
        //用Java实现
        for(BlockchainInfo value:clusterInfo.values()){
            String id = value.getCurrentWorldStateHash().toStringUtf8();
            if (diffInfoMap.containsKey(id)) {
                diffInfoMap.put(id, diffInfoMap.get(id) + 1);
                if (max < diffInfoMap.get(id)) {
                    max = diffInfoMap.get(id);
                    if (!maxMerk.equals(id)) maxMerk = id;
                }
            } else diffInfoMap.put(id, 1);
        }
        final String fmaxMerk = maxMerk; //lambda表达式中不能使用变量，只能重新定义final类型
        clusterInfo.entrySet().removeIf(info -> info.getValue().getCurrentWorldStateHash().toStringUtf8().equals(fmaxMerk));
        return clusterInfo.keySet();
    }

    /**
     * 判断节点间chain状态是否相同
     * @param src
     * @param target
     * @return
     */
    public Boolean isSameChainStatus(BlockchainInfo src, BlockchainInfo target) {
        //TODO kami 其实currentB
        if (src.getCurrentWorldStateHash().toStringUtf8() == target.getCurrentWorldStateHash().toStringUtf8()){
            return true;
        } else{
            return false;
        }
    }

    /**
     * 判断是否是种子节点
     * 目前并不完善
     * @param sysName
     * @return
     */
    public Boolean isSeedNode(String sysName){
        return sysName == "1";
    }
}
