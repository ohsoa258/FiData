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
    public String lastScanDateTime;
}
