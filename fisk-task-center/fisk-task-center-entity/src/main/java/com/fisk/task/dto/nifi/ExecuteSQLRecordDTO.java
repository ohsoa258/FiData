package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExecuteSQLRecordDTO  extends BaseProcessorDTO{
    public String databaseConnectionPoolingService;
    public String sqlSelectQuery;
    public String recordwriter;
    public String maxRowsPerFlowFile;
    public String outputBatchSize;
    public String FetchSize;

}
