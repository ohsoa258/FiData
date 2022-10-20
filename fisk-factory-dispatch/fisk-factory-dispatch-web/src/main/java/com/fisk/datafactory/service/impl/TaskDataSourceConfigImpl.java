package com.fisk.datafactory.service.impl;

import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.datafactory.dto.taskdatasourceconfig.TaskDataSourceConfigDTO;
import com.fisk.datafactory.service.ITaskDataSourceConfig;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class TaskDataSourceConfigImpl implements ITaskDataSourceConfig {

    @Override
    public List<String> testConnection(TaskDataSourceConfigDTO dto) {
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        commonDbHelper.connection(dto.connectStr, dto.connectAccount, dto.connectPwd, dto.type);
        return null;
    }

}
