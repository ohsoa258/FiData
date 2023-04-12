package com.fisk.task.dto.dispatchlog;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author cfk
 */
@Data
public class PipelMergeLog {
    @ApiModelProperty(value = "管道id")
    public String pipelId;
    @ApiModelProperty(value = "管道批次号")
    public String pipelTraceId;
    @ApiModelProperty(value = "调度的管道名称")
    public String pipelName;
    @ApiModelProperty(value = "管道开始时间")
    public Date startTime;
    @ApiModelProperty(value = "管道结束时间")
    public Date endTime;
    @ApiModelProperty(value = "持续时间/分钟")
    public String duration;
    @ApiModelProperty(value = "管道状态,正在执行/已结束")
    public String pipelStatu;
    @ApiModelProperty(value = "管道运行结果,成功/失败")
    public String result;
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;



}
