package com.yijia.thread;

public class ThreadFlagConfig {
    
    private static ThreadFlag threadFlag;
    
    public static ThreadFlag getThreadFlag(){
        
        if(threadFlag == null){
            synchronized (ThreadFlagConfig.class) {
                if(threadFlag == null){
                    init();
                }
            }
        }
        return threadFlag;
        
    }
    
    private static void init(){
        threadFlag = new ThreadFlag(true);
    }

}
