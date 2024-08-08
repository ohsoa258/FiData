package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportPageDTO;
import com.fisk.datagovernance.entity.dataquality.QualityReportPO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportRuleVO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告
 * @date 2022/3/23 12:42
 */
@Mapper
public interface QualityReportMapper extends FKBaseMapper<QualityReportPO> {
    /**
     * 查询数据校验分页列表
     *
     * @param page  分页信息
     * @param query where条件
     * @return 查询结果
     */
    Page<QualityReportVO> getAll(Page<QualityReportVO> page, @Param("query") QualityReportPageDTO query);

    /**
     * @description 质量报告规则
     * @author dick
     * @date 2024/8/8 14:23
     * @version v1.0
     * @params page
     * @params query
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportRuleVO>
     */
    List<QualityReportRuleVO> getByRuleIds(@Param("ruleIds") List<Integer> ruleIds);

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_quality_report(`report_name`, `report_type`, `report_type_name`, `report_desc`, `report_principal`, `report_state`, `run_time_cron`, `report_evaluation_criteria`, `create_time`, `create_user`, `del_flag`) VALUES (#{reportName}, #{reportType}, #{reportTypeName}, #{reportDesc}, #{reportPrincipal}, #{reportState}, #{runTimeCron}, #{reportEvaluationCriteria}, #{createTime}, #{createUser},1);")
    int insertOne(QualityReportPO po);
}