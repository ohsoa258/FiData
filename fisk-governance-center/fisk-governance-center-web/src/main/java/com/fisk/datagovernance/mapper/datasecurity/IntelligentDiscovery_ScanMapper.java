package com.fisk.datagovernance.mapper.datasecurity;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_ScanPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface IntelligentDiscovery_ScanMapper extends FKBaseMapper<IntelligentDiscovery_ScanPO> {
    @Update("UPDATE tb_Intelligentdiscovery_scan SET del_flag=0 WHERE rule_id = #{ruleId};")
    int updateByRuleId(@Param("ruleId") int ruleId);
}
