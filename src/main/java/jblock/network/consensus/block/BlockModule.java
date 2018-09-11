package jblock.network.consensus.block;

import akka.actor.ActorRef;
import jblock.protos.Peer.Block;

public class BlockModule {
    //case class PrimaryBlock4Cache(blc: Block, blocker: String,voteinedx:Int,actRef: ActorRef,blkidentifier:String)
    public static class PrimaryBlock4Cache{
        private Block blc;
        private String blocker;
        private int voteindex;
        private ActorRef actorRef;
        private String blkidentifier;

        public PrimaryBlock4Cache(Block blc, String blocker, int voteindex, ActorRef actorRef, String blkidentifier) {
            this.blc = blc;
            this.blocker = blocker;
            this.voteindex = voteindex;
            this.actorRef = actorRef;
            this.blkidentifier = blkidentifier;
        }
    }

    //正式块
    //case class ConfirmedBlock(blc: Block, height: Long, actRef: ActorRef)
    public static class ConfirmedBlock{
        private Block blk;
        private Long height;
        private ActorRef actRef;

        public ConfirmedBlock(Block blk, Long height, ActorRef actRef) {
            this.blk = blk;
            this.height = height;
            this.actRef = actRef;
        }

        public Block getBlk() {
            return blk;
        }

        public void setBlk(Block blk) {
            this.blk = blk;
        }

        public Long getHeight() {
            return height;
        }

        public void setHeight(Long height) {
            this.height = height;
        }

        public ActorRef getActRef() {
            return actRef;
        }

        public void setActRef(ActorRef actRef) {
            this.actRef = actRef;
        }
    }
}
