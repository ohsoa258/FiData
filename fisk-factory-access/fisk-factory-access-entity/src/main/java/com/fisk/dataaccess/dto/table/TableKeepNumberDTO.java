package com.fisk.dataaccess.dto.table;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableKeepNumberDTO {

    public Long id;
    /**
     * stg数据保留天数
     */
    public String keepNumber;

}
