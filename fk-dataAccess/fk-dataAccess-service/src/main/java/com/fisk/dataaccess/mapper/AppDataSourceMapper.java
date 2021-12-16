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

    /**
     * 根据应用id获取数据源类型
     *
     * @param appId appId
     * @return po
     */
    @Select("SELECT id,drive_type FROM tb_app_datasource WHERE del_flag = 1 AND app_id = #{app_id};")
    DataSourceDTO getDataSource(@Param("app_id") long appId);

}
