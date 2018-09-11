package jblock.network.module;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.typesafe.config.Config;
import jblock.network.base.BaseActor;
import jblock.network.persistence.PersistenceModule;
import jblock.utils.ActorUtils;
import jblock.utils.GlobalUtils;

import java.util.Map;

public class ModuleManager extends AbstractActor {
    private String moduleName;
    private String sysTag;

    public ModuleManager(String moduleName, String sysTag) {
        this.moduleName = moduleName;
        this.sysTag = sysTag;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getSysTag() {
        return sysTag;
    }

    public static Props props(String name , String sysTag){
        return Props.create(ModuleManager.class, name, sysTag);
    }

    private Config conf = getContext().system().settings().config();
    private ActorRef persistence  = null;
    private ActorRef sync = null;
    private ActorRef transactionPool = null;
    private ActorRef consensus = null;
    private ActorRef transCreator = null;

    private boolean isConsensusFinished = false;
    private boolean isClusterJoined = false;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(null, nu->{})
                .build();
    }

}
