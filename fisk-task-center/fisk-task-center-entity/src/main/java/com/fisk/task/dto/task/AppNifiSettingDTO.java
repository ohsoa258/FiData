package com.fisk.task.dto.task;

import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.po.AppNifiSettingPO;

/**
 * @author cfk
 */
public class AppNifiSettingDTO extends AppNifiSettingPO {
    /*
     * 表类别
     * */
    public OlapTableEnum tableType;
    /*
     * 表id
     * */
    public Integer tableId;
}
