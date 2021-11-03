package com.fisk.dataaccess.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.v3.DataSourceDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author Lock
 */
@Mapper
public interface AppDataSourceMapper extends FKBaseMapper<AppDataSourcePO> {

    @Select("SELECT id,drive_type FROM tb_app_datasource WHERE del_flag = 1 AND app_id = #{app_id};")
    DataSourceDTO getDataSource(@Param("app_id") long appId);

}
