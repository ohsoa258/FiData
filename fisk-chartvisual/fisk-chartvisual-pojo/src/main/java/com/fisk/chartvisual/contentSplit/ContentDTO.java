package com.fisk.chartvisual.contentSplit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/16 13:56
 */
public class ContentDTO {
    @JsonProperty("bdColor")
    private String bdcolor;
    private int height;
    private int width;
    @JsonProperty("selfAdaptWidth")
    private boolean selfadaptwidth;
    @JsonProperty("selfAdaptHeight")
    private boolean selfadaptheight;
    private int bigid;
    @JsonProperty("listSlicer")
    private List<Listslicer> listslicer;
    @JsonProperty("bigTyple")
    private int bigtyple;
    public void setBdcolor(String bdcolor) {
        this.bdcolor = bdcolor;
    }
    public String getBdcolor() {
        return bdcolor;
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

    public void setSelfadaptwidth(boolean selfadaptwidth) {
        this.selfadaptwidth = selfadaptwidth;
    }
    public boolean getSelfadaptwidth() {
        return selfadaptwidth;
    }

    public void setSelfadaptheight(boolean selfadaptheight) {
        this.selfadaptheight = selfadaptheight;
    }
    public boolean getSelfadaptheight() {
        return selfadaptheight;
    }

    public void setBigid(int bigid) {
        this.bigid = bigid;
    }
    public int getBigid() {
        return bigid;
    }

    public void setListslicer(List<Listslicer> listslicer) {
        this.listslicer = listslicer;
    }
    public List<Listslicer> getListslicer() {
        return listslicer;
    }

    public void setBigtyple(int bigtyple) {
        this.bigtyple = bigtyple;
    }
    public int getBigtyple() {
        return bigtyple;
    }
}
