package com.fisk.datagovernance.mapper.datasecurity;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_NoticePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface IntelligentDiscovery_NoticeMapper extends FKBaseMapper<IntelligentDiscovery_NoticePO> {
    @Update("UPDATE tb_Intelligentdiscovery_notice SET del_flag=0 WHERE rule_id = #{ruleId};")
    int updateByRuleId(@Param("ruleId") int ruleId);
}
