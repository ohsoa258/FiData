package com.fisk.datamodel.dto.widetableconfig;

import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceTableConfigDTO;
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
    public List<TableSourceTableConfigDTO> entity;

    /**
     * 连线关系
     */
    public List<TableSourceRelationsDTO> relations;

    @ApiModelProperty(value = "当前操作的事实表名", required = true)
    public String factTableName;

    @ApiModelProperty(value = "事实表数据的来源sql", required = true)
    public String factTableSourceSql;
}
