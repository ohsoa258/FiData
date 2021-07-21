package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataaccess.entity.TableBusinessPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Lock
 */
@Mapper
public interface TableBusinessMapper extends BaseMapper<TableBusinessPO> {

    /**
     * 查询
     * @param id 请求参数
     * @return 返回值
     */
    @Select("SELECT id,access_id,business_time_field,business_flag,business_day FROM tb_table_business " +
            "where access_id =#{id}")
    TableBusinessPO getData(long id);

}
