package com.fisk.datafactory.service.impl;

import com.fisk.common.core.utils.DbCommonHelperUtils;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.common.BuildCommonHelper;
import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.datafactory.dto.taskdatasourceconfig.TaskDataSourceConfigDTO;
import com.fisk.datafactory.service.ITaskDataSourceConfig;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class TaskDataSourceConfigImpl implements ITaskDataSourceConfig {

    @Override
    public List<String> testConnection(TaskDataSourceConfigDTO dto) {
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        Connection connection = commonDbHelper.connection(dto.connectStr, dto.connectAccount, dto.connectPwd, dto.type);

        IBuildCommonSqlCommand command = BuildCommonHelper.getCommand(dto.type);
        String sql = command.buildAllDbSql();

        return DbCommonHelperUtils.getAllDatabases(connection, sql);
    }

}
