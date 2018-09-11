package jblock.network.base;

import akka.actor.ActorRef;
import jblock.app.system.ClusterSystem;
import jblock.network.tools.register.ActorRegister;

public class ModuleBase {
    // object ModuleBase
    public static void registerActorRef(String sysTag, Integer actorType, ActorRef actorRef) {
        ClusterSystem clusterSystem = ClusterSystem.getInstance();
        ActorRegister actorRegister = clusterSystem.getActorRegister(sysTag);
        if(actorRegister == null){
            ActorRegister nullActorRegister = new ActorRegister();
            nullActorRegister.register(actorType, actorRef);
            clusterSystem.register(sysTag, nullActorRegister);
        } else {
            actorRegister.register(actorType, actorRef);
        }
    }

}
