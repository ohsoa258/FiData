package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamodel.dto.*;
import com.fisk.datamodel.entity.BusinessAreaPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Mapper
public interface BusinessAreaMapper extends BaseMapper<BusinessAreaPO> {

    /**
     *  查询
     * @return 查询结果
     */
    @Select("select id,business_name from tb_area_business where del_flag=1")
    List<BusinessAreaPO> getName();

    /**
     * 分页
     *
     * @param page page
     * @param key key
     * @return 查询结果
     */
    @Select("SELECT id,business_name,business_des,business_admin,business_email FROM tb_area_business\n" +
            "WHERE business_name LIKE CONCAT('%',#{key},'%')\n" +
            "AND del_flag = 1\n" +
            "ORDER BY update_time DESC ")
    List<Map<String, Object>> queryByPage(Page<Map<String, Object>> page, @Param("key") String key);

    /**
     * 获取tb_area_business表全部字段
     *
     * @return 查询结果
     */
    List<BusinessFilterDTO> columnList();
    /**
     * 获取业务域数据列表--可根据筛选器
     *
     * @return 查询结果
     */
    Page<BusinessPageResultDTO> queryList(Page<BusinessPageResultDTO> page, @Param("query") BusinessPage query);

}
