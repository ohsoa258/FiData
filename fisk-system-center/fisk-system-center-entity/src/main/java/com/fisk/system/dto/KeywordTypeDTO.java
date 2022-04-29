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
    @ApiModelProperty(value = "SQL关键字类型: (1:mysql  2:sqlserver  3:pgsql  4:doris)")
    public List<Long> keywordType;
    @NotNull
    @ApiModelProperty(value = "输入的字段名称")
    public String field;
}
