package jblock.network.tools.register;

import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.Map;

public class ActorRegister {
    private Map actorList = new HashMap<Integer, ActorRef>();

    public void register(int actorName, ActorRef actorRef){
        actorList.put(actorName,actorRef);
    }

    public ActorRef getActorRef(int actorName){
        return (ActorRef)actorList.get(actorName);
    }

    public void unregister(int actorName){
        actorList.remove(actorName);
    }
}
