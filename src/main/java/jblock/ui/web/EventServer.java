package jblock.ui.web;

import akka.actor.AbstractActor;
import jblock.app.conf.SystemProfile;
import jblock.protos.Peer.Event;

public class EventServer extends AbstractActor {
    @Override
    public void preStart(){
        //EventServer.start(getContext().system(), SystemProfile.getHttpServicePort());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Event.class, message -> {
                    // ignore
                })
                .build();
    }
}
