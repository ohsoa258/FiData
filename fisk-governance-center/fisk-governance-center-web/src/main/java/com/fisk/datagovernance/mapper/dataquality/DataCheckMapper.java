package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.DataCheckPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

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
    @Insert("INSERT INTO tb_datacheck_rule(`template_id`, `datasource_id`, `rule_name`, `table_unique`,`table_type`, `table_business_type`, `check_rule`, `create_rule`, `rule_sort`, `rule_state`,`threshold_value`, `create_time`, `create_user`, `del_flag`) VALUES (#{templateId}, #{datasourceId}, #{ruleName}, #{tableUnique}, #{tableType}, #{tableBusinessType}, #{checkRule}, #{createRule}, #{ruleSort}, #{ruleState}, #{thresholdValue}, #{createTime}, #{createUser},1);")
    int insertOne(DataCheckPO po);
}