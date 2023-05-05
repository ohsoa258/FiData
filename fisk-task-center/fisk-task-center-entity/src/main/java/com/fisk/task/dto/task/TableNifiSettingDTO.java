package com.fisk.task.dto.task;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class TableNifiSettingDTO extends MQBaseDTO {
    @ApiModelProperty(value = "表组成Id")
    public String tableComponentId;
    @ApiModelProperty(value = "应用Id")
    public Integer appId;
    @ApiModelProperty(value = "表访问")
    public Integer tableAccessId;
    @ApiModelProperty(value = "调度组件")
    public String dispatchComponentId;
    @ApiModelProperty(value = "查询增量处理器Id")
    public String queryIncrementProcessorId;
    @ApiModelProperty(value = "将数据转换为Json处理器Id")
    public String convertDataToJsonProcessorId;
    @ApiModelProperty(value = "设置增量处理器Id")
    public String setIncrementProcessorId;
    @ApiModelProperty(value = "把日志配置数据库处理器Id")
    public String putLogToConfigDbProcessorId;
    @ApiModelProperty(value = "执行目标删除处理器Id")
    public String executeTargetDeleteProcessorId;
    @ApiModelProperty(value = "执行Sql记录处理器Id")
    public String executeSqlRecordProcessorId;
    @ApiModelProperty(value = "保存目标数据库处理器Id")
    public String saveTargetDbProcessorId;
    @ApiModelProperty(value = "合并内容处理器Id")
    public String mergeContentProcessorId;
    @ApiModelProperty(value = "ods到Stg处理器Id")
    public String odsToStgProcessorId;
    @ApiModelProperty(value = "查询编号处理器Id")
    public String queryNumbersProcessorId;
    @ApiModelProperty(value = "将数字转换为Json处理器Id")
    public String convertNumbersToJsonProcessorId;
    @ApiModelProperty(value = "设置数字处理器Id")
    public String setNumbersProcessorId;
    @ApiModelProperty(value = "保存数字处理器Id")
    public String saveNumbersProcessorId;
    @ApiModelProperty(value = "avro记录设置WriterId")
    public String avroRecordSetWriterId;
    @ApiModelProperty(value = "输入数据库记录Id")
    public String putDatabaseRecordId;
    @ApiModelProperty(value = "处理器输入端口Id")
    public String processorInputPortId;
    @ApiModelProperty(value = "udt名称")
    public String processorOutputPortId;
    @ApiModelProperty(value = "处理器输出端口Id")
    public String tableInputPortId;
    @ApiModelProperty(value = "udt名称")
    public String tableOutputPortId;
    @ApiModelProperty(value = "表输出端口Id")
    public String selectSql;
    @ApiModelProperty(value = "表名")
    public String tableName;
    @ApiModelProperty(value = "类型")
    public int type;
    @ApiModelProperty(value = "处理器输入端口连接Id")
    public String processorInputPortConnectId;
    @ApiModelProperty(value = "处理器输出端口连接Id")
    public String processorOutputPortConnectId;
    @ApiModelProperty(value = "表输入端口连接id")
    public String tableInputPortConnectId;
    @ApiModelProperty(value = "表输出端口连接id")
    public String tableOutputPortConnectId;
    @ApiModelProperty(value = "nifi自定义工作流详细Id")
    public String nifiCustomWorkflowDetailId;
    @ApiModelProperty(value = "同步模式")
    public int syncMode;
    @ApiModelProperty(value = "更新字段处理器Id")
    public String updateFieldProcessorId;
    @ApiModelProperty(value = "转换Avro记录集WriterId")
    public String convertAvroRecordSetWriterId;
    @ApiModelProperty(value = "转换Put数据库记录Id")
    public String convertPutDatabaseRecordId;
    @ApiModelProperty(value = "使用Kafka处理器Id")
    public String consumeKafkaProcessorId;
    @ApiModelProperty(value = "发布Kafka处理器Id")
    public String publishKafkaProcessorId;
    @ApiModelProperty(value = "发布Kafka管道处理器Id")
    public String publishKafkaPipelineProcessorId;
    @ApiModelProperty(value = "查询监控器处理器Id")
    public String queryForSupervisionProcessorId;
    @ApiModelProperty(value = "将Json转换为监管处理器Id")
    public String convertJsonForSupervisionProcessorId;
    @ApiModelProperty(value = "发布监管处理器Id的Kafka")
    public String publishKafkaForSupervisionProcessorId;
}
