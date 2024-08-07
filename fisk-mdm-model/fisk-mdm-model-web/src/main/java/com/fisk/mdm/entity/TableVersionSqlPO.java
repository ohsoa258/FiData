package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tb_version_sql
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_version_sql")
@Data
public class TableVersionSqlPO extends BasePO implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public long id;

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

}