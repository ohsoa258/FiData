package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.LifecyclePO;
import com.fisk.datagovernance.vo.dataquality.lifecycle.LifecycleVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期
 * @date 2022/3/23 12:42
 */
@Mapper
public interface LifecycleMapper extends FKBaseMapper<LifecyclePO> {
    /**
     * 查询数据校验分页列表
     *
     * @param page    分页信息
     * @param keyword where条件
     * @param tableName 表名称
     * @return 查询结果
     */
    Page<LifecycleVO> getAll(Page<LifecycleVO> page, @Param("tableName") String tableName, @Param("keyword") String keyword);

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_lifecycle_module(`template_id`, `datasource_id`, `datasource_type`, `module_name`, `table_name`, `field_name`, `recovery_date`, `remind_date`, `is_backup`, `check_emptytb_day`, `check_refreshtb_day`, `check_consanguinity_day`, `run_time_cron`, `table_state`, `module_state`, `create_time`, `create_user`, `del_flag`) VALUES (#{templateId}, #{datasourceId}, #{datasourceType}, #{moduleName}, #{tableName}, #{fieldName}, #{recoveryDate}, #{remindDate}, #{isBackup}, #{checkEmptytbDay}, #{checkRefreshtbDay}, #{checkConsanguinityDay}, #{runTimeCron}, #{tableState}, #{moduleState}, #{createTime}, #{createUser},1);")
    int insertOne(LifecyclePO po);
}