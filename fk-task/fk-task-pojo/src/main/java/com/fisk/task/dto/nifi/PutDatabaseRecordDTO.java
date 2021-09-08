package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PutDatabaseRecordDTO   extends BaseProcessorDTO{
    public String recordReader;
    public String databaseType;
    public String statementType;
    public String databaseConnectionPoolingService;
    public String TableName;
    public String concurrentTasks;

}
