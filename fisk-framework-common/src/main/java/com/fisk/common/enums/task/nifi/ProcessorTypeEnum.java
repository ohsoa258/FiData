package com.fisk.common.enums.task.nifi;

import com.fisk.common.enums.BaseEnum;

/**
 * @author gy
 */

public enum ProcessorTypeEnum implements BaseEnum {

    /**
     * Processor组件类型
     */
    ExecuteSQL(0, "org.apache.nifi.processors.standard.ExecuteSQL"),
    ConvertAvroToJSON(1, "org.apache.nifi.processors.avro.ConvertAvroToJSON"),
    ConvertJSONToSQL(2, "org.apache.nifi.processors.standard.ConvertJSONToSQL"),
    PutSQL(3, "org.apache.nifi.processors.standard.PutSQL"),
    EvaluateJsonPath(4, "org.apache.nifi.processors.standard.EvaluateJsonPath"),
    UpdateAttribute(5, "org.apache.nifi.processors.attributes.UpdateAttribute"),
    MergeContent(6, "org.apache.nifi.processors.standard.MergeContent"),
    ReplaceText(7, "org.apache.nifi.processors.standard.ReplaceText"),
    PublishAMQP(8, "org.apache.nifi.amqp.processors.PublishAMQP"),
    SplitJson(9,"org.apache.nifi.processors.standard.SplitJson"),
    ExecuteSQLRecord(10,"org.apache.nifi.processors.standard.ExecuteSQLRecord"),
    PutDatabaseRecord(11,"org.apache.nifi.processors.standard.PutDatabaseRecord"),
    NOTIFY(12,"org.apache.nifi.processors.standard.Notify"),
    WAIT(13,"org.apache.nifi.processors.standard.Wait"),
    UPDATERECORD(14,"org.apache.nifi.processors.standard.UpdateRecord"),
    ConsumeKafka(15,"org.apache.nifi.processors.kafka.pubsub.ConsumeKafka_2_6"),
    PublishKafka(16,"org.apache.nifi.processors.kafka.pubsub.PublishKafka_2_6");


    ProcessorTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
