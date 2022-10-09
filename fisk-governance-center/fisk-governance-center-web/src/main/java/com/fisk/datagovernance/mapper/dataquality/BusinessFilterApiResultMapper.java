package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiResultPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗结果
 * @date 2022/10/8 16:58
 */
@Mapper
public interface BusinessFilterApiResultMapper extends FKBaseMapper<BusinessFilterApiResultPO> {
    /**
     * 修改数据有效性
     *
     * @return 执行结果
     */
    @Update("UPDATE tb_bizfilter_api_result SET del_flag=0 WHERE rule_id = #{ruleId};")
    int updateByRuleId(@Param("ruleId") int ruleId);
}
