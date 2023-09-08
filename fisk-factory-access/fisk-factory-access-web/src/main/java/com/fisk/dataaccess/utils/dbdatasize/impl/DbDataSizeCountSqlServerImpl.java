package com.fisk.dataaccess.utils.dbdatasize.impl;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.dataaccess.utils.dbdatasize.IBuildFactoryDbDataSizeCount;
import com.fisk.dataaccess.utils.sql.DbConnectionHelper;
import com.fisk.system.dto.datasource.DataSourceDTO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbDataSizeCountSqlServerImpl implements IBuildFactoryDbDataSizeCount {

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
        long totalSizeGB = 0;
        Connection connection = DbConnectionHelper.connection(conStr, conAccount, conPassword, conType);
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT SUM(size * 8) AS TotalSizeKB " +
                    "FROM sys.master_files " +
                    "WHERE type = 0 AND database_id = DB_ID(" + conDbname + "); ");

            if (resultSet.next()) {
                long totalSizeKB = resultSet.getLong("TotalSizeKB");
                long totalSizeMB = totalSizeKB / 1024;
                totalSizeGB = totalSizeMB / 1024;
            }

            resultSet.close();
            statement.close();
            connection.close();

            return totalSizeGB + "GB";
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbstractDbHelper.closeConnection(connection);
            AbstractDbHelper.closeStatement(statement);
            AbstractDbHelper.closeResultSet(resultSet);
        }
        return null;
    }

}
