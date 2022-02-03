package com.fisk.dataservice.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.FieldConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * api 字段 mapper
 *
 * @author dick
 */
@Mapper
public interface ApiFieldMapper extends FKBaseMapper<FieldConfigPO> {
    /**
     * 根据apiId修改字段有效性标记
     *
     * @param apiId apiId
     * @return 操作结果
     */
    @Update("UPDATE tb_field_config SET del_flag=0 WHERE api_id=#{apiId} AND del_flag=1;")
    int updateByApiId(@Param("apiId") int apiId);

    /**
     * 根据apiId集合查询字段信息
     *
     * @param apiIds api集合
     * @return 操作结果
     */
    List<FieldConfigPO> getListByApiIds(@Param("apiIds") List<Integer> apiIds);
}
