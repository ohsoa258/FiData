package com.fisk.system.dto;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class LoginServiceDTO {
    /**
     * ID
     */
    public Long id;

    /**
     *服务中文名称
     */
    public String name;

    /**
     * 服务url
     */
    public String path;

    public String component;

    public IconDTO meta;

    /**
     * 是否有权限
     */
    public Boolean authority = false;

    public String serveCode;

    public String description;

    public Integer sequenceNo;

    public List<LoginServiceDTO> children;
}
