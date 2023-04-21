package com.fisk.datamodel.dto.widetableconfig;

import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceTableConfigDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableAliasDTO {

    @ApiModelProperty(value = "sql")
    public String sql;

    /**
     * 返回带前缀表名sql
     */
    @ApiModelProperty(value = "返回带前缀表名sql")
    public String preSql;

    @ApiModelProperty(value = "实体")
    public List<TableSourceTableConfigDTO> entity;


}
