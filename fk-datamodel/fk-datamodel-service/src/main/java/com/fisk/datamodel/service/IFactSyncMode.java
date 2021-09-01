package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.factsyncmode.FactSyncModeDTO;
import com.fisk.datamodel.dto.factsyncmode.FactSyncModePushDTO;

/**
 * @author JianWenYang
 */
public interface IFactSyncMode {

    /**
     * 添加事实同步方式
     * @param dto
     * @return
     */
    ResultEnum addFactSyncMode(FactSyncModeDTO dto);

    /**
     * 获取事实同步详情
     * @param factId
     * @return
     */
    FactSyncModeDTO getFactSyncMode(int factId);

    /**
     * 修改事实同步方式
     * @param dto
     * @return
     */
    ResultEnum updateFactSyncMode(FactSyncModeDTO dto);

    /**
     * 获取推送事实表推送数据信息
     * @param id
     * @return
     */
    FactSyncModePushDTO factSyncModePush(long id);
}
