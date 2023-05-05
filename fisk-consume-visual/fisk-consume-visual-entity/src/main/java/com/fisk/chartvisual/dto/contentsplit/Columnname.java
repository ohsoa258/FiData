package com.fisk.chartvisual.dto.contentsplit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author WangYan
 * @date 2021/11/17 16:26
 */
public class Columnname {

    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "dragelemtype")
    @JsonProperty("dragElemType")
    private String dragelemtype;

    @ApiModelProperty(value = "id")
    private int id;

    @ApiModelProperty(value = "维度")
    private String dimension;

    @ApiModelProperty(value = "curkey")
    @JsonProperty("curKey")
    private int curkey;


    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setDragelemtype(String dragelemtype) {
        this.dragelemtype = dragelemtype;
    }
    public String getDragelemtype() {
        return dragelemtype;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }
    public String getDimension() {
        return dimension;
    }

    public void setCurkey(int curkey) {
        this.curkey = curkey;
    }
    public int getCurkey() {
        return curkey;
    }
}
