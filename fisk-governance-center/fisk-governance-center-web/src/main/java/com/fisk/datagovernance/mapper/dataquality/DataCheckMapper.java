package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.DataCheckPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DeleteCheckResultVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验
 * @date 2022/3/23 12:42
 */
@Mapper
public interface DataCheckMapper extends FKBaseMapper<DataCheckPO> {
    /**
     * 查询数据校验列表
     *
     * @return 查询结果
     */
    List<DataCheckVO> getAllRule();

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_datacheck_rule(`template_id`, `datasource_id`, `rule_name`, `schema_name`, `table_unique`,  `table_name`,  `table_describe`, `table_type`, `table_business_type`, `rule_check_type`, `rule_execute_node`, `rule_execute_sort`, `rule_weight`,`rule_describe`,`rule_state`, `rule_illustrate`, `create_time`, `create_user`, `del_flag`) VALUES (#{templateId}, #{datasourceId}, #{ruleName}, #{schemaName}, #{tableUnique}, #{tableName}, #{tableDescribe}, #{tableType}, #{tableBusinessType}, #{ruleCheckType}, #{ruleExecuteNode}, #{ruleExecuteSort}, #{ruleWeight}, #{ruleDescribe}, #{ruleState}, #{ruleIllustrate}, #{createTime}, #{createUser},1);")
    int insertOne(DataCheckPO po);

    @Select("SELECT\n" +
            "\trule.id AS ruleId,\n" +
            "\trule.rule_name AS ruleName,\n" +
            "\truleExt.error_data_retention_time AS errorDataRetentionTime \n" +
            "FROM\n" +
            "\ttb_datacheck_rule rule\n" +
            "\tLEFT JOIN tb_datacheck_rule_extend ruleExt ON rule.id = ruleExt.rule_id \n" +
            "WHERE\n" +
            "\truleExt.record_error_data = 1 \n" +
            "\tAND rule.del_flag = 1 \n" +
            "\tAND ruleExt.del_flag = 1 \n" +
            "\tAND ruleExt.error_data_retention_time > 0")
    List<DeleteCheckResultVO> getDeleteDataCheckResult();
}