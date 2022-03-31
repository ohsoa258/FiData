package com.fisk.datagovernance.dto.datasecurity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 行级过滤条件配置表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-30 15:29:16
 */
@Data
public class RowfilterConfigDTO {

    @ApiModelProperty(value = "主键(修改时必传)", required = true)
    public long id;

    @ApiModelProperty(value = "tb_rowsecurity_config表id(修改时必传)", required = true)
    public long rowsecurityId;

    @ApiModelProperty(value = "字段名称", required = true)
    public String fieldName;

    @ApiModelProperty(value = "运算符:  > = < !=  like", required = true)
    public String operator;

    @ApiModelProperty(value = "查询内容", required = true)
    public String result;

    @ApiModelProperty(value = "查询的链式关系: and or")
    public String chainRelationship;
}
