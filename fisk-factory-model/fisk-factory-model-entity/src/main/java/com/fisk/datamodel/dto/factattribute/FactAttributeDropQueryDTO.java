package com.fisk.datamodel.dto.factattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDropQueryDTO {
    @ApiModelProperty(value = "id")
    public int id;
    /**
     * 查询事实字段类型
     */
    @ApiModelProperty(value = "查询事实字段类型")
    public List<Integer> type;
}
