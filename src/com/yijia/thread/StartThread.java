package com.yijia.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.yijia.elasticsearch.bean.ESBeanConfig;

public class StartThread extends Thread {
       @Override
       public void run() {
           
              ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
              scheduler.scheduleAtFixedRate(new AutoSynchThread(), 0, ESBeanConfig.getESBean().getTimeInterval(), TimeUnit.MINUTES);
              // 网络
              // scheduler.awaitTermination(timeout, unit);
       }
}
