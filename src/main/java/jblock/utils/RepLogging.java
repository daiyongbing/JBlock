package jblock.utils;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public interface RepLogging {
    public void logMsg(int lOG_TYPE , String moduleName, String msg, String cluster_addr);
}
