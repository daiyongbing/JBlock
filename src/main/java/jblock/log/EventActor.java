package jblock.log;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;

import akka.event.LoggingAdapter;
import akka.stream.actor.AbstractActorPublisher;
import jblock.network.PeerHelper.Topic;
import jblock.network.tools.PeerExtension;
import jblock.network.tools.PeerExtensionImpl;
import jblock.protos.Peer.Event;
import scala.collection.Iterator;
import scala.collection.immutable.Vector;
import scala.collection.immutable.VectorIterator;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class EventActor extends AbstractActorPublisher<Event> {
    static Props props = Props.create(EventActor.class);


    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Cluster cluster = Cluster.get(getContext().system());
    Set nodes = new HashSet<Address>();
    Vector<Event> buffer = new Vector<Event>(0, 0, 0);



    /** 启动,订阅集群入网、离网事件,订阅Topic事件
     *
     */
    @Override
    public void preStart(){
        cluster.subscribe(getSelf(), MemberEvent.class);
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

        //发送订阅Event
        mediator.tell(new DistributedPubSubMediator.Subscribe(Topic.Event, getSelf()), getSelf());
        //发送当前出块人
        PeerExtensionImpl pe = new PeerExtension().get(getContext().system());
        Event.Builder builder = Event.newBuilder();
        builder.setFrom(pe.getBlocker);
        builder.setTo("");
        builder.setAction(Event.Action.CANDIDATOR);
        Event event = builder.build();
        getSelf().tell(event, sender());
    }

        /** 停止处理，取消订阅
         *
         */
        @Override
        public void postStop(){
            cluster.unsubscribe(getSelf());
        }


    /** 接收Event处理，支持所谓“背压”方式，即根据web端消费能力push
     * java与scala的消息不同
     * @since 2018-08-31
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                //Topic事件
                .match(Event.class, event -> {
                    if(super.isCanceled()){
                        self().tell(PoisonPill.getInstance(), self());
                    }else{
                        if (buffer.isEmpty() && super.totalDemand() > 0) {
                            onNext(event);
                        }
                        else {
                            buffer.appendBack(event);
                            if (super.totalDemand() > 0) {
                                Vector<Event> use = buffer.splitAt((int) super.totalDemand())._1;
                                Vector<Event> keep = buffer.splitAt((int)super.totalDemand())._2;
                                buffer = keep;
                               VectorIterator<Event> u = use.iterator();
                               if (u.hasNext()){
                                   onNext(u.next());
                               }
                            }
                        }
                    }
                    getSender().tell(event, getSelf());
                })
                //集群事件
                .match(ClusterEvent.CurrentClusterState.class, state -> {
                    Iterator<Member> iterator = state.members().iterator();
                    if (iterator.hasNext()){
                        Member m = iterator.next();
                        if (m.status() == MemberStatus.up()){
                            Event.Builder builder = Event.newBuilder();
                            builder.setFrom(m.address().toString());
                            builder.setTo("");
                            builder.setAction(Event.Action.MEMBER_UP);
                            Event event = builder.build();
                            getSelf().tell(event,getSelf() );
                        }
                    }
                })
                // 节点组网
                .match(MemberUp.class, mUp -> {
                    log.info("Member is Up: {}", mUp.member());
                    nodes.add(mUp.member().address());
                    Event.Builder builder = Event.newBuilder();
                    builder.setFrom(mUp.member().address().toString());
                    builder.setTo("");
                    builder.setAction(Event.Action.MEMBER_UP);
                    Event event = builder.build();
                    getSender().tell(event, sender());

                })
                // Unreachable
                .match(UnreachableMember.class, mUnreachable -> {
                    log.info("Member detected as unreachable: {}", mUnreachable.member());
                    // nothing to do
                })
                // 节点离网
                .match(MemberRemoved.class, mRemoved -> {
                    log.info("Member is Removed: {}", mRemoved.member());
                    String maddr = mRemoved.member().address().toString(); //离网节点的地址
                    String saddr = Cluster.get(getContext().system()).selfAddress().toString();
                    if (maddr == saddr) {
                        getContext().system().terminate();
                    } else {
                        nodes.remove(mRemoved.member().address());
                        Event.Builder builder = Event.newBuilder();
                        builder.setFrom(mRemoved.member().address().toString());
                        builder.setAction(Event.Action.MEMBER_DOWN);
                        builder.setTo("");
                        Event event = builder.build();
                        getSender().tell(event, getSelf());
                    }
                })
                .match(MemberEvent.class, message -> {
                    // ignore
                    log.info("MemberEvent: {}", message);
                })
                .build();
    }

    /**
     * 测试
     * proto生成的Java类的构造方法全部是私有的，并且没有提供私有属性的get方法
     * 要想修改私有属性的值，只能通过Builer来实现，具体参考：https://blog.csdn.net/puppylpg/article/details/80837143
     * @param args
     */
    public static void main(String[] args){
        Event.Builder builder = Event.newBuilder();
        builder.setFrom("akka.ssl.tcp://JBlock@192.168.56.1:63067");
        builder.setAction(Event.Action.MEMBER_DOWN);
        builder.setTo("akka.ssl.tcp://JBlock@192.168.56.1:63045");
        Event event = builder.build();
        System.out.println("from:"+event.getFrom() +"\nto:"+event.getTo()+"\naction:"+event.getAction().toString());
    }
}
