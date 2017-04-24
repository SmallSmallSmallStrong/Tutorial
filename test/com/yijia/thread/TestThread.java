package com.yijia.thread;

import org.apache.log4j.Logger;

public class TestThread {
    
    private static Logger logger = Logger.getLogger(TestThread.class);

    public static void main(String[] args) {

        for(int i = 1; i < 10; i++){
            new Thread(new InsertThread(i + "")).start();
            logger.info("线程【" + i + "】开始。");
        }
        
    }

}
