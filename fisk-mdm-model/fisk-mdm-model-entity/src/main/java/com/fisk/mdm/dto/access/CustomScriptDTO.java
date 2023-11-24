package com.fisk.mdm.dto.access;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author jianwenyang
 */
@Data
public class CustomScriptDTO {

    public Integer id;

    /**
     * 表id
     */
    @ApiModelProperty(value = "表id")
    public Integer tableId;

    /**
     * 执行顺序
     */
    @ApiModelProperty(value = "执行顺序")
    public Integer sequence;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    public String name;

    /**
     * 脚本
     */
    @ApiModelProperty(value = "脚本")
    public String script;

    /**
     * 执行类型:1stg 2 ods
     */
    @ApiModelProperty(value = "执行类型:1stg 2 ods")
    public Integer execType;

}
