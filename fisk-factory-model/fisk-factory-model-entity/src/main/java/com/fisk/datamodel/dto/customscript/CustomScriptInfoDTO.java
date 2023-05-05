package com.fisk.datamodel.dto.customscript;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class CustomScriptInfoDTO extends CustomScriptDTO {

    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "创建者")
    public String createUser;

}
