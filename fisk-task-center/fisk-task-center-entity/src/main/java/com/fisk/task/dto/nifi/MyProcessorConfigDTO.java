package com.fisk.task.dto.nifi;

import com.davis.client.model.ProcessorConfigDTO;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class MyProcessorConfigDTO extends ProcessorConfigDTO {

    @SerializedName("retryCount")
    private Integer retryCount;

    @SerializedName("maxBackoffPeriod")
    private String maxBackoffPeriod;

    @SerializedName("backoffMechanism")
    private String backoffMechanism;

    @SerializedName("retriedRelationships")
    private List<String> retriedRelationships;

}
