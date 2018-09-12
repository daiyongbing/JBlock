package jblock.utils;

import akka.actor.Props;
import jblock.protos.Peer.Transaction;
import org.junit.Test;


public class GlobalUtils {
   // case class TranscationPoolPackage(t:Transaction,createTime:Long)
   public static class TranscationPoolPackage{
       //目的是使其支持模式匹配
       Props props;
       Transaction t;
       Long createTime;
       public TranscationPoolPackage(Transaction t, Long createTime){
           //this.props = props;
           this.t = t;
           this.createTime = createTime;
       }
   }
    //case class BlockChainStatus(CurrentBlockHash:String,CurrentMerkle:String,CurrentHeight:Long)
    public static class BlockChainStatus{
        Props props;
        public String CurrentBlockHash;
        public String CurrentMerkle;
        public Long CurrentHeight;

        public BlockChainStatus(String currentBlockHash, String currentMerkle, Long currentHeight) {
            this.CurrentBlockHash = currentBlockHash;
            this.CurrentMerkle = currentMerkle;
            this.CurrentHeight = currentHeight;
        }
    }

   public static class BlockEvent{
       //同步信息广播
       public static final String CHAIN_INFO_SYNC = "CHAIN_INFO_SYNC";

       //创建block
       public static final String CREATE_BLOCK = "CREATE_BLOCK";
       //出块人
       public static final String BLOCKER = "BLOCKER";
       public static final String BLOCK_HASH = "BLOCK_HASH";
       //创世块
       public static final String GENESIS_BLOCK = "GENESIS_BLOCK";
       //出块成功
       public static final String NEW_BLOCK = "NEW_BLOCK";
       //背书请求
       public static final String BLOCK_ENDORSEMENT = "BLOCK_ENDORSEMENT";
       //背书反馈
       public static final String ENDORSEMENT = "ENDORSEMENT";
       //出块确认
       public static final String ENDORSEMENT_CHECK = "ENDORSEMENT_CHECK";
       //出块确认反馈
       public static final String ENDORSEMENT_RESULT = "ENDORSEMENT_RESULT";
       //同步区块
       public static final String BLOCK_SYNC = "BLOCK_SYNC";
       //同步区块数据
       public static final String BLOCK_CHAIN = "BLOCK_CHAIN";
   }

   public static class ActorType{
       public static final int MEMBER_LISTENER = 1;
       public static final int MODULE_MANAGER = 2;
       public static final int API_MODULE = 3;
       public static final int PEER_HELPER = 4;
       public static final int BLOCK_MODULE = 5;
       public static final int PRELOADTRANS_MODULE = 6;
       public static final int ENDORSE_MODULE = 7;
       public static final int VOTER_MODULE = 8;
       public static final int SYNC_MODULE = 9;
       public static final int TRANSACTION_POOL = 10;
       public static final int PERSISTENCE_MODULE = 11;
       public static final int CONSENSUS_MANAGER = 12;
       public static final int STATISTIC_COLLECTION = 13;
   }

   public static class EventType{
       public static final int PUBLISH_INFO = 1;
       public static final int RECEIVE_INFO = 2;
   }

    public static String AppConfigPath = "application.conf";
    public static String SysConfigPath = "conf/system.conf";

}
