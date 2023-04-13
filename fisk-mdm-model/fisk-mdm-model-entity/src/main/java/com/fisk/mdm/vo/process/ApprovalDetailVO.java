package com.fisk.mdm.vo.process;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-04-10
 * @Description:
 */
@Data
public class ApprovalDetailVO {
    @ApiModelProperty(value = "流程工单ID")
    private Integer applyId;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "审批编号")
    private Integer applyCode;
    @ApiModelProperty(value = "申请人")
    private String applicant;
    @ApiModelProperty(value = "操作类型")
    private String operationType;
    @ApiModelProperty(value = "申请时间")
    @JsonFormat(shape =JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    private LocalDateTime applicationTime;
    @ApiModelProperty(value = "节点")
    private List<PersonVO> persons;
}
