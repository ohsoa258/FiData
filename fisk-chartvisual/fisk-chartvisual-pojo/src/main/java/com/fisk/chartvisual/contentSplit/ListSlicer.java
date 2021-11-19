package com.fisk.chartvisual.contentSplit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/17 16:27
 */
@Data
public class ListSlicer {
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
}
