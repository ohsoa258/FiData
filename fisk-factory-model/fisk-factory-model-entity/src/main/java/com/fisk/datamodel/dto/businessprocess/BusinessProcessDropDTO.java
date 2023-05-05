package com.fisk.datamodel.dto.businessprocess;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessDropDTO {

    @ApiModelProperty(value = "id")
    public int id;

    @ApiModelProperty(value = "业务流程中午名称")
    public String businessProcessCnName;
}
