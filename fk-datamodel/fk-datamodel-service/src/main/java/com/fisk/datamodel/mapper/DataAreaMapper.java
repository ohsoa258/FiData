package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamodel.entity.DataAreaPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author: Lock
 */
@Mapper
public interface DataAreaMapper extends BaseMapper<DataAreaPO> {

    @Select("SELECT \n" +
            "\ta.id,\n" +
            "\ta.business_name,\n" +
            "\tb.data_name,\n" +
            "\tb.date_des\n" +
            "FROM `业务域表名` a\n" +
            "LEFT JOIN `数据域表名` AS b\n" +
            "ON a.id = f.id\n" +
            "WHERE business_name LIKE concat('%',#{key},'%') \n" +
            "AND a.del_flag = 1\n" +
            "ORDER BY a.update_time DESC;")
    List<Map<String, Object>> queryByPage(Page<Map<String,Object>> page, @Param("key") String key);

}
