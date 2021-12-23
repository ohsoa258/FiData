package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lock
 */
@Data
public class KeywordDTO{

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    public long id;

    /**
     * SQL关键词
     */
    @ApiModelProperty(value = "SQL关键词",required = true)
    public String keyword;

    /**
     * SQL关键词类型(1:mysql  2:sqlserver  3:pgsql  4:doris)
     */
    @ApiModelProperty(value = "SQL关键词类型(1:mysql  2:sqlserver  3:pgsql  4:doris",required = true)
    public long keywordType;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    public String describe;
}
