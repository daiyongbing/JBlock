package jblock.network;


public class PeerHelper {
    public static class Topic{
        public static final String Transaction = "Transaction";
        public static final String Block = "Block";
        public static final String Event = "Event";
        public static final String Endorsement = "Endorsement";
    }

    public static class InnerTopic{
        public static final String BlockRestore = "BlockRestore";
    }
}
