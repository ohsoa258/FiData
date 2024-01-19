package com.fisk.datamodel.entity.mainpage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DataCountVO {

    /**
     * dim 维度表数据量
     */
    @ApiModelProperty(value = "dim 维度表数据量")
    private Long dimDataCount;

    /**
     * fact 事实表数据量
     */
    @ApiModelProperty(value = "fact 事实表数据量")
    private Long factDataCount;

    /**
     * help 帮助表数据量
     */
    @ApiModelProperty(value = "help 帮助表数据量")
    private Long helpDataCount;

    /**
     * config 配置表数据量
     */
    @ApiModelProperty(value = "config 配置表数据量")
    private Long configDataCount;

    /**
     * DWD数据量
     */
    @ApiModelProperty(value = "DWD数据量")
    private Long dwdDataCount;

    /**
     * DWS数据量
     */
    @ApiModelProperty(value = "DWS数据量")
    private Long dwsDataCount;

}
