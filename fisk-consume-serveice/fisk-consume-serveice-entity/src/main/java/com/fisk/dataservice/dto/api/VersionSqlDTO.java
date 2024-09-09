package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VersionSqlDTO {

    /**
     * apiId
     */
    @ApiModelProperty(value = "apiId")
    private Integer apiId;

    /**
     * 版本号
     */
    @ApiModelProperty(value = "版本号")
    private String versionNumber;

    /**
     * 版本描述（同发布描述）
     */
    @ApiModelProperty(value = "版本描述（同发布描述）")
    private String versionDes;

    /**
     * 历史sql
     */
    @ApiModelProperty(value = "历史sql")
    private String historicalSql;

    /**
     * 历史查询总数sql
     */
    @ApiModelProperty(value = "历史查询总数sql")
    private String historicalCountSql;

    /**
     * api类型 1 sql、2 自定义sql
     */
    @ApiModelProperty(value = "api类型 1 sql、2 自定义sql")
    private Integer apiType;

    /**
     *
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;

}
