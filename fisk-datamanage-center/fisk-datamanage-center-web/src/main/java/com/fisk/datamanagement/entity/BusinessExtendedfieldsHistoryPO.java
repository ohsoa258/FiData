package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author wangjian
 * @date 2024-03-01 14:32:54
 */
@TableName("tb_business_extendedfields_history")
@Data
public class BusinessExtendedfieldsHistoryPO {
    @TableId(value = "id", type = IdType.AUTO)
    public long id;

    @ApiModelProperty(value = "")
    private String historyId;

    @ApiModelProperty(value = "")
    private String dimdomaintype;

    @ApiModelProperty(value = "")
    private String dimdomainid;

    @ApiModelProperty(value = "")
    private String dimdomain;

    @ApiModelProperty(value = "")
    private String dimtableid;

    @ApiModelProperty(value = "")
    private String dimtable;

    @ApiModelProperty(value = "")
    private String attributeid;

    @ApiModelProperty(value = "")
    private String attribute;

    @ApiModelProperty(value = "")
    private String indexid;

    @ApiModelProperty(value = "")
    private String attributeEnName;

    public String dimtableEnName;

    public String createdTime;

    public String createdUser;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    public LocalDateTime updateTime;
    @TableField(value = "update_user", fill = FieldFill.UPDATE)
    public String updateUser;

    @TableLogic
    public int delFlag;
}
