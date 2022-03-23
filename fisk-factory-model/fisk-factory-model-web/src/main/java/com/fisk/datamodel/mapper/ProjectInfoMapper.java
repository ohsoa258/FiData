package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamodel.entity.ProjectInfoPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Mapper
public interface ProjectInfoMapper extends BaseMapper<ProjectInfoPO> {

    /**
     * 分页查询
     *
     * @param key  搜索条件
     * @param page 分页对象
     * @return 查询结果
     */

    @Select("SELECT\n" +
            "\tb.id,\n" +
            "\ta.business_name,\n" +
            "\tb.project_name,\n" +
            "\tb.project_des,\n" +
            "\tb.project_principal,\n" +
            "\tb.principal_email,\n" +
            "\tb.project_flag\n" +
            "FROM `tb_area_business` a\n" +
            "LEFT JOIN `tb_project_info` AS b \n" +
            "ON a.id = b.business_id\n" +
            "WHERE business_name LIKE concat('%',#{key},'%') \n" +
            "AND b.del_flag = 1\n" +
            "ORDER BY b.update_time DESC ")
    List<Map<String, Object>> queryByPage(Page<Map<String,Object>> page, @Param("key") String key);

}
