package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗mapper
 * @date 2022/3/23 12:42
 */
@Mapper
public interface BusinessFilterMapper extends FKBaseMapper<BusinessFilterPO> {
    /**
     * 查询数据校验分页列表
     *
     * @param page    分页信息
     * @param keyword where条件
     * @param tableUnique 表名称/Id
     * @return 查询结果
     */
    Page<BusinessFilterVO> getAll(Page<BusinessFilterVO> page,
                                  @Param("datasourceId") int datasourceId,
                                  @Param("tableUnique") String tableUnique,
                                  @Param("tableBusinessType") int tableBusinessType,
                                  @Param("keyword") String keyword);

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_bizfilter_rule(`template_id`, `datasource_id`, `rule_name`, `table_unique`, `table_type`, `table_business_type`, `create_rule`, `rule_sort`, `rule_state`, `create_time`, `create_user`, `del_flag`) VALUES (#{templateId}, #{datasourceId}, #{ruleName}, #{tableUnique}, #{tableType}, #{tableBusinessType}, #{createRule}, #{ruleSort}, #{ruleState}, #{createTime}, #{createUser}, 1);\n;")
    int insertOne(BusinessFilterPO po);
}
