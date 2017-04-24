package com.yijia.bean;

public class Page {

    private int curentPage = 1;// 当前页
    private int pageSize = 20;// 每页的记录数
    private long rowTotal;// 总记录数
    @SuppressWarnings("unused")
    private int firstRow = 0;// 本页起始记录
    @SuppressWarnings("unused")
    private long pageNum;// 总页数

    public String getShowPages() {
        StringBuffer buffer = new StringBuffer();
        
//        buffer.append("<div class='dataTables_info'>显示 " + getFirstRow() + " 到" + endNum + "，共" + rowTotal + " 条</div>");
        buffer.append("<div class='dataTables_info'>共" + rowTotal + " 条</div>");
        buffer.append("<div class='dataTables_paginate paging_simple_numbers'>");
        buffer.append("<a class='paginate_button' href='javascript:pageNext(1);'>首页</a>");
        if (curentPage == 1) {
            buffer.append("<a class='paginate_button previous disabled'>上一页</a>");
        } else {
            buffer.append("<a class='paginate_button previous' href='javascript:pageNext(" + (curentPage - 1) + ");'>上一页</a>");
        }

        if(getPageNum() <= 5){
            for (int i = 1; i <= getPageNum(); i++) {
                buffer.append("<span>");
                if (i == curentPage) {
                    buffer.append("<a href='javascript:pageNext(" + i + ");' class='paginate_button current' >" + i + "</a>");
                } else {
                    buffer.append("<a href='javascript:pageNext(" + i + ");' class='paginate_button' >" + i + "</a>");
                }
                buffer.append("</span>");
            }
        }else{
            if (1 == curentPage) {
                buffer.append("<input id='searchPage' style='width:32px; height:28px; margin-left:5px; margin-top:-2px;' value='" + curentPage + "' onblur='pageNext();'>");
            } else {
                buffer.append("<a href='javascript:pageNext(1);' class='paginate_button' >1</a>");
            }
            if (2 == curentPage) {
                buffer.append("<input id='searchPage' style='width:32px; height:28px; margin-left:5px; margin-top:-2px;' value='" + curentPage + "' onblur='pageNext();'>");
            } else {
                buffer.append("<a href='javascript:pageNext(2);' class='paginate_button' >2</a>");
            }
            if(1 == curentPage || 2 == curentPage || (getPageNum() - 1) == curentPage || getPageNum() == curentPage){
                buffer.append("<a href='javascript:void(0);' class='paginate_button' >...</a>");
                buffer.append("<a href='javascript:void(0);' class='paginate_button' >...</a>");
            }else{
                buffer.append("<input id='searchPage' style='width:32px; height:28px; margin-left:5px; margin-top:-2px;' value='" + curentPage + "' onblur='pageNext();'>");
            }
            if ((getPageNum() - 1) == curentPage) {
                buffer.append("<input id='searchPage' style='width:32px; height:28px; margin-left:5px; margin-top:-2px;' value='" + curentPage + "' onblur='pageNext();'>");
            } else {
                buffer.append("<a href='javascript:pageNext(" + (getPageNum() - 1) + ");' class='paginate_button' >" + (getPageNum() - 1) + "</a>");
            }
            if (getPageNum() == curentPage) {
                buffer.append("<input id='searchPage' style='width:32px; height:28px; margin-left:5px; margin-top:-2px;' value='" + curentPage + "' onblur='pageNext();'>");
            } else {
                buffer.append("<a href='javascript:pageNext(" + getPageNum() + ");' class='paginate_button' >" + getPageNum() + "</a>");
            }
        }
        if (curentPage == getPageNum()) {
            buffer.append("<a class='paginate_button next disabled' href='javascript:void(0);'>下一页</a>");
        } else {
            buffer.append("<a class='paginate_button next' href='javascript:pageNext(" + (curentPage + 1) + ");'>下一页</a> ");
        }

        buffer.append("<a class='paginate_button' href='javascript:pageNext(" + getPageNum() + ");'>末页</a>");

        return buffer.toString();
    }

    public int getCurentPage() {
        return curentPage;
    }

    /** 当前页 */
    public void setCurentPage(int curentPage) {
        this.curentPage = curentPage;

    }

    public int getPageSize() {
        return pageSize;
    }

    /** 每页的记录数 */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getFirstRow() {
        if(curentPage > getPageNum()){
            curentPage = new Long(getPageNum()).intValue();
        }
        return (curentPage - 1) * pageSize;
    }

    /** 设置开始页数 */
    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    /** 总页数 */
    public Long getPageNum() {
        if (rowTotal % pageSize > 0) {
            return (rowTotal / pageSize) + 1;
        } else {
            return rowTotal / pageSize;
        }
    }

    /** 总页数 */
    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    /** 总记录数 */
    public long getRowTotal() {
        return rowTotal;
    }

    /** 总记录数 */
    public void setRowTotal(long rowTotal) {
        this.rowTotal = rowTotal;
    }

}
