package com.fisk.mdm.dto.process;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.mdm.vo.process.ProcessApplyVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2023-04-06
 * @Description:
 */
@Data
@NoArgsConstructor
public class ProcessApplyDTO {

    @ApiModelProperty(value = "申请人/描述")
    private String keyword;

    @ApiModelProperty(value = "审批状态")
    private String state;

    @ApiModelProperty(value = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape =JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape =JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "返回view")
    private Page<ProcessApplyVO> page;
}
