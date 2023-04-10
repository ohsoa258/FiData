package com.fisk.system.dto;

import lombok.Data;

/**
 * @TableName tb_system_version
 */
@Data
public class SystemVersionDTO {

    /**
     * 版本id
     */
    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 发布时间
     */
    private String publishTime;

}
