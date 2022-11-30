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
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<QualityReportLogVO> page;
}
