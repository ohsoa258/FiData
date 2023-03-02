package com.fisk.datamodel.utils.mysql;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;

/**
 * @author JianWenYang
 */
@Component
public class DataSourceConfigUtil {

    @Resource
    UserClient userClient;
    @Value("${fiData-data-dw-source}")
    private Integer dwSource;

    /**
     * 获取ods数据源配置
     *
     * @return
     */
    public DataSourceDTO getDwSource() {
        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(dwSource);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        return dataSourceConfig.data;
    }

    /**
     * 连接数据库
     *
     * @return statement
     */
    public Connection getConnection() {
        DataSourceDTO dwSource = getDwSource();
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        return commonDbHelper.connection(dwSource.conStr, dwSource.conAccount, dwSource.conPassword, dwSource.conType);
    }

}
