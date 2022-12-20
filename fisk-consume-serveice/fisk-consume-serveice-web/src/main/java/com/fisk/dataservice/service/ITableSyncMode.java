package com.fisk.dataservice.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tablesyncmode.ApiTableSyncModeDTO;
import com.fisk.dataservice.dto.tablesyncmode.TableSyncModeDTO;

/**
 * @author JianWenYang
 */
public interface ITableSyncMode {

    /**
     * 新增api服务调度
     *
     * @param dto
     * @return
     */
    ResultEnum addApiTableSyncMode(ApiTableSyncModeDTO dto);

    /**
     * 表服务调度配置
     *
     * @param dto
     * @return
     */
    ResultEnum tableServiceTableSyncMode(TableSyncModeDTO dto);

}
