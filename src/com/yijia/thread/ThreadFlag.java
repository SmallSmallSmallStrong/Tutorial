package com.yijia.thread;

public class ThreadFlag {
    
    private Boolean canChange;
    
    
    public ThreadFlag(boolean canChange){
        this.canChange = canChange;
    }
    
    public Boolean getCanChange() {
        return canChange;
    }

    public synchronized void setCanChange(Boolean canChange) {
        this.canChange = canChange;
    }
    
}
