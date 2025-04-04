package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BuildCallDbProcedureProcessorDTO extends BaseProcessorDTO {
    @ApiModelProperty(value = "数据库连接id")
    public String dbConnectionId;
    @ApiModelProperty(value = "执行sql")
    public String executsql;

    /*
     * 是否有下一个组件
     * */
    @ApiModelProperty(value = "是否有下一个组件")
    public Boolean haveNextOne;
    /**
     * SQL Pre-Query
     */
    @ApiModelProperty(value = "SQL Pre-Query")
    public String sqlPreQuery;

    /**
     * sql-post-query
     */
    @ApiModelProperty(value = "sql-post-query")
    public String sqlPostQuery;

    /**
     * nifi组件并发数量（目前只给数接/数仓的查询组件开启并发）
     */
    @ApiModelProperty(value = "nifi组件并发数量")
    public Integer concurrencyNums;
}
