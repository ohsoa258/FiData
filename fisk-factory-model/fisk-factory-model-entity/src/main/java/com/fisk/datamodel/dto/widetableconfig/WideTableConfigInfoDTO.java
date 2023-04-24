package com.fisk.datamodel.dto.widetableconfig;

import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceTableConfigDTO;
import com.fisk.datamodel.dto.widetablerelationconfig.WideTableRelationConfigDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableConfigInfoDTO {

    @ApiModelProperty(value = "页面大小")
    public int pageSize;

    @ApiModelProperty(value = "实体")
    public List<TableSourceTableConfigDTO> entity;

    @ApiModelProperty(value = "关系")
    public List<WideTableRelationConfigDTO> relations;

}
