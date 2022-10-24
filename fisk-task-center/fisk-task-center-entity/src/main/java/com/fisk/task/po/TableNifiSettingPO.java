package com.fisk.task.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_table_nifi_setting")
public class TableNifiSettingPO extends BasePO {
    public String tableComponentId;
    public Integer appId;
    public Integer tableAccessId;
    public String dispatchComponentId;
    public String queryIncrementProcessorId;
    public String convertDataToJsonProcessorId;
    public String setIncrementProcessorId;
    public String putLogToConfigDbProcessorId;
    public String executeTargetDeleteProcessorId;
    public String executeSqlRecordProcessorId;
    public String saveTargetDbProcessorId;
    public String mergeContentProcessorId;
    public String odsToStgProcessorId;
    public String queryNumbersProcessorId;
    public String convertNumbersToJsonProcessorId;
    public String setNumbersProcessorId;
    public String saveNumbersProcessorId;
    public String avroRecordSetWriterId;
    public String putDatabaseRecordId;
    public String processorInputPortId;
    public String processorOutputPortId;
    public String tableInputPortId;
    public String tableOutputPortId;
    public String selectSql;
    public String tableName;
    public int type;
    public String processorInputPortConnectId;
    public String processorOutputPortConnectId;
    public String tableInputPortConnectId;
    public String tableOutputPortConnectId;
    public String nifiCustomWorkflowDetailId;
    public int syncMode;
    public String updateFieldProcessorId;
    public String convertAvroRecordSetWriterId;
    public String convertPutDatabaseRecordId;
    public String consumeKafkaProcessorId;
    public String publishKafkaProcessorId;
    public String publishKafkaPipelineProcessorId;
    public String queryForSupervisionProcessorId;
    public String convertJsonForSupervisionProcessorId;
    public String publishKafkaForSupervisionProcessorId;
    public String csvReaderId;
    public String getFtpProcessorId;
    public String convertExcelToCsvProcessorId;
    public String convertRecordProcessorId;
    public String generateFlowFileProcessorId;
    public String invokeHttpProcessorId;
    public String updateFieldForCodeProcessorId;
    public String convertAvroRecordSetWriterForCodeId;
    public String convertPutDatabaseRecordForCodeId;
    public String evaluateTimeVariablesProcessorId;

}
