package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.QualityReportRulePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告规则
 * @date 2022/5/16 18:24
 */
@Mapper
public interface QualityReportRuleMapper extends FKBaseMapper<QualityReportRulePO> {
    /**
     * 修改质量报表规则
     *
     * @return 执行结果
     */
    @Update("UPDATE tb_quality_report_rule SET del_flag=0 WHERE report_id = #{reportId};")
    int updateByReportId(@Param("reportId") int reportId);
}
