package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class IntelligentDiscovery_ScanResultVO {
    /**
     * 扫描的Sheet名称
     */
    @ApiModelProperty(value = "扫描的Sheet名称")
    public String sheetName;

    /**
     * 扫描的Sheet数据
     */
    @ApiModelProperty(value = "扫描的Sheet数据")
    public List<IntelligentDiscovery_ScanDataVO> sheetData;
}
