package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.dto.dataquality.datasource.DataSourceConQuery;
import com.fisk.datagovernance.dto.dataquality.datasource.TableRuleCountDTO;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据源连接mapper
 *
 * @author dick
 */
@Mapper
public interface DataSourceConMapper extends FKBaseMapper<DataSourceConPO> {
    /**
     * 获取权限下所有数据源连接
     * @param page 分页信息
     * @param query where条件
     * @return 查询结果
     */
    Page<DataSourceConVO> listDataSourceCon(Page<DataSourceConVO> page, @Param("query") DataSourceConQuery query);

    /**
     * 查询FiData数据质量规则
     *
     * @return 查询结果
     */
    @Select("SELECT\n" +
            "\tsource.datasource_id AS sourceId,\n" +
            "\ttable_unique AS tableUnique,\n" +
            "\ttable_business_type AS tableBusinessType,\n" +
            "\ttable_type AS tableType,\n" +
            "\tCOUNT(*) AS tableRuleCount,\n" +
            "\t'校验规则' AS 'tableRuleType' \n" +
            "FROM\n" +
            "\ttb_datacheck_rule rule\n" +
            "\tLEFT JOIN tb_datasource_config source ON rule.datasource_id = source.id \n" +
            "WHERE\n" +
            "\tsource.datasource_type = 1 \n" +
            "\tAND source.del_flag = 1 \n" +
            "\tAND rule.del_flag = 1 \n" +
            "GROUP BY\n" +
            "\tsourceId,\n" +
            "\ttableUnique,\n" +
            "\ttableBusinessType,\n" +
            "\ttableType UNION ALL\n" +
            "SELECT\n" +
            "\tsource.datasource_id AS sourceId,\n" +
            "\ttable_unique AS tableUnique,\n" +
            "\ttable_business_type AS tableBusinessType,\n" +
            "\ttable_type AS tableType,\n" +
            "\tCOUNT(*) AS tableRuleCount,\n" +
            "\t'清洗规则' AS 'tableRuleType' \n" +
            "FROM\n" +
            "\ttb_bizfilter_rule rule\n" +
            "\tLEFT JOIN tb_datasource_config source ON rule.datasource_id = source.id \n" +
            "WHERE\n" +
            "\tsource.datasource_type = 1 \n" +
            "\tAND source.del_flag = 1 \n" +
            "\tAND rule.del_flag = 1 \n" +
            "GROUP BY\n" +
            "\tsourceId,\n" +
            "\ttableUnique,\n" +
            "\ttableBusinessType,\n" +
            "\ttableType UNION ALL\n" +
            "SELECT\n" +
            "\tsource.datasource_id AS sourceId,\n" +
            "\ttable_unique AS tableUnique,\n" +
            "\ttable_business_type AS tableBusinessType,\n" +
            "\ttable_type AS tableType,\n" +
            "\tCOUNT(*) AS tableRuleCount,\n" +
            "\t'回收规则' AS 'tableRuleType' \n" +
            "FROM\n" +
            "\ttb_lifecycle_rule rule\n" +
            "\tLEFT JOIN tb_datasource_config source ON rule.datasource_id = source.id \n" +
            "WHERE\n" +
            "\tsource.datasource_type = 1 \n" +
            "\tAND source.del_flag = 1 \n" +
            "\tAND rule.del_flag = 1 \n" +
            "GROUP BY\n" +
            "\tsourceId,\n" +
            "\ttableUnique,\n" +
            "\ttableBusinessType,\n" +
            "\ttableType")
    List<TableRuleCountDTO> getFiDataTableRuleList();

    /**
     * 查询自定义数据质量规则
     *
     * @return 查询结果
     */
    @Select("SELECT\n" +
            "\tsource.id AS sourceId,\n" +
            "\ttable_unique AS tableUnique,\n" +
            "\ttable_business_type AS tableBusinessType,\n" +
            "\ttable_type AS tableType,\n" +
            "\tCOUNT(*) AS tableRuleCount,\n" +
            "\t'校验规则' AS 'tableRuleType' \n" +
            "FROM\n" +
            "\ttb_datacheck_rule rule\n" +
            "\tLEFT JOIN tb_datasource_config source ON rule.datasource_id = source.id \n" +
            "WHERE\n" +
            "\tsource.datasource_type = 2 \n" +
            "\tAND source.del_flag = 1 \n" +
            "\tAND rule.del_flag = 1 \n" +
            "GROUP BY\n" +
            "\tsourceId,\n" +
            "\ttableUnique,\n" +
            "\ttableBusinessType,\n" +
            "\ttableType UNION ALL\n" +
            "SELECT\n" +
            "\tsource.id AS sourceId,\n" +
            "\ttable_unique AS tableUnique,\n" +
            "\ttable_business_type AS tableBusinessType,\n" +
            "\ttable_type AS tableType,\n" +
            "\tCOUNT(*) AS tableRuleCount,\n" +
            "\t'清洗规则' AS 'tableRuleType' \n" +
            "FROM\n" +
            "\ttb_bizfilter_rule rule\n" +
            "\tLEFT JOIN tb_datasource_config source ON rule.datasource_id = source.id \n" +
            "WHERE\n" +
            "\tsource.datasource_type = 2 \n" +
            "\tAND source.del_flag = 1 \n" +
            "\tAND rule.del_flag = 1 \n" +
            "GROUP BY\n" +
            "\tsourceId,\n" +
            "\ttableUnique,\n" +
            "\ttableBusinessType,\n" +
            "\ttableType UNION ALL\n" +
            "SELECT\n" +
            "\tsource.id AS sourceId,\n" +
            "\ttable_unique AS tableUnique,\n" +
            "\ttable_business_type AS tableBusinessType,\n" +
            "\ttable_type AS tableType,\n" +
            "\tCOUNT(*) AS tableRuleCount,\n" +
            "\t'回收规则' AS 'tableRuleType' \n" +
            "FROM\n" +
            "\ttb_lifecycle_rule rule\n" +
            "\tLEFT JOIN tb_datasource_config source ON rule.datasource_id = source.id \n" +
            "WHERE\n" +
            "\tsource.datasource_type = 2 \n" +
            "\tAND source.del_flag = 1 \n" +
            "\tAND rule.del_flag = 1 \n" +
            "GROUP BY\n" +
            "\tsourceId,\n" +
            "\ttableUnique,\n" +
            "\ttableBusinessType,\n" +
            "\ttableType")
    List<TableRuleCountDTO> getCustomizeTableRuleList();
}
