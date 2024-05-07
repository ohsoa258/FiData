package com.fisk.task.dto.etllog;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EtlLogDTO {

    public int id;
    public String tablename;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime startdate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime enddate;
    public Integer datarows;
    public int status;
    public String code;
    public String errordesc;
    public String topicName;
    public String querySql;

}
