package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.DataAreaDTO;
import com.fisk.datamodel.dto.DataAresPageDTO;
import com.fisk.datamodel.entity.DataAreaPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Mapper
public interface DataAreaMapper extends FKBaseMapper<DataAreaPO> {

    /**
     * 分页
     * @param page page
     * @param key key
     * @return 查询结果
     */
    @Select("SELECT \n" +
            "\tb.id,\n" +
            "\ta.business_name,\n" +
            "\tb.data_name,\n" +
            "\tb.data_des\n" +
            "FROM `tb_area_business` a\n" +
            "LEFT JOIN `tb_area_data` AS b\n" +
            "ON a.id = b.businessid\n" +
            "WHERE business_name LIKE concat('%',#{key},'%') \n" +
            "AND b.del_flag = 1\n" +
            "ORDER BY a.update_time DESC ")
    List<Map<String, Object>> queryByPage(Page<Map<String,Object>> page, @Param("key") String key);

    /**
     * 筛选器
     *
     * @param page page
     * @param query query
     * @return 查询结果
     */
    Page<DataAreaDTO> queryList(Page<DataAreaDTO> page, @Param("query") DataAresPageDTO query);
}
