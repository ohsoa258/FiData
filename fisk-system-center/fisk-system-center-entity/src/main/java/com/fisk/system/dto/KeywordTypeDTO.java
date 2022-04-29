package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author lock
 */
@Data
public class KeywordTypeDTO {

    /**
     * SQL关键字类型
     */
    @ApiModelProperty(value = "SQL关键字类型: (1:mysql  2:sqlserver  3:pgsql  4:doris)", required = true)
    public List<Long> keywordType;
    @NotNull
    public String field;
}
