package com.fisk.datagovernance.mapper.datasecurity;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_RulePageDTO;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_RulePO;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.IntelligentDiscovery_RuleVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IntelligentDiscovery_RuleMapper extends FKBaseMapper<IntelligentDiscovery_RulePO> {

    Page<IntelligentDiscovery_RuleVO> filter(Page<IntelligentDiscovery_RuleVO> page, @Param("query") IntelligentDiscovery_RulePageDTO query);

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_Intelligentdiscovery_rule(`rule_name`, `rule_type`, `rule_value`, `rule_describe`, `rule_state`," +
            " `risk_level`, `scan_period`, `scan_risk_count`, `principal`," +
            " `create_time`, `create_user`, `del_flag`) " +
            "VALUES (#{ruleName}, #{ruleType}, #{ruleValue}, #{ruleDescribe}, #{ruleState}," +
            " #{riskLevel}, #{scanPeriod}, #{scanRiskCount}, #{principal}," +
            " #{createTime}, #{createUser},1);")
    int insertOne(IntelligentDiscovery_RulePO po);
}
