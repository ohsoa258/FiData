package com.fisk.datamodel.dto.versionsql;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VersionSqlDTO {

    /**
     * 维度表/事实表id
     */
    @ApiModelProperty(value = "维度表/事实表id")
    private Integer tableId;

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
     * 表类型：0维度  1事实
     */
    @ApiModelProperty(value = "表类型：0维度  1事实")
    private Integer tableType;

}
