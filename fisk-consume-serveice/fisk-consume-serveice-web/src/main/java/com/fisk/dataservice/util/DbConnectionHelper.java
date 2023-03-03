package com.fisk.dataservice.util;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;

import java.sql.Connection;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
public class DbConnectionHelper {

    public static Connection connection(String connectionStr, String acc, String pwd, DataSourceTypeEnum type) {
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        return commonDbHelper.connection(connectionStr, acc, pwd, type);
    }

}
