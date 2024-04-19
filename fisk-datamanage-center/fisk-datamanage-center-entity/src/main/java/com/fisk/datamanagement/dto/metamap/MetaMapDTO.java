package com.fisk.datamanagement.dto.metamap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class MetaMapDTO {

    /**
     * 数据库id/业务域id
     */
    @ApiModelProperty(value = "数据库id/业务域id")
    private Integer dbOrAreaId;

    /**
     * 数据库/业务域名称
     */
    @ApiModelProperty(value = "数据库/业务域名称")
    private String dbOrAreaName;


    /**
     * 应用list/业务过程list
     */
    @ApiModelProperty(value = "应用list/业务过程list")
    private List<MetaMapAppDTO> appOrPorcessList;



}
