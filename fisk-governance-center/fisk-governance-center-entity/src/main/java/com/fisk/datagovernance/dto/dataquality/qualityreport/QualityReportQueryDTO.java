package com.fisk.datagovernance.dto.dataquality.qualityreport;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告查询DTO
 * @date 2022/3/24 14:30
 */
@Data
public class QualityReportQueryDTO {
    /**
     * 筛选器对象
     */
    @ApiModelProperty(value = "筛选器对象")
    public List<FilterQueryDTO> dto;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<QualityReportVO> page;
}
