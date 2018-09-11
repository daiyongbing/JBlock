package jblock.app.conf;

import com.typesafe.config.Config;

import java.util.ResourceBundle;

public class SystemProfile {
    /**
     * 交易创建类型
     */
    Enum Trans_Create_Type_Enum;{
        Integer MANUAL = 0; //API创建
        Integer AUTO = 1; //自动创建
    }

    private Integer _LIMIT_BLOCK_TRANS_NUM = 0;//块内最多交易数
    private Integer _MIN_BLOCK_TRANS_NUM = 0;//块内最少交易数
    private Integer _VOTE_NOTE_MIN = 0;//投票最少参与人数
    private Integer _TRAN_CREATE_DUR = 0;//交易创建时间间隔-针对自动创建
    private Integer _TRANS_CREATE_TYPE = 0;//交易创建类型
    private Integer _RETRY_TIME = 0;//投票重试次数限制
    private Integer _MAX_CATCH_TRANS_NUM = 0;//交易最多缓存数量
    private static Long _DISKSPACE_ALARM_NUM = 0l;//磁盘剩余空间预警 单位=M
    private static Integer _SERVERPORT = 8081;//http服务的端口，默认为8081
    private Integer _CHECKCERTVALIDATE = 0;//是否检查证书的有效性，0不检查，1检查
    private Integer _CONTRACTOPERATIONMODE = 0;//设置合约的运行方式，0=debug方式，1=deploy，默认为debug方式，如果发布部署，必须使用deploy方式。


    private static Integer SERVERPORT = _SERVERPORT;
    private Integer CHECKCERTVALIDATE = _CHECKCERTVALIDATE;
    private static Long DISKSPACE_ALARM_NUM = _DISKSPACE_ALARM_NUM;
    private Integer CONTRACTOPERATIONMODE=_CONTRACTOPERATIONMODE;

    private void SERVERPORT_(Integer value){
        _SERVERPORT = value;
    }

    private void CHECKCERTVALIDATE_(Integer value){
        _CHECKCERTVALIDATE = value;
    }

    private void CONTRACTOPERATIONMODE_(Integer value){
        _CONTRACTOPERATIONMODE = value;
    }

    private void DISKSPACE_ALARM_NUM_(Long value){
        _DISKSPACE_ALARM_NUM = value;
    }

    private Integer MAX_CATCH_TRANS_NUM = _MAX_CATCH_TRANS_NUM;

    private void MAX_CATCH_TRANS_NUM_(Integer value){
        _MAX_CATCH_TRANS_NUM = value;
    }

    private Integer RETRY_TIME = _RETRY_TIME;


    private void RETRY_TIME_(Integer value){
        _RETRY_TIME = value;
    }


    private Integer TRANS_CREATE_TYPE = _TRANS_CREATE_TYPE;

    private void TRANS_CREATE_TYPE_(Integer value){
        _TRANS_CREATE_TYPE = value;
    }

    private Integer TRAN_CREATE_DUR = _TRAN_CREATE_DUR;

    private void TRAN_CREATE_DUR_(Integer value){
        _TRAN_CREATE_DUR = value;
    }

    private Integer VOTE_NOTE_MIN = _VOTE_NOTE_MIN;

    private void VOTE_NOTE_MIN_(Integer value){
        _VOTE_NOTE_MIN = value;
    }

    private Integer MIN_BLOCK_TRANS_NUM = _MIN_BLOCK_TRANS_NUM;

    private void MIN_BLOCK_TRANS_NUM_(Integer value){
        _MIN_BLOCK_TRANS_NUM = value;
    }

    private Integer LIMIT_BLOCK_TRANS_NUM =  _LIMIT_BLOCK_TRANS_NUM;

    private void LIMIT_BLOCK_TRANS_NUM_(Integer value){
        _LIMIT_BLOCK_TRANS_NUM = value;
    }

    /**
     * 初始化配饰信息
     * @param config
     */
    public void initConfigSystem(Config config){
        LIMIT_BLOCK_TRANS_NUM_(config.getInt("system.block.trans_num_limit"));
        MIN_BLOCK_TRANS_NUM_(config.getInt("system.block.trans_num_min"));
        RETRY_TIME_(config.getInt("system.block.retry_time"));
        VOTE_NOTE_MIN_(config.getInt("system.vote.vote_note_min"));
        TRAN_CREATE_DUR_(config.getInt("system.transaction.tran_create_dur"));
        MAX_CATCH_TRANS_NUM_(config.getInt("system.transaction.max_cache_num"));
        TRANS_CREATE_TYPE_(config.getInt("system.trans_create_type"));
        DISKSPACE_ALARM_NUM_(config.getLong("system.diskspaceManager.diskspacealarm"));
        SERVERPORT_(config.getInt("system.httpServicePort"));
        CHECKCERTVALIDATE_(config.getInt("system.checkCertValidate"));
        CONTRACTOPERATIONMODE_(config.getInt("system.contractOperationMode"));
    }

    public Integer getLimitBlockTransNum(){
        return LIMIT_BLOCK_TRANS_NUM;
    }

    public Integer getMinBlockTransNum(){
        return MIN_BLOCK_TRANS_NUM;
    }

    public Integer getVoteNoteMin(){
        return VOTE_NOTE_MIN;
    }

    public Integer getTranCreateDur(){
        return TRAN_CREATE_DUR;
    }

    public Integer getMaxCacheTransNum(){
        return MAX_CATCH_TRANS_NUM;
    }

    public Integer getTransCreateType(){
        return TRANS_CREATE_TYPE;
    }

    public Integer getRetryTime(){
        return RETRY_TIME;
    }

    public static Long getDiskSpaceAlarm(){
        return DISKSPACE_ALARM_NUM;
    }

    public static Integer getHttpServicePort(){
        return SERVERPORT;
    }

    public Integer getCheckCertValidate(){
        return CHECKCERTVALIDATE;
    }

    public Integer getContractOperationMode(){
        return CONTRACTOPERATIONMODE;
    }
}
