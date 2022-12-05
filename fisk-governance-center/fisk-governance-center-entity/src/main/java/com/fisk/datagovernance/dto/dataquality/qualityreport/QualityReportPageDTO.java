package com.fisk.datagovernance.dto.dataquality.qualityreport;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告分页DTO
 * @date 2022/11/29 10:47
 */
@Data
public class QualityReportPageDTO {
    /**
     * 条件
     */
    @ApiModelProperty(value = "条件")
    public String where;

    /**
     * 分页
     */
    @ApiModelProperty(value = "分页")
    public Page<QualityReportVO> page;
}
