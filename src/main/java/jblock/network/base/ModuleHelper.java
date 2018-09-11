package jblock.network.base;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import jblock.app.system.ClusterSystem;
import jblock.network.tools.PeerExtension;
import jblock.network.tools.PeerExtensionImpl;
import jblock.network.tools.register.ActorRegister;

import static jblock.app.system.ClusterSystem.getActorRegister;

public abstract class ModuleHelper extends AbstractActor {
    PeerExtensionImpl pe = new PeerExtension().get(getContext().system());

    /**
     * 从注册中心获取actor引用
     * @param sysTag
     * @param actorType
     * @return
     */
    public ActorRef getActorRef(String sysTag, int actorType){
        ActorRegister actorRegister = ClusterSystem.getActorRegister(sysTag);
        if(actorRegister != null){
            ActorRef actorRef = actorRegister.getActorRef(actorType);
            if(actorRef != null){
                return actorRef;
            }else {
                return self();
            }
        }else{
            return self();
        }
    }

    /**
     * 从注册中心获取actor引用
     * @param actorType
     * @return
     */
    public ActorRef getActorRef(int actorType){
        ActorRegister actorReg = getActorRegister(pe.getSysTag);
        if(actorReg != null){
            ActorRef actorRef = actorReg.getActorRef(actorType);
            if(actorRef != null){
                return actorRef;
            }else {
                return self();
            }
        }else {
            return self();
        }
    }

    /**
     * 向注册中心注册actor引用
     * @param sysTag
     * @param actorType
     * @param actorRef
     * @return
     */
    public void registerActorRef(String sysTag, int actorType, ActorRef actorRef){
        ActorRegister actorRegister = getActorRegister(sysTag);
        if( actorRegister!= null){
            ActorRegister actRegister = new ActorRegister();
            actRegister.register(actorType, actorRef);
            ClusterSystem.register(sysTag, actRegister);
        }else {
            actorRegister.register(actorType, actorRef);
        }
    }

    /**
     * 向注册中心注册actor引用
     * @param actorType
     * @param actorRef
     * @return
     */
    public void registerActorRef(int actorType, ActorRef actorRef ) {
        ActorRegister actorRegister = getActorRegister(pe.getSysTag);
        if(actorRegister == null){
            actorRegister.register(actorType, actorRef);
        }else {
            ActorRegister actReg = new ActorRegister();
            actReg.register(actorType, actorRef);
            ClusterSystem.register(pe.getSysTag, actReg);
        }
    }
}
