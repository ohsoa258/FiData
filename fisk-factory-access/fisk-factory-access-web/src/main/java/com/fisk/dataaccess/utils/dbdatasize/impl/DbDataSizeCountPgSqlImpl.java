package com.fisk.dataaccess.utils.dbdatasize.impl;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.dataaccess.utils.dbdatasize.IBuildFactoryDbDataSizeCount;
import com.fisk.dataaccess.utils.sql.DbConnectionHelper;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class DbDataSizeCountPgSqlImpl implements IBuildFactoryDbDataSizeCount {

    /**
     * 获取数据接入-ods库当前存储的数据大小 gb
     *
     * @return
     */
    @Override
    public String DbDataStoredSize(DataSourceDTO data) {
        String conStr = data.getConStr();
        String conAccount = data.getConAccount();
        String conPassword = data.getConPassword();
        DataSourceTypeEnum conType = data.getConType();
        String conDbname = "'" + data.getConDbname() + "'";
        String totalSize = "0";
        Connection connection = DbConnectionHelper.connection(conStr, conAccount, conPassword, conType);
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT pg_size_pretty(pg_database_size(" + conDbname + ")) AS total_size;");


            if (resultSet.next()) {
                totalSize = resultSet.getString("total_size");
            }

            resultSet.close();
            statement.close();
            connection.close();

            return totalSize;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbstractDbHelper.closeConnection(connection);
            AbstractDbHelper.closeStatement(statement);
            AbstractDbHelper.closeResultSet(resultSet);
        }
        return totalSize;
    }

}
