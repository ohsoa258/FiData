package com.fisk.datafactory.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.app.AppDriveTypeDTO;
import com.fisk.datafactory.dto.taskdatasourceconfig.TaskDataSourceConfigDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ITaskDataSourceConfig {

    /**
     * 测试连接,成功返回数据库名集合
     *
     * @param dto
     * @return
     */
    List<String> testConnection(TaskDataSourceConfigDTO dto);

    /**
     * 获取驱动类型
     *
     * @return
     */
    List<AppDriveTypeDTO> getDrive();

    /**
     * 新增任务数据源配置
     *
     * @param dto
     * @return
     */
    ResultEnum addOrUpdateTaskDataSourceConfig(TaskDataSourceConfigDTO dto);

    /**
     * 获取详情
     *
     * @param taskId
     * @return
     */
    TaskDataSourceConfigDTO getTaskDataSourceConfig(Integer taskId);

}
