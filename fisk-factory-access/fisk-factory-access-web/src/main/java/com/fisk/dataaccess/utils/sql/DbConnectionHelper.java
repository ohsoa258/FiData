package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;

import java.sql.Connection;

/**
 * @author JianWenYang
 */
public class DbConnectionHelper {

    public static Connection connection(String connectionStr, String acc, String pwd, DataSourceTypeEnum type) {
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        return commonDbHelper.connection(connectionStr, acc, pwd, type);
    }

}
