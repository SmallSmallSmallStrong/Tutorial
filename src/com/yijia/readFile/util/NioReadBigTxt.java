package com.yijia.readFile.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yijia.elasticsearch.SynchUtil;
import com.yijia.elasticsearch.TypeJDBCES;
import com.yijia.elasticsearch.bean.ESBeanConfig;
import com.yijia.readFile.bean.FilePathBeanConfig;
import com.yijia.util.Tool;

/**
 * 使用nio读取大文件，并存入ES
 * 
 * @author zhqy
 *
 */
public class NioReadBigTxt extends Thread{

    private List<String> columnNameList = new ArrayList<String>();
    private List<String> typeNameList = new ArrayList<String>();

//public static void main(String[] args) {
//  NioReadBigTxt a = new NioReadBigTxt();
//a.read();
//    }
    
    @Override
    public void run() {
        read();
    }
    
    /**
     * 初始化所需参数
     */
    private void init(){
        
        columnNameList.add("field_zh");
        columnNameList.add("field_mm");
        
        typeNameList.add(TypeJDBCES.VARCHAR.getJdbc());
        typeNameList.add(TypeJDBCES.VARCHAR.getJdbc());
        
    }
    
    public void read(){
        init();
        readDirectory();
    }

    /**
     * 根据文件夹获取下面所有的文件
     */
    public void readDirectory() {
        File file = new File(FilePathBeanConfig.getBean().getPath());
        File[] tempList = file.listFiles();
        if(tempList != null && tempList.length > 0){
            for (int i = 0; i < tempList.length; i++) {
                if (tempList[i].isFile()) {
                    readFile(tempList[i].getPath());
                }
                if (tempList[i].isDirectory()) {
                }
            }
        }
    }

    private void readFile(String filePath) {

        FileChannel fcin = null;

        try {
            int bufSize = Tool.sToI(FilePathBeanConfig.getBean().getBuffSize(), 1000);// 一次读取的字节长度
            File fin = new File(filePath);// 读取的文件
            Date startDate = new Date();
            fcin = new RandomAccessFile(fin, "r").getChannel();
            ByteBuffer rBuffer = ByteBuffer.allocate(bufSize);

            readFileByLine(bufSize, fcin, rBuffer);
            Date endDate = new Date();

            System.out.print(startDate + "|" + endDate);// 测试执行时间
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fcin != null && fcin.isOpen()) {
                try {
                    fcin.close();
                } catch (IOException e) {
                }
            }
            SynchUtil.closeClient();
        }
    }

    private void readFileByLine(int bufSize, FileChannel fcin, ByteBuffer rBuffer) {
        List<String> dataList = new ArrayList<String>();// 存储读取的每行数据
        byte[] lineByte = new byte[0];

        // String encode = "GBK";
        String encode = FilePathBeanConfig.getBean().getEncode();
        try {
            // temp：由于是按固定字节读取，在一次读取中，第一行和最后一行经常是不完整的行，因此定义此变量来存储上次的最后一行和这次的第一行的内容，
            // 并将之连接成完成的一行，否则会出现汉字被拆分成2个字节，并被提前转换成字符串而乱码的问题
            byte[] temp = new byte[0];
            while (fcin.read(rBuffer) != -1) {// fcin.read(rBuffer)：从文件管道读取内容到缓冲区(rBuffer)
                int rSize = rBuffer.position();// 读取结束后的位置，相当于读取的长度
                byte[] bs = new byte[rSize];// 用来存放读取的内容的数组
                rBuffer.rewind();// 将position设回0,所以你可以重读Buffer中的所有数据,此处如果不设置,无法使用下面的get方法
                rBuffer.get(bs);// 相当于rBuffer.get(bs,0,bs.length())：从position初始位置开始相对读,读bs.length个byte,并写入bs[0]到bs[bs.length-1]的区域
                rBuffer.clear();

                int startNum = 0;
                int LF = 10;// 换行符
                int CR = 13;// 回车符
                boolean hasLF = false;// 是否有换行符
                for (int i = 0; i < rSize; i++) {
                    if (bs[i] == LF) {
                        hasLF = true;
                        int tempNum = temp.length;
                        int lineNum = i - startNum;
                        lineByte = new byte[tempNum + lineNum];// 数组大小已经去掉换行符

                        System.arraycopy(temp, 0, lineByte, 0, tempNum);// 填充了lineByte[0]~lineByte[tempNum-1]
                        temp = new byte[0];
                        System.arraycopy(bs, startNum, lineByte, tempNum, lineNum);// 填充lineByte[tempNum]~lineByte[tempNum+lineNum-1]

                        String line = new String(lineByte, 0, lineByte.length, encode);// 一行完整的字符串(过滤了换行和回车)
                        dataList.add(line);
                        writeESByLine(line);

                        // 过滤回车符和换行符
                        if (i + 1 < rSize && bs[i + 1] == CR) {
                            startNum = i + 2;
                        } else {
                            startNum = i + 1;
                        }

                    }
                }
                if (hasLF) {
                    temp = new byte[bs.length - startNum];
                    System.arraycopy(bs, startNum, temp, 0, temp.length);
                } else {// 兼容单次读取的内容不足一行的情况
                    byte[] toTemp = new byte[temp.length + bs.length];
                    System.arraycopy(temp, 0, toTemp, 0, temp.length);
                    System.arraycopy(bs, 0, toTemp, temp.length, bs.length);
                    temp = toTemp;
                }
            }
            if (temp != null && temp.length > 0) {// 兼容文件最后一行没有换行的情况
                String line = new String(temp, 0, temp.length, encode);
                dataList.add(line);
                writeESByLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写到ES里
     * 
     * @param fcout
     * @param wBuffer
     * @param line
     */
    private void writeESByLine(String line) {
        if(Tool.isEmpty(line)){
           return; 
        }
        String[] strs = line.split("----");
        if(strs == null || strs.length != 2){
            return;
        }
        
        Map<String, Object> colMap = new HashMap<String, Object>();
        colMap.put(columnNameList.get(0), strs[0]);
        colMap.put(columnNameList.get(1), strs[1]);
        String indexName = ESBeanConfig.getESBean().getIndexName();
        String typeName = ESBeanConfig.getESBean().getStationtoen();
        SynchUtil.indexDocument(indexName, typeName, columnNameList, typeNameList, colMap);
    }
}
