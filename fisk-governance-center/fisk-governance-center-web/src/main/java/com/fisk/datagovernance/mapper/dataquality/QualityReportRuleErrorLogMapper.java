package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.QualityReportRuleErrorLogPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告下规则执行错误日志
 * @date 2022/5/16 18:24
 */
@Mapper
public interface QualityReportRuleErrorLogMapper extends FKBaseMapper<QualityReportRuleErrorLogPO> {

}
