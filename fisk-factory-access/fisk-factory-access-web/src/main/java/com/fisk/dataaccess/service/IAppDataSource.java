package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.datasource.DataSourceInfoDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.fisk.dataaccess.dto.v3.DataSourceDTO;
import com.fisk.dataaccess.dto.v3.SourceColumnMetaQueryDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;

import java.util.List;

/**
 * @author Lock
 */
public interface IAppDataSource extends IService<AppDataSourcePO> {
    /**
     * 获取所有数据源以及数据库、表数据
     *
     * @param appId appId
     * @return dto
     */
    List<DataSourceDTO> getDataSourceMeta(long appId);

    /**
     * 根据appId重新加载所有数据源以及数据库、表数据
     *
     * @param appId appId
     * @return dto
     */
    DataSourceDTO setDataSourceMeta(long appId, long appDataSourceId);

    /**
     * 根据服务配置信息,获取所有的数据库名称
     *
     * @param dto dto
     * @return 数据库集合
     */
    List<String> getDatabaseNameList(AppDataSourceDTO dto);

    /**
     * 根据表名或视图名获取字段集合
     *
     * @param dto
     * @return
     */
    List<TableStructureDTO> getSourceColumnMeta(SourceColumnMetaQueryDTO dto);

    /**
     * 根据应用id获取数据源集合
     *
     * @param appId
     * @return
     */
    List<DataSourceInfoDTO> getDataSourcesByAppId(Integer appId);

    /**
     * 根据数据源类型获取平台配置模块的外部数据源
     *
     * @param driverType
     * @return
     */
    List<com.fisk.system.dto.datasource.DataSourceDTO> getOutDataSourcesByTypeId(String driverType);

    /**
     * 根据数据源id获取单个平台配置模块的外部数据源详情
     *
     * @param Id
     * @return
     */
    ResultEntity<com.fisk.system.dto.datasource.DataSourceDTO> getOutSourceById(Integer Id);

    /**
     * 获取指定app下的非重复驱动类型
     *
     * @param id
     * @return
     */
    List<AppDataSourcePO> getDataSourceDrivesTypeByAppId(Long id);

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 配合task模块，当平台配置修改数据源信息时，数据接入引用的数据源信息一并修改
     *
     * @param dto
     * @return
     */
    Boolean editDataSource(DataSourceSaveDTO dto);

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 根据SystemDataSourceId获取数据接入引用的数据源信息
     *
     * @param id
     * @return
     */
    List<AppDataSourceDTO> getDataSourcesBySystemDataSourceId(Integer id);


    /**
     * 获取数据接入引用的数据源id
     *
     * @param id
     * @return
     */
    AppDataSourceDTO getAccessDataSources(Long id);

    /**
     * @param id
     * @return
     */
    ResultEntity<com.fisk.system.dto.datasource.DataSourceDTO> getSystemDataSourceById(Integer id);

    /**
     * 数据接入，刷新redis里存储的表信息
     *
     * @param appId
     * @return
     */
    List<DataSourceDTO> refreshRedis(long appId);

    /**
     * 通过应用id获取应用引用的所有数据源信息
     *
     * @param appId
     * @return
     */
    List<AppDataSourceDTO> getAppSourcesByAppId(long appId);
}
