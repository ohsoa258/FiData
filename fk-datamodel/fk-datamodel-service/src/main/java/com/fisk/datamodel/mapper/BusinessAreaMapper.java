package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamodel.entity.BusinessAreaPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author: Lock
 */
@Mapper
public interface BusinessAreaMapper extends BaseMapper<BusinessAreaPO> {

    @Select("select id,business_name from tb_area_business where del_flag=1")
    List<BusinessAreaPO> getName();

    @Select("SELECT id,business_name,business_des,business_admin,business_email FROM tb_area_business\n" +
            "WHERE business_name LIKE CONCAT('%',#{key},'%')\n" +
            "AND del_flag = 1\n" +
            "ORDER BY update_time DESC ")
    List<Map<String, Object>> queryByPage(Page<Map<String, Object>> page, @Param("key") String key);
}
