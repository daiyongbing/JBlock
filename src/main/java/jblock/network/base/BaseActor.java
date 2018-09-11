package jblock.network.base;

import akka.actor.AbstractActor;

import akka.actor.Scheduler;

public abstract class BaseActor extends AbstractActor {
    String selfAddr = akka.serialization.Serialization.serializedActorPath(self());
    akka.actor.Cancellable schedulerLink = null;
    Scheduler scheduler = getContext().system().scheduler();

    /**
     * 清除定时器
     *
     * @return
     */
    public Boolean clearSched() {
        if (schedulerLink != null){
            return schedulerLink.cancel();
        } else return null;
    }
}
