package com.fisk.datamodel.entity.mainpage;

import lombok.Data;

@Data
public class TblAndRow {

    /**
     * 表名
     */
    private String tblName;

    /**
     * 行数
     */
    private Long rowCount;

}
