package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.DataCheckExtendPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckExtendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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


    /**
     * 查询表字段规则信息
     *
     * @return 字段规则列表
     */
    List<DataCheckExtendVO> getDataCheckExtendByRuleIdList(@Param("ruleIdList") List<Integer> ruleIdList);
}
