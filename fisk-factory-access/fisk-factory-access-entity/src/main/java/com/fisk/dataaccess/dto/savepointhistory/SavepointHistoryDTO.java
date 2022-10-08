package com.fisk.dataaccess.dto.savepointhistory;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class SavepointHistoryDTO {

    public Long id;

    public Long tableAccessId;

    /**
     * 检查点路径
     */
    public String savepointPath;

    /**
     * 检查点时间
     */
    public LocalDateTime savepointDate;

}
