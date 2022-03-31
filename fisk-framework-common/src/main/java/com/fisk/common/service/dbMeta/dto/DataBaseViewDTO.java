package com.fisk.common.service.dbMeta.dto;

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

    @ApiModelProperty(value = "视图名称",required = true)
    public String viewName;
    /**
     * 视图字段
     */
    public List<TableStructureDTO> fields;
}