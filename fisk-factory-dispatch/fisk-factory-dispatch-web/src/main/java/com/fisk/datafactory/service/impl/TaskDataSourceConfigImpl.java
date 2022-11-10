package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DbCommonHelperUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.common.BuildCommonHelper;
import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.app.AppDriveTypeDTO;
import com.fisk.datafactory.dto.taskdatasourceconfig.TaskDataSourceConfigDTO;
import com.fisk.datafactory.entity.TaskDataSourceConfigPO;
import com.fisk.datafactory.map.TaskDataSourceConfigMap;
import com.fisk.datafactory.mapper.TaskDataSourceConfigMapper;
import com.fisk.datafactory.service.ITaskDataSourceConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class TaskDataSourceConfigImpl implements ITaskDataSourceConfig {

    @Resource
    TaskDataSourceConfigMapper mapper;
    @Resource
    DataAccessClient dataAccessClient;

    @Override
    public List<String> testConnection(TaskDataSourceConfigDTO dto) {
        DataSourceTypeEnum sourceTypeEnum = DataSourceTypeEnum.getEnum(dto.type.toUpperCase());
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        Connection connection = commonDbHelper.connection(dto.connectStr, dto.connectAccount, dto.connectPwd, sourceTypeEnum);

        IBuildCommonSqlCommand command = BuildCommonHelper.getCommand(sourceTypeEnum);
        String sql = command.buildAllDbSql();

        return DbCommonHelperUtils.getAllDatabases(connection, sql);
    }

    @Override
    public List<AppDriveTypeDTO> getDrive() {
        ResultEntity<List<AppDriveTypeDTO>> driveType = dataAccessClient.getDriveType();
        if (driveType.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_OPS_CONFIG_EXISTS);
        }
        List<AppDriveTypeDTO> collect = driveType.data
                .stream()
                .filter(e -> e.type == 0 && !"oracle-cdc".equals(e.name) && !"sap".equals(e.name))
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public ResultEnum addOrUpdateTaskDataSourceConfig(TaskDataSourceConfigDTO dto) {
        if (dto.id > 0) {
            TaskDataSourceConfigPO po = mapper.selectById(dto.id);
            if (po == null && !po.taskId.equals(dto.taskId)) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            return mapper.updateById(TaskDataSourceConfigMap.INSTANCES.dtoToPo(dto)) > 0 ? ResultEnum.SUCCESS : ResultEnum.DATA_SUBMIT_ERROR;
        }
        return mapper.insert(TaskDataSourceConfigMap.INSTANCES.dtoToPo(dto)) > 0 ? ResultEnum.SUCCESS : ResultEnum.DATA_SUBMIT_ERROR;
    }

    @Override
    public TaskDataSourceConfigDTO getTaskDataSourceConfig(Integer taskId) {
        QueryWrapper<TaskDataSourceConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TaskDataSourceConfigPO::getTaskId, taskId);
        TaskDataSourceConfigPO po = mapper.selectOne(queryWrapper);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return TaskDataSourceConfigMap.INSTANCES.poToDto(po);
    }

}
