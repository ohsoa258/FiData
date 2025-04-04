package com.fisk.datamodel.dto.customscript;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CustomScriptDTO {

    @ApiModelProperty(value = "id")
    public Integer id;

    /**
     * 表类型:1维度 2事实
     */
    @ApiModelProperty(value = "表类型:1维度 2事实")
    public Integer type;

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
