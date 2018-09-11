package jblock.app.conf;

import com.typesafe.config.Config;

/**
 *时间策略相关的配置信息类
 * @author daiyongbing
 * @since v1.0
 */
public class TimePolicy {
    private int _TIMEOUT_BLOCK = 0;//出块超时
    private int _TIMEOUT_ENDORSE = 0;//背书超时
    private int _TIMEOUT_PRELOAD_TRANS = 0;//预执行超时
    private int _TIMEOUT_SYNC_CHAIN = 0;//同步超时
    private Long _VOTE_RETYR_DELAY = 0l;//投票延迟
    private Long _SYS_NODE_STABLE_DELAY = 0l;//节点稳定延迟
    private int _STABLE_TIME_DUR = 0;
    private Long _VOTE_WAITING_DELAY = 0l;//投票长时等待
    private int _TRANSCATION_WAITING = 900;//transcation_waiting


    private Long VOTE_WAITING_DELAY = _VOTE_WAITING_DELAY;
    public Long getVoteWaitingDelay(){return VOTE_WAITING_DELAY;}

    private int TRANSCATION_WAITING = _TRANSCATION_WAITING;

    public void VOTE_WAITING_DELAY(Long value){
        this._VOTE_WAITING_DELAY = value;
    }

    public void TRANSCATION_WAITING_(int value){
        this._TRANSCATION_WAITING = value;
    }

    private int TIMEOUT_BLOCK = _TIMEOUT_BLOCK;
    public void TIMEOUT_BLOCK_(int value){
        _TIMEOUT_BLOCK = value;
    }

    public int getTimeOutBlock(){
        return TIMEOUT_BLOCK;
    }



    private int TIMEOUT_ENDORSE = _TIMEOUT_ENDORSE;

    private void TIMEOUT_ENDORSE_(int value){
        _TIMEOUT_ENDORSE = value;
    }

    public int getTimeoutEndorse(){
        return TIMEOUT_ENDORSE;
    }

    private int TIMEOUT_PRELOAD_TRANS = _TIMEOUT_PRELOAD_TRANS;

    private void TIMEOUT_PRELOAD_TRANS_(int value){
        _TIMEOUT_PRELOAD_TRANS = value;
    }

    public int getTimeoutPreload(){
        return TIMEOUT_PRELOAD_TRANS;
    }

    private int TIMEOUT_SYNC_CHAIN = _TIMEOUT_SYNC_CHAIN;

    private void TIMEOUT_SYNC_CHAIN_(int value){
        _TIMEOUT_SYNC_CHAIN = value;
    }

    public int getTimeoutSync(){
        return TIMEOUT_SYNC_CHAIN;
    }

    private Long VOTE_RETYR_DELAY = _VOTE_RETYR_DELAY;

    private void VOTE_RETYR_DELAY_(Long value){
        _VOTE_RETYR_DELAY = value;
    }

    public Long getVoteRetryDelay(){
        return VOTE_RETYR_DELAY;
    }

    public int getTranscationWaiting(){
        return TRANSCATION_WAITING;
    }

    private Long SYS_NODE_STABLE_DELAY = _SYS_NODE_STABLE_DELAY;

    private void SYS_NODE_STABLE_DELAY_(Long value){
        _SYS_NODE_STABLE_DELAY = value;
    }

    public Long getSysNodeStableDelay(){
        return SYS_NODE_STABLE_DELAY;
    }

    private int STABLE_TIME_DUR = _STABLE_TIME_DUR;

    private void STABLE_TIME_DUR_(int value){
        _STABLE_TIME_DUR = value;
    }

    public int getStableTimeDur(){
        return STABLE_TIME_DUR;
    }

    /**
     * 初始化时间相关策略
     * @param config
     */
    public void initTimePolicy(Config config){
        VOTE_RETYR_DELAY = config.getLong("system.time.block.vote_retry_delay");
        VOTE_WAITING_DELAY = config.getLong("system.time.block.waiting_delay");
        SYS_NODE_STABLE_DELAY = config.getLong("system.cluster.node_stable_delay");
        STABLE_TIME_DUR = config.getInt("system.time.stable_time_dur");
        int policyType = config.getInt("system.time.timeout_policy_type");
        TRANSCATION_WAITING = config.getInt("system.time.timeout.transcation_waiting");

        switch (policyType){
            case PolicyType.MANUAL:
            {
                TIMEOUT_BLOCK = config.getInt("system.time.timeout.block");
                TIMEOUT_ENDORSE = config.getInt("system.time.timeout.endorse");
                TIMEOUT_PRELOAD_TRANS = config.getInt("system.time.timeout.transaction_preload");
                TIMEOUT_SYNC_CHAIN = config.getInt("system.time.timeout.sync_chain");
            }
            break;
            case PolicyType.AUTO:
            {
                //这里我们根据经验设定算法，通过基准时间（一个出块时间），来配置其他的超时时间
                //类似于默认
                int basePre = config.getInt("system.time.timeout.base_preload");
                int baseSync = config.getInt("system.time.timeout.base_sync");
                int baseAdd = config.getInt("system.time.timeout.base_addition");
                TIMEOUT_PRELOAD_TRANS = basePre;
                TIMEOUT_ENDORSE = basePre*2;
                TIMEOUT_BLOCK = (3 * basePre + baseAdd);
                TIMEOUT_SYNC_CHAIN = baseSync;
            }
        }
    }

    /**
     * 时间策略类型
     */
    public static class PolicyType {
        final public static int MANUAL = 1;
        final public static int AUTO = 0;
    }
}
