package com.yijia.thread;

import org.apache.log4j.Logger;

import com.yijia.elasticsearch.bean.ESBeanConfig;
import com.yijia.elasticsearch.bean.SynchData;
import com.yijia.util.Tool;

/**
 * 自动同步数据
 * 
 * @author zhqy
 *
 */
public class AutoSynchThread extends Thread {
    
    private Logger logger = Logger.getLogger(AutoSynchThread.class);
    private Integer synchInterval = Tool.sToI(ESBeanConfig.getESBean().getSynchInterval(), 59);
    
    /** 自动 */
    public static final Integer AUTO = 0;
    /** 手动 */
    public static final Integer HAND = 1;
    
    /** 自动同步次数 */
    private static int NUM = 1;
    /** 同步类型 */
    private Integer synchType;
    
    public AutoSynchThread() {
        synchType = AutoSynchThread.AUTO;
    }
    
    /**
     * 后台同步
     * 
     * @param synchType 同步类型
     * @param synchTableName 指定同步表名(null表示根据配置文件， *代表全部，多个表用前后逗号区分，例如：,z_user,z_role,)
     */
    public AutoSynchThread(Integer synchType) {
        if(AutoSynchThread.AUTO.equals(synchType)){
            this.synchType = AutoSynchThread.AUTO;
        }else if(AutoSynchThread.HAND.equals(synchType)){
            this.synchType = AutoSynchThread.HAND;
        }
    }
    
    @Override
    public void run() {
        String msg = null;
        if(AutoSynchThread.AUTO.equals(synchType)){
            msg = "自动";
                synch(msg);
                if(synchInterval != null && synchInterval > 0){
                    try {
                        Thread.sleep(synchInterval);
                    } catch (InterruptedException e) {
                        logger.error("第 " + AutoSynchThread.NUM + " 次同步后的休眠被强制唤醒【" + msg + "】");
                    }
                }
        }else if(AutoSynchThread.HAND.equals(synchType)){
            msg = "手动";
            synch(msg);
        }
    }
    
    /**
     * 开始同步
     * 
     * @param msg 自动或者手动
     */
    private synchronized void synch(String msg){
        logger.info("第 " + AutoSynchThread.NUM + " 次同步数据【" + msg + "】");
        new SynchData();
        AutoSynchThread.NUM++;
    }

}
