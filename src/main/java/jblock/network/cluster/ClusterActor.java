package jblock.network.cluster;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import jblock.network.PeerHelper.Topic;
import jblock.protos.Peer.Event;
import jblock.utils.ActorUtils;
import jblock.utils.GlobalUtils.EventType;

public class ClusterActor extends AbstractActor {
    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    /**
     * 根据全网节点的地址（带IP）判断是否属于同一个System
     *
     * @param src
     * @param tar
     * @return
     */
    public Boolean isThisAddr(String src, String tar){
        return src.startsWith(tar);
    }

    /**
     * 广播Event消息
     *
     * @param eventType 发送、接受
     * @param mediator
     * @param addr
     * @param topic
     * @param action
     */
    public void sendEvent(int eventType, ActorRef mediator, String addr, String topic, Event.Action action) {
        switch (eventType){
            case EventType.PUBLISH_INFO:
                {
                    //Event evt = new Event(addr, topic, action);   //这里报错先注释，用下面暂时替代
                    mediator.tell(new DistributedPubSubMediator.Publish(Topic.Event, getSelf()), getSelf());
                }
                break;
            case EventType.RECEIVE_INFO:
                {
                    //Event evt = new Event(topic, addr, action);   //这里报错先注释，用下面暂时替代
                    mediator.tell(new DistributedPubSubMediator.Publish(Topic.Event, getSelf()), getSelf());
                }
                break;
            default:break;
        }
    }


    /**
     * 广播SyncEvent消息
     *
     * @param eventType 发送、接受
     * @param mediator
     * @param fromAddr
     * @param toAddr
     * @param action
     */
    public void sendEventSync(int eventType, ActorRef mediator, String fromAddr, String toAddr, Event.Action action) {
        if(eventType == EventType.PUBLISH_INFO){
            //Event evt = new Event(fromAddr, toAddr, action);  //这里报错先注释，用下面暂时替代
            mediator.tell(new DistributedPubSubMediator.Publish(Topic.Event, getSelf()), getSelf());
        }
        if(eventType == EventType.RECEIVE_INFO){
            //Event evt = new Event(fromAddr, toAddr, action);  //这里报错先注释，用下面暂时替代
            mediator.tell(new DistributedPubSubMediator.Publish(Topic.Event, getSelf()), getSelf());
        }
    }

    /**
     * 获取有完全信息的地址（ip和port）
     * @param ref
     * @return
     */
    public String getClusterAddr(ActorRef ref) {
        return akka.serialization.Serialization.serializedActorPath(ref);
    }

    /**
     * cluster订阅消息
     *
     * @param mediator
     * @param self
     * @param addr
     * @param topic
     * @param isEvent
     */
    public void SubscribeTopic(ActorRef mediator, ActorRef self, String addr, String topic, Boolean isEvent) {
        mediator.tell(new DistributedPubSubMediator.Subscribe(topic, self()), getSender()); //这里用getSender()还是getSelf()？
        //广播本次订阅事件
        if (isEvent) sendEvent(EventType.PUBLISH_INFO, mediator, addr, Topic.Event, Event.Action.SUBSCRIBE_TOPIC);
    }

    /**
     * 两个节点是否相同的system
     * （不完全对，有可能是相同IP和port但是UID不同）
     * @param from
     * @param self
     * @return
     */
    public Boolean isSameSystem(ActorRef from, ActorRef self){
        ActorUtils actorUtils = ActorUtils.getInstance();
        return actorUtils.getIpAndPort(getClusterAddr(from)) == actorUtils.getIpAndPort(getClusterAddr(self));
    }

    /**
     * 两个节点是否是相同的system
     * @param from
     * @param self
     * @return
     */
    public Boolean isSameSystem(ActorRef from, String self){
        ActorUtils actorUtils = ActorUtils.getInstance();
        return actorUtils.getIpAndPort(getClusterAddr(from)) == actorUtils.getIpAndPort(self);
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
