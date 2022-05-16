package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.DataCheckExtendPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验扩展属性
 * @date 2022/5/16 18:49
 */
@Mapper
public interface DataCheckExtendMapper extends FKBaseMapper<DataCheckExtendPO> {
    /**
     * 修改数据校验扩展属性
     *
     * @return 执行结果
     */
    @Update("UPDATE tb_datacheck_rule_extend SET del_flag=0 WHERE rule_id = #{ruleId};")
    int updateByRuleId(@Param("ruleId") int ruleId);
}
