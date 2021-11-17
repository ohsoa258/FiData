package com.fisk.chartvisual.contentSplit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/17 16:27
 */
public class Listslicer {
    private int id;
    private int logx;
    private int logy;
    private int height;
    private int width;
    private Lift lift;
    @JsonProperty("pageSwitch")
    private boolean pageswitch;
    @JsonProperty("pageNum")
    private int pagenum;
    @JsonProperty("pageSize")
    private int pagesize;
    private List<String> cities;
    private List<String> value;
    @JsonProperty("isActive")
    private boolean isactive;
    @JsonProperty("likeValue")
    private String likevalue;
    private boolean linkage;
    private int z;
    @JsonProperty("X")
    private int x;
    @JsonProperty("Y")
    private int y;
    private int w;
    private int h;
    @JsonProperty("curWScale")
    private double curwscale;
    @JsonProperty("curHScale")
    private double curhscale;
    @JsonProperty("curSelfType")
    private String curselftype;
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setLogx(int logx) {
        this.logx = logx;
    }
    public int getLogx() {
        return logx;
    }

    public void setLogy(int logy) {
        this.logy = logy;
    }
    public int getLogy() {
        return logy;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    public int getWidth() {
        return width;
    }

    public void setLift(Lift lift) {
        this.lift = lift;
    }
    public Lift getLift() {
        return lift;
    }

    public void setPageswitch(boolean pageswitch) {
        this.pageswitch = pageswitch;
    }
    public boolean getPageswitch() {
        return pageswitch;
    }

    public void setPagenum(int pagenum) {
        this.pagenum = pagenum;
    }
    public int getPagenum() {
        return pagenum;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }
    public int getPagesize() {
        return pagesize;
    }

    public void setCities(List<String> cities) {
        this.cities = cities;
    }
    public List<String> getCities() {
        return cities;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }
    public List<String> getValue() {
        return value;
    }

    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }
    public boolean getIsactive() {
        return isactive;
    }

    public void setLikevalue(String likevalue) {
        this.likevalue = likevalue;
    }
    public String getLikevalue() {
        return likevalue;
    }

    public void setLinkage(boolean linkage) {
        this.linkage = linkage;
    }
    public boolean getLinkage() {
        return linkage;
    }

    public void setZ(int z) {
        this.z = z;
    }
    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }
    public int getX() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }
    public int getY() {
        return y;
    }

    public void setW(int w) {
        this.w = w;
    }
    public int getW() {
        return w;
    }

    public void setH(int h) {
        this.h = h;
    }
    public int getH() {
        return h;
    }

    public void setCurwscale(double curwscale) {
        this.curwscale = curwscale;
    }
    public double getCurwscale() {
        return curwscale;
    }

    public void setCurhscale(double curhscale) {
        this.curhscale = curhscale;
    }
    public double getCurhscale() {
        return curhscale;
    }

    public void setCurselftype(String curselftype) {
        this.curselftype = curselftype;
    }
    public String getCurselftype() {
        return curselftype;
    }
}
