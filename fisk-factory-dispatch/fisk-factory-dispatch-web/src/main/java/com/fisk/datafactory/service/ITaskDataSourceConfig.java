package com.fisk.datafactory.service;

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

}
