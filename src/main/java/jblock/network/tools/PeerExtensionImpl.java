package jblock.network.tools;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.Extension;
import jblock.network.consensus.block.BlockModule.PrimaryBlock4Cache;
import jblock.protos.Peer;
import jblock.utils.GlobalUtils.BlockChainStatus;
import jblock.utils.GlobalUtils.TranscationPoolPackage;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeerExtensionImpl implements Extension {
    private Map<String, TranscationPoolPackage> transactions = new LinkedHashMap<String, TranscationPoolPackage>();

    //本地缓存网络节点
    private Map<String, Address> nodes = new TreeMap<String,Address>();
    //本地缓存稳定的网络节点
    private Map<String,Address> stableNodes = new TreeMap<String,Address>();
    //本地上次候选人名单
    private Map<String,String> candidator = new TreeMap<String,String>();

    private ConcurrentLinkedQueue<PrimaryBlock4Cache> tmpEndorse  = new ConcurrentLinkedQueue<PrimaryBlock4Cache>();

    private AtomicReference<String> blocker = new AtomicReference<String>("");
    //保存当前hash值（通过全网广播同步更新成最新的block hash）

    private AtomicReference<String> voteBlockHash = new AtomicReference<String>("0");
    private AtomicReference<BlockChainStatus> SystemCurrentChainStatus = new AtomicReference<BlockChainStatus>(new BlockChainStatus("0","",0l));

    //必须是线程安全的,需要原子操作
    private AtomicBoolean isBlocking = new AtomicBoolean(false);
    private AtomicBoolean endorState = new AtomicBoolean(false);
    private AtomicBoolean isBlockVote = new AtomicBoolean(false);

    private AtomicBoolean isSync = new AtomicBoolean(false);
    private AtomicInteger cacheBlkNum = new AtomicInteger(0);
    private AtomicReference<Address> seedNode = new AtomicReference<Address>(new Address("", ""));
    private AtomicInteger blker_index = new AtomicInteger(1);

    private AtomicReference<String> sys_ip = new AtomicReference<String>("");
    private AtomicReference<String> sys_port = new AtomicReference<String>("");
    private AtomicReference<String> dbTag = new AtomicReference<String>("");
    private AtomicReference<String> sysTag = new AtomicReference<String>("");

    //数据访问锁
    private Lock transLock = new ReentrantLock();
    private Lock  nodesLock = new ReentrantLock();
    private Lock  nodesStableLock = new ReentrantLock();
    private Lock  candidatorLock = new ReentrantLock();

    //系统状态
    //0 系统启动；1 组网中；2 组网完成； 3 同步； 4 同步完成；5 投票；6 投票完成； 7 背书； 8 背书完成； 9 出块； 10 出块完成 11 创始块开始 12 创始块结束
    private AtomicInteger systemStatus = new AtomicInteger(0);

    public PrimaryBlock4Cache getTmpEndorse(){
        return this.tmpEndorse.poll();
    }

    public void setTmpEndorse(PrimaryBlock4Cache v){
        this.tmpEndorse.add(v);
    }

    public List<TranscationPoolPackage> getTransListClone(int num) {
        List<TranscationPoolPackage> result = new ArrayList<TranscationPoolPackage>();
        transLock.lock();
        try{
            int len = (transactions.size() < num)?transactions.size():num;
            /**
             * 这段代码的作用是取出transactions的前len个元素，然后对其进行遍历，将结果的value存到result中
             */
            //transactions.take(len).foreach(pair => pair._2 +=: result)

            //用Java实现相同的效果
            List<TranscationPoolPackage> list = new ArrayList<TranscationPoolPackage>();
            transactions.values().forEach(value -> list.add(value));
            //result = list.subList(0, len); //神坑！！！修改subList的内容会影响到原List，此处需要对subList反转，不能使用此方法
            for (int i=len-1; i>=0; i--){
                result.add(list.get(i));
            }
        }finally{
            transLock.unlock();
        }
        return result;
    }

    public void putTran(Peer.Transaction tran){
        transLock.lock();
        try{
            String txid = tran.getTxid();
            if (transactions.containsKey(txid)) {
                System.out.println(txid + "exists in cache");
            }
            else transactions.put(txid, new TranscationPoolPackage(tran,System.currentTimeMillis()/1000));
        }finally {
            transLock.unlock();
        }
    }

    //TODO kami 需要清空之前所有的block的Transaction
    public void removeTrans(List<Peer.Transaction> trans) {
        transLock.lock();
        try{
            for (Peer.Transaction curT : trans) {
                if (transactions.containsKey(curT.getTxid())) transactions.remove(curT.getTxid());
            }
        }finally{
            transLock.unlock();
        }
    }

    public void removeTranscation(Peer.Transaction tran){
        transLock.lock();
        try{
            if (transactions.containsKey(tran.getTxid())) transactions.remove(tran.getTxid());
        }finally{
            transLock.unlock();
        }
    }

    public int getTransLength() {
        int len = 0;
        transLock.lock();
        try{
            len = transactions.size();
        }finally{
            transLock.unlock();
        }
        return len;
    }


    public String getCurrentBlockHash() {
        return this.getSystemCurrentChainStatus().CurrentBlockHash;
    }

    public Set<Address> getNodes() {
        Set<Address> source = new HashSet<Address>();
        nodesLock.lock();
        try{
            source= new HashSet<Address>(nodes.values());
        }finally{
            nodesLock.unlock();
        }
        return source;
    }

    public void putNode(Address addr) {
        nodesLock.lock();
        try{
            String key = addr.toString();
            nodes.put(key, addr);
        }finally{
            nodesLock.unlock();
        }
    }

    public void removeNode(Address addr) {
        nodesLock.lock();
        try{
            String key = addr.toString();
            nodes.remove(key);
        }finally{
            nodesLock.unlock();
        }
    }

    public void resetNodes(Set<Address> nds) {
        nodesLock.lock();
        try{
            nodes = new TreeMap<String, Address>();
        }finally{
            nodesLock.unlock();
        }
        nds.forEach(address -> putNode(address)); //lambda表达式仅支持Java8以上
    }

    public Set<Address> getStableNodes(){
        Set<Address> source;
        nodesStableLock.lock();
        try{
            source = new HashSet<Address>(stableNodes.values());
        }finally{
            nodesStableLock.unlock();
        }
        return source;
    }

    public void putStableNode(Address addr) {
        nodesStableLock.lock();
        try{
            String key = addr.toString();
            stableNodes.put(key, addr);
        }finally{
            nodesStableLock.unlock();
        }
    }

    public void removeStableNode(Address addr) {
        nodesStableLock.lock();
        try{
            String key = addr.toString();
            stableNodes.remove(key);
        }finally{
            nodesStableLock.unlock();
        }
    }

    public void resetStableNodes(Set<Address> nds) {
        nodesStableLock.lock();
        try{
            stableNodes = new TreeMap<String,Address>();
        }finally{
            nodesStableLock.unlock();
        }
        nds.forEach(address -> putStableNode(address));
    }

    public Set<String> getCandidator() {
        Set<String> source;
        candidatorLock.lock();
        try{
            source = new HashSet<String>(candidator.values());
        }finally{
            candidatorLock.unlock();
        }
        return source;
    }

    public void putCandidator(String addr) {
        candidatorLock.lock();
        try{
            String key = addr.toString();
            candidator.put(key, addr);
        }finally{
            candidatorLock.unlock();
        }
    }

    public void resetCandidator(ArrayList<String> nds) {
        candidatorLock.lock();
        try{
            candidator = new TreeMap<String, String>();
        }finally{
            candidatorLock.unlock();
        }
        nds.forEach(addr -> putCandidator(addr));
    }



    public void resetBlocker(String addr) {
        blocker.set(addr);
    }

    public String getBlocker = blocker.get();

    public void resetSeedNode(Address addr) {
        seedNode.set(addr);
    }

    public Address getSeedNode = seedNode.get();

    public void resetSystemCurrentChainStatus(BlockChainStatus value){
        this.SystemCurrentChainStatus.set(value);
    }

    public BlockChainStatus getSystemCurrentChainStatus(){
        return this.SystemCurrentChainStatus.get();
    }

    public void setIpAndPort(String ip, String port){
        this.sys_ip.set(ip);
        this.sys_port.set(port);
    }

    public String getIp = sys_ip.get();

    public String getPort = sys_port.get();

    public void setDBTag(String root){
        dbTag.set(root);
    }

    public String getDBTag = dbTag.get();

    public void setSystemStatus(int status) {
        systemStatus.set(status);
    }

    public int getSystemStatus = systemStatus.get();

    public String getMerk = this.getSystemCurrentChainStatus().CurrentMerkle;

    public void setSysTag(String name) {
        sysTag.set(name);
    }

    public String getSysTag = sysTag.get();

    public void setIsSync(Boolean isSync){
        this.isSync.set(isSync);
    }

    public Boolean getIsSync(){
        return isSync.get();
    }

    public void setIsBlocking(Boolean isblocking) {
        this.isBlocking.set(isblocking);
    }

    public Boolean getIsBlocking() {
        return this.isBlocking.get();
    }

    public Boolean getIsBlockVote() {
        return this.isBlockVote.get();
    }

    public void setIsBlockVote(Boolean value){
        this.isBlockVote.set(value);
    }

    public void setEndorState(Boolean endorState) {
        this.endorState.set(endorState);
    }

    public Boolean getEndorState() {
        return this.endorState.get();
    }

    public Long getCacheHeight() {
        return this.getSystemCurrentChainStatus().CurrentHeight;
    }

    public int addCacheBlkNum() {
        return cacheBlkNum.addAndGet(1);
    }

    public int rmCacheBlkNum() {
        return  cacheBlkNum.addAndGet(-1);
    }

    public int getCacheBlkNum() {
        return cacheBlkNum.get();
    }

    public int getBlker_index = blker_index.get();
    public int AddBlker_index = blker_index.getAndAdd(1);
    public void resetBlker_index(){
        blker_index.set(0);
    }
    public void setBlker_index(int value) {
        blker_index.set(value);
    }

    public String getVoteBlockHash = voteBlockHash.get();
    public void setVoteBlockHash(String value) {
        voteBlockHash.set(value);
    }

    /*********系统Actor注册相关操作开始************/
    private Map<Integer, akka.actor.ActorRef> actorList = new HashMap<Integer, ActorRef>();

    public void register(int actorName, ActorRef actorRef){
        actorList.put(actorName,actorRef);
    }

    public ActorRef getActorRef(int actorName) {
        return actorList.get(actorName);
    }

    public void unregister(int actorName) {
        actorList.remove(actorName);
    }
    /*********系统Actor注册相关操作结束************/
}
