package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.dataaccess.dto.v3.DataSourceDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;

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
    DataSourceDTO getDataSourceMeta(long appId);

    /**
     * 根据appId重新加载所有数据源以及数据库、表数据
     *
     * @param appId appId
     * @return dto
     */
    DataSourceDTO setDataSourceMeta(long appId);
}
