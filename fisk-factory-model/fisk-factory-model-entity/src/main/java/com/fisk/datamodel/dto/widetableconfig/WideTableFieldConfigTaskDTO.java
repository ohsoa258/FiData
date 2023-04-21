package com.fisk.datamodel.dto.widetableconfig;

import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceTableConfigDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableFieldConfigTaskDTO extends MQBaseDTO {

    @ApiModelProperty(value = "id")
    public int id;

    @ApiModelProperty(value = "业务Id")
    public int businessId;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "sql脚本")
    public String sqlScript;

    @ApiModelProperty(value = "用户Id")
    public Long userId;

    @ApiModelProperty(value = "宽表")
    public OlapTableEnum wideTable;

    @ApiModelProperty(value = "实体")
    public List<TableSourceTableConfigDTO> entity;

    @ApiModelProperty(value = "关系")
    public List<TableSourceRelationsDTO> relations;

}
