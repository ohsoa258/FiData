package com.fisk.datamodel.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryQueryDTO;
import com.fisk.task.dto.DwLogResultDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ITableHistory {

    /**
     * 添加表历史记录
     *
     * @param dto
     * @return
     */
    ResultEnum addTableHistory(List<TableHistoryDTO> dto);

    /**
     * 根据参数,获取发布列表
     *
     * @param dto
     * @return
     */
    List<TableHistoryDTO> getTableHistoryList(TableHistoryQueryDTO dto);

    /**
     * 获取数仓表单表发布时，nifi的同步情况：日志+报错信息
     *
     * @param dto
     * @return
     */
    DwLogResultDTO getDwPublishNifiStatus(TableHistoryQueryDTO dto);
}
