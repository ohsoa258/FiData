package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExecuteSQLRecordDTO extends BaseProcessorDTO {

    @ApiModelProperty(value = "数据库连接池服务")
    public String databaseConnectionPoolingService;
    @ApiModelProperty(value = "sql 选择查询")
    public String sqlSelectQuery;
    @ApiModelProperty(value = "记录写入器")
    public String recordwriter;

    @ApiModelProperty(value = "每个流文件的最大行数")
    public String maxRowsPerFlowFile;
    @ApiModelProperty(value = "输出批量大小")
    public String outputBatchSize;

    @ApiModelProperty(value = "取大小")
    public String FetchSize;
    /**
     * esql-auto-commit
     */
    @ApiModelProperty(value = "Esql自动提交")
    public String esqlAutoCommit;

}
