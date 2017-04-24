package com.yijia.elasticsearch.bean;

import org.apache.log4j.Logger;

import com.yijia.jdbc.ConnectServerConfig;
import com.yijia.thread.ThreadFlag;
import com.yijia.thread.ThreadFlagConfig;

public class SynchData {
    
    private Logger logger = Logger.getLogger(SynchData.class);

    private ThreadFlag canChange;
    
    /**
     * 同步数据
     * 
     * @param synchTableName 指定同步表名(null表示根据配置文件， *代表全部，多个表用前后逗号区分，例如：,z_user,z_role,)
     */
    public SynchData() {
        synchInit();
        canChange.setCanChange(false);
        ConnectServerConfig.synch();
        canChange.setCanChange(true);
	}

    
    /**
     * 初始化参数和等待可以同步数据
     */
    private synchronized void synchInit(){
        canChange = ThreadFlagConfig.getThreadFlag();
        if(false == canChange.getCanChange()){
            //每隔3s检查canChange 如果标志变为 true就跳出该方法
            //貌似没啥用处啊
            while(true){
                try {
                    Thread.sleep(1000 * 3);
                } catch (InterruptedException e) {
                    logger.info(ESBeanConfig.getESBean().getStation() + " 线程被强制唤醒了。", e);
                }
                if(true == canChange.getCanChange()){
                    break;
                }
            }
        }
    }

}
