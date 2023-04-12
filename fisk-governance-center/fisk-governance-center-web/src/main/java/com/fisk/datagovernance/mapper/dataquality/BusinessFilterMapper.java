package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗mapper
 * @date 2022/3/23 12:42
 */
@Mapper
public interface BusinessFilterMapper extends FKBaseMapper<BusinessFilterPO> {
    /**
     * 查询清洗规则列表
     *
     * @return 查询结果
     */
    List<BusinessFilterVO> getAllRule();

    /**
     * 查询清洗规则
     *
     * @return 查询结果
     */
    BusinessFilterVO getRuleById(@Param("ruleId") long ruleId);

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_bizfilter_rule(`datasource_id`, `rule_name`, `table_unique`, `table_type`, `table_business_type`, `rule_sort`, `rule_state`, `create_time`, `create_user`, `del_flag`) VALUES (#{datasourceId}, #{ruleName}, #{tableUnique}, #{tableType}, #{tableBusinessType}, #{ruleSort}, #{ruleState}, #{createTime}, #{createUser}, 1);\n;")
    int insertOne(BusinessFilterPO po);
}
