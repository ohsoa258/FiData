package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryQueryDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ITableHistory {

    /**
     * 添加表历史记录
     * @param dto
     * @return
     */
    ResultEnum addTableHistory(TableHistoryDTO dto);

    /**
     * 根据参数,获取发布列表
     * @param dto
     * @return
     */
    List<TableHistoryDTO> getTableHistoryList(TableHistoryQueryDTO dto);

}
