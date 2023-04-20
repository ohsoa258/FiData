package com.fisk.dataservice.vo.atvserviceanalyse;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 统计数据服务API熔断情况VO
 * @date 2023/4/20 18:25
 */
@Data
public class AtvCallApiFuSingAnalyseVO {
    /**
     * 上次扫描结果
     */
    @ApiModelProperty(value = "上次扫描结果")
    public String lastScanResult;

    /**
     * 上次扫描时间
     */
    @ApiModelProperty(value = "上次扫描时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime lastScanDateTime;

    /**
     * 扫描失败总次数
     */
    @ApiModelProperty(value = "扫描失败总次数")
    public Integer scanFailCount;

    /**
     * 扫描成功总次数
     */
    @ApiModelProperty(value = "扫描成功总次数")
    public Integer scanSuccessCount;
}
