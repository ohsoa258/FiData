package com.fisk.dataaccess.dto.taskschedule;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataAccessIdsDTO {

    public Long appId;
    public Long tableId;
    /**
     * 区分维度 事实 指标;区分数据湖表  ftp  api
     */
    public int flag;
}
