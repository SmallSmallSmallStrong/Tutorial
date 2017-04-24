//package com.yijia.servlet;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.io.UnsupportedEncodingException;
//
//import javax.servlet.Servlet;
//import javax.servlet.ServletConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletInputStream;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.log4j.Logger;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.yijia.bean.YiJiaBeanConfig;
//import com.yijia.elasticsearch.bean.ESBeanConfig;
//import com.yijia.elasticsearch.exception.ElasticsearchInitException;
//import com.yijia.jdbc.ConnectServer;
//import com.yijia.jdbc.ConnectServerConfig;
//import com.yijia.jdbc.exception.JDBCInitException;
//import com.yijia.thread.AutoSynchThread;
//import com.yijia.thread.ThreadFlag;
//import com.yijia.thread.ThreadFlagConfig;
//import com.yijia.util.Tool;
//
///**
// * Servlet implementation class DataSynch
// */
//@WebServlet(description = "数据同步", urlPatterns = { "/DataSynch" })
//public class DataSynch extends HttpServlet {
//    
//	private static final long serialVersionUID = 1L;
//
//    private Logger logger = Logger.getLogger(DataSynch.class);
//    private ConnectServer connserver;
//    
//    /**
//     * Default constructor. 
//     */
//    public DataSynch() {
//    }
//
//	/**
//	 * @see Servlet#init(ServletConfig)
//	 */
//	public void init(ServletConfig config) throws ServletException {
//	    connserver = ConnectServerConfig.initConnectServer();
//	}
//
//	/**
//	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
//	 */
//	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        printHtml(request, response, "请以post方式访问。");
//	}
//	
//	@Override
//	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//	    jsonMode(request, response);
//	}
//	
//	/**
//	 * 以parameter的方式获取值
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	public void parameterMode(HttpServletRequest request, HttpServletResponse response){
//        String username = request.getParameter("username");
//        String password = request.getParameter("password");
//        String mode = request.getParameter("mode");
//        login(request, response, username, password, mode);
//    }
//	
//	/**
//	 * 以json的方式获取值
//	 * 
//	 * @param request
//	 * @param response
//	 */
//	public void jsonMode(HttpServletRequest request, HttpServletResponse response){
//	    JSONObject json = result2json(request, response);
//        String username = null;
//        try {
//            username = json.getString("username");
//        } catch (Exception e) {
//            String msg = "请输入用户名。";
//            logger.info(msg);
//            printHtml(request, response, msg);
//            return;
//        }
//        String password = null;
//        try {
//            password = json.getString("password");
//        } catch (Exception e) {
//            String msg = "请输入密码。";
//            logger.info(msg);
//            printHtml(request, response, msg);
//            return;
//        }
//        String mode = null;
//        try {
//            mode = json.getString("mode");
//        } catch (Exception e) {
//            String msg = "请输入mode。";
//            logger.info(msg);
//            printHtml(request, response, msg);
//            return;
//        }
//        login(request, response, username, password, mode);
//	}
//    
//    /**
//     * 将request用utf-8的编码转为json
//     * 
//     * @param request
//     * @param response
//     * @return
//     */
//    public JSONObject result2json(HttpServletRequest request, HttpServletResponse response){
//        JSONObject json = null;
//        try {
//            request.setCharacterEncoding("UTF-8");
//            response.setContentType("text/html;charset=UTF-8");
//            String acceptjson = "";  
//            BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));  
//            StringBuffer sb = new StringBuffer("");
//            String temp;
//            while ((temp = br.readLine()) != null) {
//                sb.append(temp);
//            }
//            br.close();
//            acceptjson = sb.toString();
//            if (!Tool.isEmpty(acceptjson)) {
//                json = new JSONObject(acceptjson);
//            }
//        } catch (UnsupportedEncodingException e) {
//            logger.info("设置编码方式失败。", e);
//        } catch (JSONException e) {
//            logger.info("字符串转json失败。", e);
//        } catch (IOException e) {
//            logger.info("获取数据失败。", e);
//        }
//        return json;
//    }
//	
//	/**
//	 * 登陆认证，成功则开始同步数据
//	 * 
//	 * @param username
//	 * @param password
//	 */
//	public void login(HttpServletRequest request, HttpServletResponse response, String username, String password, String mode){
//        if(!YiJiaBeanConfig.getYiJiaBean().getUsername().equals(username)
//                || !YiJiaBeanConfig.getYiJiaBean().getPassword().equals(password)){
//            String msg = "登陆失败，请重试。";
//            logger.info("用户名：" + username + "  密码：" + password + " 登陆失败。");
//            printHtml(request, response, msg);
//        }else{
//            logger.info("用户：" + username + " 登陆成功。");
//            synch(request, response, mode);
//        }
//	}
//	
//	/**
//	 * 同步数据
//     * 
//	 * @param request
//	 * @param response
//	 */
//	public void synch(HttpServletRequest request, HttpServletResponse response, String mode){
//	    String msg;
//        if ("zhudong".equals(mode)) {
//            logger.info("进入主动获取模式");
//            
//            try {
//                status();
//            } catch (JDBCInitException e) {
//                msg = e.getMessage();
//                printHtml(request, response, msg);
//                return;
//            } catch (ElasticsearchInitException e) {
//                msg = e.getMessage();
//                printHtml(request, response, msg);
//                return;
//            }
//
//            ThreadFlag canChange = ThreadFlagConfig.getThreadFlag();
//            if(false == canChange.getCanChange()){
//                msg = "正在同步数据，请稍后。";
//            }else{
//                new AutoSynchThread(AutoSynchThread.HAND).start();
//                msg = "已进行后台同步数据，请稍后。";
//            }
//            logger.info(msg);
//            printHtml(request, response, msg);
//        }
//	}
//	
//	/**
//	 * 判断链接是否正常
//	 * 
//	 * @throws JDBCInitException
//	 */
//	private void status() throws JDBCInitException{
//
//        if(false == connserver.connStatus()){
//            throw new JDBCInitException("【" + ESBeanConfig.getESBean().getStation() + "】 数据服务器链接失败，请联系管理员。");
//        }
//        
//        if(false == connserver.esStatus()){
//            throw new ElasticsearchInitException("【" + ESBeanConfig.getESBean().getStation() + "】 es数据服务未启动，请联系管理员。");
//        }
//	}
//	
//	/**
//	 * 将结果输出
//	 * 
//	 * @param request
//	 * @param response
//	 * @param msg
//	 */
//	public void printHtml(HttpServletRequest request, HttpServletResponse response, String msg){
//        try {
//            response.setContentType("text/html;charset=utf-8");
//            request.setCharacterEncoding("utf-8");
//            PrintWriter out = response.getWriter();
//            out.println(msg);
//            out.flush();
//            out.close();
//        } catch (UnsupportedEncodingException e) {
//            logger.info("设置编码方式失败。", e);
//        } catch (IOException e) {
//            logger.info("获取输出流失败。", e);
//        }
//	}
//
//    /**
//     * @see Servlet#destroy()
//     */
//    public void destroy() {
//        ConnectServerConfig.close();
//    }
//
//}
