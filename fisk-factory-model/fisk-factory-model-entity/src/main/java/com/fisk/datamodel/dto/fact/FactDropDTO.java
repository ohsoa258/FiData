package com.fisk.datamodel.dto.fact;

import com.fisk.datamodel.dto.factattribute.FactAttributeDropDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactDropDTO {
    @ApiModelProperty(value = "id")
    public int id;

    @ApiModelProperty(value = "事实表英文名称")
    public String factTableEnName;
    /**
     * 事实表字段列表
     */
    @ApiModelProperty(value = "事实表字段列表")
    public List<FactAttributeDropDTO> list;
}
