package com.gst.fmradio.model;

/**
 * Created by yyshang on 5/25/15.
 */
public class Channel {

    //用来放实体类，定义属性和set/get方法，与数据库中的表存在对应关系
    private int id;
    private String name;
    private int channelnum;
    //定义一个收藏col，使用int类型，使用3代表未被收藏，2代表收藏
    private int col;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChannelnum() {
        return channelnum;
    }

    public void setChannelnum(int channelnum) {
        this.channelnum = channelnum;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

}
