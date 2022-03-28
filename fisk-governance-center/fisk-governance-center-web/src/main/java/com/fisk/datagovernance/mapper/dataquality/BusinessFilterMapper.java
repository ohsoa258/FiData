package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
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
     * @param tableName 表名称
     * @return 查询结果
     */
    Page<BusinessFilterVO> getAll(Page<BusinessFilterVO> page, @Param("tableName") String tableName, @Param("keyword") String keyword);

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_bizfilter_module(`template_id`, `datasource_id`, `datasource_type`, `module_name`, `filter_step`, `table_name`, `pro_table_name`, `module_rule`, `run_time_cron`, `module_exec_sort`, `module_state`, `create_time`, `create_user`, `del_flag`) VALUES (#{templateId}, #{datasourceId}, #{datasourceType}, #{moduleName}, #{filterStep}, #{tableName}, #{proTableName}, #{moduleRule}, #{runTimeCron}, #{moduleExecSort}, #{moduleState}, #{createTime}, #{createUser},1);")
    int insertOne(BusinessFilterPO po);
}
