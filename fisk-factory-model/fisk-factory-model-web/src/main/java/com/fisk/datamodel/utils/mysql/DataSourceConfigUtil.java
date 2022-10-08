package com.fisk.datamodel.utils.mysql;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author JianWenYang
 */
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
    public Connection getStatement() {
        DataSourceDTO dwSource = getDwSource();
        Connection conn;
        try {
            Class.forName(dwSource.conType.getDriverName());
            conn = DriverManager.getConnection(dwSource.conStr, dwSource.conAccount, dwSource.conPassword);
        } catch (Exception e) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, e);
        }
        return conn;
    }

}
