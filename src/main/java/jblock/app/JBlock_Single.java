package jblock.app;

import akka.actor.Address;
import jblock.app.system.ClusterSystem;

public class JBlock_Single {
    public static void main(String[] args){
        String systemTag = "1";
        if(args!=null && args.length>0){
            systemTag = args[0];
        }
        ClusterSystem sys1 = new ClusterSystem(systemTag, ClusterSystem.InitType.SINGLE_INIT,true);
        sys1.init();
        Address joinAddress = sys1.getClusterAddr();
        sys1.joinCluster(joinAddress);
        sys1.start();
    }
}
