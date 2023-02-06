package com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.IntelligentDiscovery_LogsVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_LogsQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyWord;

    /**
     * tb_Intelligentdiscovery_rule表主键ID
     */
    @ApiModelProperty(value = "tb_Intelligentdiscovery_rule表主键ID")
    public int ruleId;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<IntelligentDiscovery_LogsVO> page;
}
