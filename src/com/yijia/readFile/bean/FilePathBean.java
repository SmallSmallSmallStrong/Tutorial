package com.yijia.readFile.bean;

public class FilePathBean {

    /** 文件夹路径 */
    private String path;
    /** 编码方式 */
    private String encode;
    /** 一次读取大小 */
    private String buffSize;

    /** 文件夹路径 */
    public String getPath() {
        return path;
    }

    /** 文件夹路径 */
    public void setPath(String path) {
        this.path = path;
    }

    /** 编码方式 */
    public String getEncode() {
        return encode;
    }

    /** 编码方式 */
    public void setEncode(String encode) {
        this.encode = encode;
    }

    /** 一次读取大小 */
    public String getBuffSize() {
        return buffSize;
    }

    /** 一次读取大小 */
    public void setBuffSize(String buffSize) {
        this.buffSize = buffSize;
    }

}
