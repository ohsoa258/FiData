package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author wangjian
 * @date 2024-09-25 16:13:25
 */
@TableName("tb_apitreelist_history")
@Data
public class ApitreelistHistoryPO{

    @ApiModelProperty(value = "pid")
    public String pid;
    @ApiModelProperty(value = "appId")
    public String appId;
    @ApiModelProperty(value = "apiId")
    public String apiId;
    @ApiModelProperty(value = "属性id")
    public String attributeId;
    @ApiModelProperty(value = "历史id")
    public String historyId;
    @ApiModelProperty(value = "app名称")
    public String appName;
    @ApiModelProperty(value = "api名称")
    public String apiName;
    @ApiModelProperty(value = "属性名称")
    public String attributeName;

    public LocalDateTime createTime;

    public String createUser;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    public LocalDateTime updateTime;

    @TableField(value = "update_user", fill = FieldFill.UPDATE)
    public String updateUser;

    @TableLogic
    public int delFlag;
}
