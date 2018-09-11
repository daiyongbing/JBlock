package jblock.app.system;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jblock.app.conf.SystemConf;
import jblock.app.conf.SystemProfile;
import jblock.network.base.ModuleBase;
import jblock.network.cluster.MemberListener;
import jblock.network.module.ModuleManager;
import jblock.network.tools.Statistic.StatisticCollection;
import jblock.network.tools.register.ActorRegister;
import jblock.storage.cfg.StoreConfig;
import jblock.ui.web.EventServer;
import jblock.utils.GlobalUtils.ActorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * System创建类
 * */
public class ClusterSystem {
    // ClusterSystem是系统的创建类，为了避免同时创建多个系统，将其设计成单例模式
    // 采用“双重校验锁法”实现单例模式，具有线程安全和懒加载两种特性
    private volatile static ClusterSystem instance;
    private ClusterSystem(){
        //nothing to do
    }
    public static ClusterSystem getInstance(){
        if(instance == null){
            synchronized (ClusterSystem.class){
                if(instance == null){
                    instance = new ClusterSystem();
                }
            }
        }
        return instance;
    }
/******************************** 单例模式 - end *******************************/

/******************************ClusterSystem的伴生对象 begin******************************/

    /**
     * 初始化类型,Java中没有object和结构体类型，使用静态内部类完成
     */
    public static class InitType{
        public static final int SINGLE_INIT = 1;//单机单节点
        public static final int MULTI_INIT = 2;//单机多节点
    }

    private static Map actorRegisterList = new HashMap<String, ActorRegister>();

    public static void register(String systemName, ActorRegister actorRegister){
        actorRegisterList.put(systemName, actorRegister);
        // 要不要返回actorRegisterList？
    }

    public static ActorRegister getActorRegister(String sysName){
        return (ActorRegister)actorRegisterList.get(sysName);
    }

    public static void unregister(String systemName){
        actorRegisterList.remove(systemName);
    }
/******************************ClusterSystem的伴生对象 end******************************/

/*************************ClusterSystem的带参构造用于系统组网的初始化**********************/
    private String sysTag;
    private int initType;
    private boolean sysStart;
    private String moduleName;
    private Config sysConf;
    public void setSysStart(boolean sysStart) {
        this.sysStart = sysStart;
    }

    /**
     *
     * @param sysTag 系统system命名
     * @param initType 初始化类型
     * @param sysStart 是否开启system（不开启仅用于初始化）
     */
    public ClusterSystem(String sysTag, Integer initType, Boolean sysStart) {
        this.sysTag = sysTag;
        this.initType = initType;
        this.sysStart = sysStart;
        this.moduleName= modulePrefix + "_" + sysTag;
        this.sysConf = initSystem(this.sysTag);
    }


    private final String USER_CONFIG_PATH = "conf/system.conf";
    private final String modulePrefix = "JBlockCluster";

    private ActorRef webSocket = null;

    private ActorRef memberLis = null;

    private ActorRef moduleManager = null;

    private ActorRef statistics = null;

    private Boolean enableWebSocket = false;

    private Boolean enableStatistic = false;

    private ActorSystem sysActor = null;

    private Address clusterAddr = null;

    //LoggingAdapter log = Logging.getLogger(, this);
    Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 是否开启Web Socket（API）
     */
    public void enableWS() {
        this.enableWebSocket = true;
    }


    /**
     * 获取用户和系统的联合配置
     *
     * @param userConfigFilePath
     * @return
     */
    public Config getUserCombinedConf(String userConfigFilePath) {
        File userConfFile = new File(userConfigFilePath);
        Config innerConf = ConfigFactory.load();
        if (userConfFile.exists()) {
            Config combined_conf = ConfigFactory.parseFile(userConfFile).withFallback(innerConf);
            Config final_conf = ConfigFactory.load(combined_conf);
            return final_conf;
        } else {
            //logMsg(LOG_TYPE.WARN, moduleName, "Couldn't find the user config file", "");
            log.warn(moduleName, "Couldn't find the user config file" );
            return innerConf;
        }
    }

