package com.fisk.mdm.dto.access;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.task.dto.accessmdm.LogResultDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wangjian
 */
@Data
public class TableHistoryDTO {
    /**
     * 发布表id
     */
    @ApiModelProperty(value = "发布表id")
    public Integer tableId;
    /**
     * 发布备注
     */
    @ApiModelProperty(value = "发布备注")
    public String remark;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    /**
     * 是否开启同步
     */
    @ApiModelProperty(value = "是否开启同步")
    public boolean openTransmission;

    /**
     *关联发布日志标识
     */
    @ApiModelProperty(value = "关联发布日志标识")
    public String subRunId;
    /**
     * 本次同步详情
     */
    @ApiModelProperty(value = "本次同步详情")
    public LogResultDTO dto;

    /**
     * 关联发布日志内容
     */
    @ApiModelProperty(value = "关联发布日志内容")
    public List<String> msg;
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;
}
