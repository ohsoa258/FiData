package com.fisk.chartvisual.dto.contentsplit;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author WangYan
 * @date 2021/11/17 16:26
 */
public class Columnname {
    private String name;
    @JsonProperty("dragElemType")
    private String dragelemtype;
    private int id;
    private String dimension;
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
