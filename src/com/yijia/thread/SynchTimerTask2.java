package com.yijia.thread;

import java.util.Date;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.yijia.elasticsearch.bean.ESBeanConfig;
import com.yijia.util.Tool;

public class SynchTimerTask2 extends TimerTask {
    
    private Logger logger = Logger.getLogger(SynchTimerTask2.class);
    private Integer synchTime = Tool.sToI(ESBeanConfig.getESBean().getSynchTime(), 10);
    
    @Override
    public void run() {
        /** 自动同步次数 */
        for(int num = 1; num <= synchTime; num++){
            logger.info(Tool.formatTime(new Date(), "yyyy-MM-dd", "") + " 第 " + num + " 次自动同步数据开始。");
            new AutoSynchThread().start();
        }
    }
    
}
