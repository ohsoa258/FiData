package com.fisk.dataaccess.dto.table;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BatchPublishDTO {

    public List<Long> ids;

    public boolean openTransmission;

    public String currUserName;
    public List<TableHistoryDTO> tableHistorys;

}
