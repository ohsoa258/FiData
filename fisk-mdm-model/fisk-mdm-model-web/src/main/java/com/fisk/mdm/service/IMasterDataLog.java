package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.masterdata.MasterDataDTO;
import com.fisk.mdm.dto.masterdatalog.MasterDataLogQueryDTO;
import com.fisk.mdm.vo.masterdatalog.MasterDataLogPageVO;

import java.util.Map;

/**
 * @author JianWenYang
 */
public interface IMasterDataLog {

    /**
     * 添加主数据维护日志
     *
     * @param data
     * @param tableName
     * @return
     */
    ResultEnum addMasterDataLog(Map<String, Object> data, String tableName);

    /**
     * 主数据维护日志数据列表
     *
     * @param dto
     * @return
     */
    MasterDataLogPageVO listMasterDataLog(MasterDataLogQueryDTO dto);

    /**
     * 主数据维护日志回滚
     *
     * @param dto
     * @return
     */
    ResultEnum rollBackMasterData(MasterDataDTO dto);

}
