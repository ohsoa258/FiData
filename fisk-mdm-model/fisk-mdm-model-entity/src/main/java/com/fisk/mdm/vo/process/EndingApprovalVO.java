package com.fisk.mdm.vo.process;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2023-04-06
 */
@Data
public class EndingApprovalVO {

    @ApiModelProperty(value = "流程工单ID")
    private Integer applyId;

    @ApiModelProperty(value = "审批编号")
    private String approvalCode;

    @ApiModelProperty(value = "流程ID")
    private Integer processId;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "申请人")
    private String applicant;

    @ApiModelProperty(value = "申请人名称")
    private String applicantName;

    @ApiModelProperty(value = "操作类型")
    private String operationType;

    @ApiModelProperty(value = "当前节点审批状态")
    private String state;

    @ApiModelProperty(value = "审批时间")
    @JsonFormat(shape =JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    private LocalDateTime approvalTime;

    @ApiModelProperty(value = "申请时间")
    @JsonFormat(shape =JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    private LocalDateTime applicationTime;
}
