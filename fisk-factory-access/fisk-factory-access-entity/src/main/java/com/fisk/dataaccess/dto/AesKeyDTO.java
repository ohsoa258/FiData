package com.fisk.dataaccess.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class AesKeyDTO {

    /**
     * apiCode
     */
    @ApiModelProperty(value = "apiCode")
    public String apiCode;

}
