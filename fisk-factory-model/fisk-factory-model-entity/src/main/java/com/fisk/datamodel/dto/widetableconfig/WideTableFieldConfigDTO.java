package com.fisk.datamodel.dto.widetableconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableFieldConfigDTO {

    public int pageSize;

    /**
     * 所有表信息: entity.get(0)一定是主表
     */
    public List<WideTableSourceTableConfigDTO> entity;

    /**
     * 连线关系
     */
    public List<WideTableSourceRelationsDTO> relations;

    @ApiModelProperty(value = "当前操作的事实表名")
    public String factTableName;

    @ApiModelProperty(value = "事实表数据的来源sql")
    public String factTableSourceSql;
}
