package com.fisk.dataaccess.dto.apiresultconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ApiResultConfigDTO {
    /**
     * app数据源id
     */
    @ApiModelProperty(value = "app数据源id")
    public Integer appDatasourceId;
    /**
     * 节点名称
     */
    @ApiModelProperty(value = "节点名称")
    public String name;
    /**
     * 父级名称
     */
    @ApiModelProperty(value = "父级名称")
    public String parent;
    /**
     * 是否选中
     */
    @ApiModelProperty(value = "是否选中")
    public Boolean checked;

}
