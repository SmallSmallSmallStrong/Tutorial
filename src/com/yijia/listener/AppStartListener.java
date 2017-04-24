package com.yijia.listener;

import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import com.yijia.thread.AutoSynchThread;
import com.yijia.thread.StartThread;

public class AppStartListener implements ServletContextListener, HttpSessionListener {

	private Logger logger = Logger.getLogger(AppStartListener.class);
	// private String synchHour = ESBeanConfig.getESBean().getSynchHour();
	// private String synchMinute = ESBeanConfig.getESBean().getSynchMinute();
	// private String synchSecond = ESBeanConfig.getESBean().getSynchSecond();
	// private Timer timer;

	public void contextInitialized(ServletContextEvent sce) {
		logger.info("应用程序初始化开始……");
		TimeZone tz = TimeZone.getTimeZone("GMT+8");
		TimeZone.setDefault(tz);
		// new AutoSynchThread().start();
		// new NioReadBigTxt().start();

		// 间隔时长
		// long daySpan = 24 * 60 * 60 * 1000;
		// final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '" +
		// synchHour + ":" + synchMinute + ":" + synchSecond + "'");
		// // 首次运行时间
		// Date startTime = null;
		// try {
		// startTime = new SimpleDateFormat("yyyy-MM-dd
		// HH:mm:ss").parse(sdf.format(new Date()));
		// } catch (ParseException e) {
		// logger.error("启动失败，请重启项目", e);
		// }
		// if(System.currentTimeMillis() > startTime.getTime()){
		// startTime = new Date(startTime.getTime() + daySpan);
		// }
		//
		// timer = new Timer();
		// timer.schedule(new SynchTimerTask(), startTime, daySpan);
		new StartThread().start();
	}

	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("应用程序注销……");
	}

	public void sessionCreated(HttpSessionEvent arg0) {
		logger.info("Session创建");
	}

	public void sessionDestroyed(HttpSessionEvent arg0) {
		logger.info("Session销毁");
	}

}
