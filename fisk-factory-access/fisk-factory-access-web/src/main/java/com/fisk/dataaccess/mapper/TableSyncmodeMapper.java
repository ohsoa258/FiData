package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataaccess.entity.TableSyncmodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Lock
 */
@Mapper
public interface TableSyncmodeMapper extends BaseMapper<TableSyncmodePO> {

    /**
     * 查询
     * @param id 请求参数
     * @return 返回值
     */
    @Select("select id,sync_mode,sync_field," +
            "custom_delete_condition,custom_insert_condition," +
            "timer_driver,corn_expression,retain_history_data,retain_time,retain_unit,version_unit,version_custom_rule, " +
            "max_rows_per_flow_file, fetch_size " +
            "from tb_table_syncmode " +
            "where id =#{id}")
    TableSyncmodePO getData(long id);

}
