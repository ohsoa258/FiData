package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AssignmentDTO {

    @ApiModelProperty(value = "id")
    public int id;

    @ApiModelProperty(value = "列表")
    public List<Integer> list;
}
