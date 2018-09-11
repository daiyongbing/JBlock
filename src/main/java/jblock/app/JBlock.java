package jblock.app;

import akka.actor.ActorSystem;
import akka.actor.Address;
import jblock.app.system.ClusterSystem;

import java.util.*;

public class JBlock {
    public static void main(String[] args){
        //创建系统实例
        ClusterSystem sys1 = new ClusterSystem("1",ClusterSystem.InitType.MULTI_INIT,true);
        sys1.init();//初始化（参数和配置信息）
        Address joinAddress = sys1.getClusterAddr();//获取组网地址
        sys1.joinCluster(joinAddress);//加入网络
        sys1.enableWS();//开启API接口
        sys1.start();//启动系统

        ActorSystem cluster = sys1.getActorSys();//获取内部系统SystemActor实例

        int node_min = 5;
        //如果node_max>node_min 将启动node反复离网和入网的仿真，但是由于system离网后无法复用并重新加入
        //运行一定时间会内存溢出
        int node_max = 5;
        boolean node_add = true;

        Set<ClusterSystem> nodes = new HashSet<ClusterSystem>();
        nodes.add(sys1); //入网

        Set<ClusterSystem> nodes_off = new HashSet<ClusterSystem>();

        for(int i =2; i <= node_max; i++) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int len = nodes.size();
            ClusterSystem sys = new ClusterSystem(String.valueOf(i),ClusterSystem.InitType.MULTI_INIT,true);
            sys.init();
            sys.joinCluster(joinAddress);
            sys.start();
            nodes.add(sys);
        }

        //node数量在最大和最小值之间振荡,仿真node入网和离网
        //离网的system似乎无法复用,只能重新新建实例
        if(node_max > node_min){
            node_add = false;
            for(int i=1; i<=1000; i++) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int len = nodes.size();
                if(len >= node_max){
                    node_add=false;
                }else if(len <= node_min){
                    node_add=true;
                }
                if(!node_add){
                    //Set不能直接取指定位置的元素，先将其转换为List后再取
                    List<ClusterSystem> list = new ArrayList<ClusterSystem>(nodes);
                    ClusterSystem nd_system = list.get(nodes.size()-1);
                    nd_system.leaveCluster(cluster);
                    try{
                        //Await.ready(nd_system.terminate(), Duration.Inf)
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    //Await.ready(nd_system.whenTerminated, 30.seconds)
                    //nd_system.terminate();
                    //nd_system.shutdown()
                    nodes.remove(nd_system);
                    //nodes_off += nd_system
                } else{
                    //避免systemName重复
                    ClusterSystem sys = new ClusterSystem(String.valueOf(i),ClusterSystem.InitType.MULTI_INIT,true);
                    sys1.init();
                    joinAddress = sys1.getClusterAddr();
                    sys1.joinCluster(joinAddress);
                    sys1.start();
                    nodes.add(sys);
                    //nodes_off -= nd_system
                }
            }
        }
    }
}
