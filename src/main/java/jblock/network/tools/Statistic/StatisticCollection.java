package jblock.network.tools.Statistic;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import jblock.network.consensus.block.BlockModule.ConfirmedBlock;
import jblock.network.tools.PeerExtension;
import jblock.network.PeerHelper.Topic;
import akka.cluster.pubsub.DistributedPubSub;
import jblock.network.tools.PeerExtensionImpl;
import jblock.protos.Peer;
import jblock.utils.TimeUtils;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static jblock.network.PeerHelper.Topic;
import static jblock.network.PeerHelper.Topic;


public class StatisticCollection extends AbstractActor {
    // scala中StatisticCollection的伴生对象，这里使用静态内部类实现相同功能
    public static final class TPSCollection{}
    public static final class PerformanceCollection{}


    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    //mediator其实是一个用于消息分发和订阅的子Actor
    final ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    //mediator的使用示例
   /* public StatisticCollection(){
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe("content", getSelf()),getSelf());
    }*/


    Cancellable schedulerLink = null;

    static final String moduleName = "Statistic";

    final PeerExtensionImpl pe = new PeerExtension().get(getContext().system());

    Long count = 0l;

    Long secondCount = 0l;

    Boolean isReady = false;

    Long totalTranCount = 0l;

    Long totalTranSize = 0l;
    Long totalBlkSize = 0l;
    Long totalBlkCount = 0l;
    Long totalResultSize = 0l;

    Scheduler scheduler = getContext().system().scheduler();

    public Boolean clearSched(){
        if (schedulerLink != null){
            return schedulerLink.cancel();
        } else {
            return null;
        }
    }




    @Override
    public void preStart() {
        log.info(moduleName, "Statistic module start");
        //订阅Topic.Block
        mediator.tell(new DistributedPubSubMediator.Subscribe(Topic.Block, self()), getSelf());
        scheduler.scheduleOnce(FiniteDuration.create(1, TimeUnit.SECONDS), getSelf(), new TPSCollection(), getContext().dispatcher(), getSelf());
        scheduler.scheduleOnce(FiniteDuration.create(10, TimeUnit.SECONDS), getSelf(), new PerformanceCollection(), getContext().dispatcher(), getSelf());
    }





    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(ConfirmedBlock.class, cb -> {
                if (!isReady) isReady = true;
                Peer.Block blk = cb.getBlk();
                count = count + blk.getTransactionsList().size();
                totalBlkCount += 1;
                totalTranCount += blk.getTransactionsList().size();
                totalBlkSize += blk.toByteArray().length;
                blk.getNonHashData().getTransactionResultsList().forEach(trans -> totalResultSize += trans.toByteArray().length);
                blk.getTransactionsList().forEach(trans -> totalTranSize += trans.toByteArray().length);
            })
            .match(TPSCollection.class, message -> {
               /* logMsg(LOG_TYPE.INFO, moduleName, s"Statistic instant TPS is $count in ${TimeUtils.getCurrentTime()} " +
                        s" ~ Rest Trans is ${pe.getTransLength()}", "")*/
                log.info(moduleName, "Statistic instant TPS is " +  count + " in " + TimeUtils.getCurrentTime()+ "~ Rest Trans is "+ pe.getTransLength());
                if (isReady) {
                    secondCount += 1;
                    /*logMsg(LOG_TYPE.INFO, moduleName, s"Statistic AVG TPS is ${totalTranCount / secondCount} " +
                            s"in ${TimeUtils.getCurrentTime()} ~ Rest Trans is ${pe.getTransLength()}", "")*/
                    log.info(moduleName, "Statistic AVG TPS is"+  (totalTranCount / secondCount)+ "in " + TimeUtils.getCurrentTime()+ "~ Rest Trans is "+ pe.getTransLength());
                }
                count = 0l;
                scheduler.scheduleOnce(FiniteDuration.create(1, TimeUnit.SECONDS), getSelf(), new TPSCollection(), getContext().dispatcher(), getSelf());
            })
            .match(PerformanceCollection.class, pc -> {
                if (isReady) {
                    /*logMsg(LOG_TYPE.INFO, moduleName, s"Statistic AVG Blk（exclusive trans and trans result） Size  is ${(totalBlkSize - totalTranSize - totalResultSize) / totalBlkCount} in ${TimeUtils.getCurrentTime()}", "")
                    logMsg(LOG_TYPE.INFO, moduleName, s"Statistic AVG Trans Size  is ${totalTranSize / totalTranCount} in ${TimeUtils.getCurrentTime()}", "")
                    logMsg(LOG_TYPE.INFO, moduleName, s"Statistic AVG Trans Result Size  is ${totalResultSize / totalTranCount} in ${TimeUtils.getCurrentTime()}", "")*/
                }
                scheduler.scheduleOnce(FiniteDuration.create(10, TimeUnit.SECONDS), getSelf(), new PerformanceCollection(), getContext().dispatcher(), getSelf());
            })
            /*.match(LogTime.class, lt -> {
                //logTime(lt.module, lt.msg, lt.time, lt.cluster_addr);
            })*/
            .build();
    }

}
