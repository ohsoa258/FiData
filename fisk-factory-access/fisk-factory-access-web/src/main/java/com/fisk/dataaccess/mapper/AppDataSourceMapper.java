package com.fisk.dataaccess.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.v3.DataSourceDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 获取所有实时应用的
     *
     * @param realtimeAccount
     * @return list
     */
    @Select("SELECT app_id FROM tb_app_datasource WHERE del_flag=1 and realtime_account = #{realtime_account};")
    List<String> getRealtimeAccountList(@Param("realtime_account") String realtimeAccount);

    /**
     * 根据应用id获取驱动类型
     *
     * @param appId 应用id
     * @return 驱动类型
     */
    @Select("SELECT drive_type FROM tb_app_datasource WHERE app_id = #{app_id};")
    String getDriveTypeByAppId(@Param("app_id") Long appId);

    /**
     * 根据应用id获取数据源类型
     *
     * @param id
     * @return po
     */
    @Select("SELECT id,drive_type FROM tb_app_datasource WHERE del_flag = 1 AND id = #{id};")
    DataSourceDTO getDataSourceById(@Param("id") long id);

    @Select("SELECT id,drive_type FROM tb_app_datasource WHERE del_flag = 1 AND app_id = #{id};")
    List<DataSourceDTO> getDataSourceListById(@Param("id") long id);
}
