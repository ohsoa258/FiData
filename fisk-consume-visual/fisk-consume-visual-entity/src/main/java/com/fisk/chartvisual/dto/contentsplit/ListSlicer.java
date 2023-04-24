package com.fisk.chartvisual.dto.contentsplit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/17 16:27
 */
@Data
public class ListSlicer {

    @ApiModelProperty(value = "id")
    private int id;
    @ApiModelProperty(value = "logx")
    private int logx;

    @ApiModelProperty(value = "logy")
    private int logy;

    @ApiModelProperty(value = "高")
    private int height;

    @ApiModelProperty(value = "宽")
    private int width;

    @ApiModelProperty(value = "lift")
    private Lift lift;

    @ApiModelProperty(value = "页面开关")
    @JsonProperty("pageSwitch")
    private boolean pageswitch;

    @ApiModelProperty(value = "页面数")
    @JsonProperty("pageNum")
    private int pagenum;

    @ApiModelProperty(value = "页面大小")
    @JsonProperty("pageSize")
    private int pagesize;

    @ApiModelProperty(value = "城市")
    private List<String> cities;

    @ApiModelProperty(value = "值")
    private List<String> value;

    @ApiModelProperty(value = "是否活跃")
    @JsonProperty("isActive")
    private boolean isactive;
    @ApiModelProperty(value = "相似值")
    @JsonProperty("likeValue")
    private String likevalue;

    @ApiModelProperty(value = "联系")
    private boolean linkage;

    @ApiModelProperty(value = "z")
    private int z;
    @ApiModelProperty(value = "x")
    @JsonProperty("X")
    private int x;

    @ApiModelProperty(value = "y")
    @JsonProperty("Y")
    private int y;

    @ApiModelProperty(value = "w")
    private int w;

    @ApiModelProperty(value = "h")
    private int h;

    @ApiModelProperty(value = "curwscale")
    @JsonProperty("curWScale")
    private double curwscale;

    @ApiModelProperty(value = "curhscale")
    @JsonProperty("curHScale")
    private double curhscale;

    @ApiModelProperty(value = "curselftype")
    @JsonProperty("curSelfType")
    private String curselftype;
}
