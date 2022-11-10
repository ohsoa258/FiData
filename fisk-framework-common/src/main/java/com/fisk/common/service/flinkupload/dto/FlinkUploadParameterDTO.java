package com.fisk.common.service.flinkupload.dto;

import com.fisk.common.core.enums.flink.CommandEnum;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FlinkUploadParameterDTO {

    public String host;

    public Integer port;

    public String user;

    public String password;

    public String uploadPath;

    public String uploadWay;

    public String fileName;

    public CommandEnum commandEnum;

    public String commandPath;

}
