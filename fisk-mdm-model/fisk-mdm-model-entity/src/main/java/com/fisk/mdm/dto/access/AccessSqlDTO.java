package com.fisk.mdm.dto.access;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author jianwenyang
 */
@Data
public class AccessSqlDTO {
    @ApiModelProperty(value = "accessId")
    private long accessId;

    @ApiModelProperty(value = "entityId")
    private Integer entityId;
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    private Integer dataSourceId;
    /**
     * 执行sql
     */
    @ApiModelProperty(value = "执行sql")
    private String sqlScript;

    /**
     * 版本描述（同发布描述）
     */
    @ApiModelProperty(value = "版本描述（同发布描述）")
    private String versionDes;
}
