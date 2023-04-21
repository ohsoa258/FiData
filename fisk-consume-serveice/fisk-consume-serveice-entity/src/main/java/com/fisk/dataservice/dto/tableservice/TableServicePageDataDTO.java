package com.fisk.dataservice.dto.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableServicePageDataDTO {

    @ApiModelProperty(value = "id")
    public Integer id;

    /**
     * 显示名称
     */
    @ApiModelProperty(value = "显示名称")
    public String displayName;

    @ApiModelProperty(value = "发布")
    public Integer publish;

}
