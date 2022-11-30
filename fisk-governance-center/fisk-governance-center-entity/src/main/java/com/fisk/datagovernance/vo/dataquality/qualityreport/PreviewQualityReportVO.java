package com.fisk.datagovernance.vo.dataquality.qualityreport;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 预览质量报告
 * @date 2022/11/29 14:32
 */
@Data
public class PreviewQualityReportVO {
    /**
     * 数据
     */
    @ApiModelProperty(value = "数据")
    public JSONArray dataArray;

    /**
     * sheet名称
     */
    @ApiModelProperty(value = "sheet名称")
    public String sheetName;
}
