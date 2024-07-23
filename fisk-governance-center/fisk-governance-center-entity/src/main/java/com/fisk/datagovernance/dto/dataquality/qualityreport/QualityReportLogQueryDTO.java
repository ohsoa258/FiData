package com.fisk.datagovernance.dto.dataquality.qualityreport;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportLogVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告日志查询DTO
 * @date 2022/11/29 17:20
 */
@Data
public class QualityReportLogQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

    /**
     * 报告id
     */
    @ApiModelProperty(value = "报告id")
    public int reportId;

    /**
     * 报告批次号
     */
    @ApiModelProperty(value = "报告批次号")
    public String reportBatchNumber;

    /**
     * 检查时间-开始时间
     */
    @ApiModelProperty(value = "检查时间-开始时间")
    public String createReportStartTime;

    /**
     * 检查时间-结束时间
     */
    @ApiModelProperty(value = "检查时间-结束时间")
    public String createReportEndTime;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<QualityReportLogVO> page;
}
