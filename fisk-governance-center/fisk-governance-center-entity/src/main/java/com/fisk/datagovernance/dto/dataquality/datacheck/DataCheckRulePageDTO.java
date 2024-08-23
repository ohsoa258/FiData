package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则分页DTO
 * @date 2024/8/22 13:29
 */
@Data
public class DataCheckRulePageDTO {
    /**
     * 数据元组ID集合
     */
    @ApiModelProperty(value = "数据元组ID集合")
    public List<Long> dataCheckGroupIds;

    /**
     * 规则数据源类型：1：质量规则配置数据 2：数据元配置数据
     */
    @ApiModelProperty(value = "规则数据源类型：1：质量规则配置数据 2：数据元配置数据")
    public int ruleDataSourceType;

    /**
     * 表全名称（含架构名）/表Id
     */
    @ApiModelProperty(value = "表全名称（含架构名）/表Id")
    public String tableUnique;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 执行环节：3
     */
    @ApiModelProperty(value = "执行环节：3")
    public int checkProcess;

    /**
     * 规则状态：1
     */
    @ApiModelProperty(value = "规则状态：1")
    public String ruleState;

    /**
     * 分页
     */
    @ApiModelProperty(value = "分页")
    public Page<DataCheckVO> page;
}
