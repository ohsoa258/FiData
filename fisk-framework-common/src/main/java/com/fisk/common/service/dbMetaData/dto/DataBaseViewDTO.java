package com.fisk.common.service.dbMetaData.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 1.0
 * @description 数据库视图对象
 * @date 2021/12/31 14:38
 */
@Data
public class DataBaseViewDTO {

    @ApiModelProperty(value = "视图名称，带架构名", required = true)
    public String viewName;

    @ApiModelProperty(value = "视图架构名")
    public String viewFramework;

    @ApiModelProperty(value = "视图名称，不带架构名")
    public String viewRelName;

    @ApiModelProperty(value = "1: 当前视图有效; 2: 当前视图无效")
    public int flag;

    /**
     * 视图字段
     */
    public List<TableStructureDTO> fields;
}