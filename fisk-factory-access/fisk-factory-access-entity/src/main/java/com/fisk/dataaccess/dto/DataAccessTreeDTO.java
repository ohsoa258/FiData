package com.fisk.dataaccess.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * <p>
 *     应用注册树
 * </p>
 * @author Lock
 */
@Data
public class DataAccessTreeDTO {

    /**
     * id
     */
    public long id;
    /**
     * 应用名称
     */
    public String appName;

    /**
     * 应用注册下的物理表
     */
    public List<TableNameTreeDTO> list;

    /**
     * 1: 数据接入; 2:数据建模
     */
    @ApiModelProperty(value = "1: 数据接入; 2:数据建模", required = true)
    public int flag;

    /**
     * 0:实时  1:非实时
     */
    @ApiModelProperty(value = "应用类型(0:实时  1:非实时)", required = true)
    public int appType;
}
