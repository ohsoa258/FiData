package com.fisk.dataaccess.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.savepointhistory.SavepointHistoryDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ISavepointHistory {

    /**
     * 添加检查点历史记录
     *
     * @param dto
     * @return
     */
    ResultEnum addSavepointHistory(SavepointHistoryDTO dto);

    /**
     * 获取检查点历史记录
     *
     * @param tableAccessId
     * @return
     */
    List<SavepointHistoryDTO> getSavepointHistory(long tableAccessId);

}
