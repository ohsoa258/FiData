package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.QualityReportRulePO;
import com.fisk.datagovernance.vo.appcount.AppRuleCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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

    /**
     * 根据规则id删除已引用的规则
     *
     * @return 执行结果
     */
    @Update("UPDATE tb_quality_report_rule SET del_flag=0 WHERE rule_id = #{ruleId};")
    int updateByRuleId(@Param("ruleId") int ruleId);

    @Select("SELECT\n" +
            "\tt1.report_id AS appId,\n" +
            "\tCOUNT(*) AS count \n" +
            "FROM\n" +
            "\ttb_quality_report_rule t1\n" +
            "\tLEFT JOIN tb_quality_report t2 ON t1.report_id = t2.id\n" +
            "\tLEFT JOIN tb_datacheck_rule t3 ON t1.rule_id = t3.id \n" +
            "WHERE\n" +
            "\tt1.del_flag = 1 \n" +
            "\tAND t2.del_flag = 1 \n" +
            "\tAND t3.del_flag = 1 \n" +
            "\tAND t1.report_type = 100 \n" +
            "GROUP BY\n" +
            "\tt1.report_id")
    List<AppRuleCountVO> getAppRuleCount();


    @Select("SELECT\n" +
            "\treportRule.id,\n" +
            "\treportRule.report_id AS reportId,\n" +
            "\treportRule.report_type AS reportType,\n" +
            "\treportRule.rule_id AS ruleId,\n" +
            "\treportRule.rule_sort AS ruleSort \n" +
            "FROM\n" +
            "\ttb_quality_report_rule reportRule\n" +
            "\tLEFT JOIN tb_datacheck_rule rule ON reportRule.rule_id = rule.id \n" +
            "WHERE\n" +
            "\treportRule.del_flag = 1 \n" +
            "\tAND rule.del_flag = 1 \n" +
            "\tAND reportRule.report_id = #{reportId}\n" +
            "\tORDER BY reportRule.rule_sort ASC")
    List<QualityReportRulePO> getQualityReportRuleList(@Param("reportId") int reportId);
}
