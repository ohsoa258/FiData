package com.fisk.datamodel.dto.widetableconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class WideTableConfigDTO {

    @ApiModelProperty(value = "id")
    public Integer id;
    /**
     * 业务域id
     */
    @ApiModelProperty(value = "业务域id")
    public int businessId;
    /**
     * 宽表名称
     */
    @ApiModelProperty(value = "宽表名称")
    public String name;
    /**
     * 宽表sql脚本
     */
    @ApiModelProperty(value = "宽表sql脚本")
    public String sqlScript;
    /**
     * 配置详情
     */
    @ApiModelProperty(value = "配置详情")
    public String configDetails;

}
