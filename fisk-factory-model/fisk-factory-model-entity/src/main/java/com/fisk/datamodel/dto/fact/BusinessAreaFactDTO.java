package com.fisk.datamodel.dto.fact;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BusinessAreaFactDTO {


    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    public long id;

    /**
     * 业务域名称
     */
    @ApiModelProperty(value = "业务域名称")
    public String businessName;

    public List<FactListDTO> factList;

}
