package com.fisk.dataaccess.dto.api.doc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API代码示例
 * @date 2022/2/3 14:10
 */
@Data
public class ApiResponseCodeDTO {
    /**
     * 代码code
     */
    @ApiModelProperty(value = "代码code")
    public String code;

    /**
     * 类型
     */
    @ApiModelProperty(value = "类型")
    public String type;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    public String desc;

    /**
     * 行样式
     */
    @ApiModelProperty(value = "行样式")
    public String trStyle;
}
