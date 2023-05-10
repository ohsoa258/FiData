package com.fisk.mdm.service;


import com.fisk.mdm.dto.access.TableHistoryDTO;

import java.util.List;

/**
 * @author Lock
 */
public interface ITableHistory {

    /**
     * 添加表历史记录
     * @param dto
     * @return
     */
    long addTableHistory(List<TableHistoryDTO> dto);

    /**
     * 根据参数,获取发布列表
     * @param dto
     * @return
     */
    List<TableHistoryDTO> getTableHistoryList(TableHistoryDTO dto);

}
