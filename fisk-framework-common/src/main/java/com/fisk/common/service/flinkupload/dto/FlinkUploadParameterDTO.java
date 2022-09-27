package com.fisk.common.service.flinkupload.dto;

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

}
