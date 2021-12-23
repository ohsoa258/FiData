package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lock
 */
@Data
public class KeywordTypeDTO {

    /**
     * SQL关键字类型
     */
    @ApiModelProperty(value = "SQL关键字类型", required = true)
    public List<Long> keywordType;

}
