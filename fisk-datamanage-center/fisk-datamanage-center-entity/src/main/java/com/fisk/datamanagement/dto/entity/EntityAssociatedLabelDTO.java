package com.fisk.datamanagement.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class EntityAssociatedLabelDTO {

    @ApiModelProperty(value = "guid")
    public Integer guid;

    @ApiModelProperty(value = "列表")
    public List<Integer> list;

}