    /**
     * 获取完整配置信息
     * 用户系统初始化
     *
     * @param sysName
     * @return
     */
    public Config getConfigBySys(String sysName) {
        //TODO 将来找个路径也是可配置的
        Config myConfig = ConfigFactory.parseString("akka.remote.netty.ssl.security.key-store = \"jks/mykeystore_" + sysName +
                ".jks\"");
        Config regularConfig = getUserCombinedConf(USER_CONFIG_PATH);
        Config combined = myConfig.withFallback(regularConfig);
        Config complete = ConfigFactory.load(combined);
        return complete;
    }

    public Config getConf() {
        return this.sysConf;
    }

    public Boolean hasDiskSpace() {
        Boolean b = true;
        StoreConfig sc = StoreConfig.getStoreConfig();
        Long ds = sc.getFreeDiskSpace() / (1000 * 1000);
        if (new SystemProfile().getDiskSpaceAlarm() >= ds) {
            b = false;
        }
        return b;
    }

    /**
     * 初始化系统参数
     *
     * @param sysName
     * @return
     */
    public Config initSystem(String sysName) {
        Config conf = getConfigBySys(sysName);
        //logMsg(LOG_TYPE.INFO, moduleName, "System configuration successfully", "");
        log.info(moduleName, "System configuration successfully");
        if (conf.getInt("system.ws_enable") == 1) {
            this.enableWebSocket = true;
        } else if (conf.getInt("system.ws_enable") == 0) {
            this.enableWebSocket = false;
        }

        if (conf.getInt("system.statistic_enable") == 1) {
            this.enableStatistic = true;
        } else if (conf.getInt("system.statistic_enable") == 0) {
            this.enableStatistic = false;
        }
        return conf;
    }

    public Address getClusterAddr() {
        return this.clusterAddr;
    }

    /**
     * 组网
     *
     * @param address
     * @return
     */
    public Boolean joinCluster(Address address) {
        if(initType == InitType.SINGLE_INIT){
            Cluster.get(sysActor);
        }
        if(initType == InitType.MULTI_INIT){
            Cluster.get(sysActor).join(address);
        }
        return true;
    }

    /**
     * 初始化
     */
    public void init() {
        if(sysStart){
            sysActor = ActorSystem.create(SystemConf.SYSTEM_NAME, sysConf);
            clusterAddr = Cluster.get(sysActor).selfAddress();
        }
        new ClusterSystem().register(sysTag, new ActorRegister());
        //logMsg(LOG_TYPE.INFO, moduleName, s"System(${sysTag}) init successfully", "s");
        log.info(moduleName, "init successfully");
    }

    /**
     * 启动系统
     */
    public void start() {
        SystemProfile systemProfile = new SystemProfile();
        if (enableStatistic) {
            this.statistics = sysActor.actorOf(Props.create(StatisticCollection.class), "statistic");
        }
        Props props = ModuleManager.props("moduleManager", sysTag);
        moduleManager = sysActor.actorOf(props, "moduleManager");
        ModuleBase.registerActorRef(sysTag, ActorType.MODULE_MANAGER, moduleManager);
        systemProfile.initConfigSystem(sysActor.settings().config());
        if (!hasDiskSpace()) {
            Cluster.get(sysActor).down(clusterAddr);
            new Exception("not enough disk space");
        }
        if (enableWebSocket) {
            this.webSocket = sysActor.actorOf(Props.create(EventServer.class), "ws");
        }
        this.memberLis = sysActor.actorOf(Props.create(MemberListener.class), "memberListener");
        ModuleBase.registerActorRef(sysTag, ActorType.MEMBER_LISTENER, memberLis);
        if (enableWebSocket) {
            ModuleBase.registerActorRef(sysTag, ActorType.API_MODULE, webSocket);
        }
        if (enableStatistic) {
            ModuleBase.registerActorRef(sysTag, ActorType.STATISTIC_COLLECTION, statistics);
        }

        //logMsg(LOG_TYPE.INFO, "system", s"ClusterSystem ${sysTag} start", "");
        log.info("system", "ClusterSystem "+ sysTag+ "start");
    }

    /**
     * 离网
     *
     * @param clusterActor
     */
    public void leaveCluster(ActorSystem clusterActor) {
        Cluster.get(clusterActor).leave(getClusterAddr());
    }

    public ActorSystem getActorSys() {
        return this.sysActor;
    }
}