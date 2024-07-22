package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.DataCheckConditionPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckConditionVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckExtendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据校验规则-检查条件
 * @date 2022/3/22 14:51
 */
@Mapper
public interface DataCheckConditionMapper extends FKBaseMapper<DataCheckConditionPO> {
    /**
     * 数据校验规则-检查条件属性
     *
     * @return 执行结果
     */
    @Update("UPDATE tb_datacheck_rule_condition SET del_flag=0 WHERE rule_id = #{ruleId};")
    int updateByRuleId(@Param("ruleId") int ruleId);

    /**
     * 查询表字段检查条件信息
     *
     * @return 字段规则列表
     */
    List<DataCheckConditionVO> getDataCheckExtendByRuleIdList(@Param("ruleIdList") List<Integer> ruleIdList);
}
