package com.fisk.system.dto;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class LoginServiceDTO {
    /**
     *服务中文名称
     */
    public String name;

    /**
     *服务url
     */
    public  String path;

    public String component;

    public IconDTO meta;

    public List<LoginServiceDTO> dto;
}
