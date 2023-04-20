package com.fisk.chartvisual.dto.stringinterception;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author WangYan
 * @date 2022/3/1 16:17
 */
public class CurSelectDataSourceType {

    @ApiModelProperty(value = "id")
    private int id;

    @ApiModelProperty(value = "类型")
    private String type;
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

}