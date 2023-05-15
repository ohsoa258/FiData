package com.fisk.task.dto.nifi;

import com.fisk.common.core.enums.task.SynchronousTypeEnum;
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
    public SynchronousTypeEnum synchronousTypeEnum;
    public String schemaName;
    /**
     * 是否严格按照字段名称添加属性,实际上是bool类型
     */
    public String putDbRecordTranslateFieldNames;
    /**
     * 是否给表名加双引号 pgsql区分大小写需要
     */
    public boolean putDbRecordQuotedTableIdentifiers;

}
