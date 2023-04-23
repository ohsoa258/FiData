package com.fisk.mdm.dto.complextype;

import com.fisk.mdm.enums.DataTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-07-11 16:16
 */
@Data
public class ComplexTypeDetailsParameterDTO {

    @ApiModelProperty(value = "唯一编码")
    private String code;
    @ApiModelProperty(value = "数据类型枚举")
    private DataTypeEnum dataTypeEnum;

}
