package com.fisk.datafactory.dto.dataaccess;

import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataAccessIdDTO {
    public long appId;
    public long tableId;
    /**
     * Timer driven OR CRON driven
     */
    public String syncMode;
    /**
     * 表达式 OR 秒
     */
    public String expression;
    /**
     * nifi流程回写的应用组件id
     */
    public String appComponentId;
    /**
     * nifi流程回写的物理表组件
     */
    public String tableComponentId;

    /**
     * 调度组件id
     */
    public String schedulerComponentId;

    /*
    * 数据类别
    * */
    public OlapTableEnum olapTableEnum;

    /**
     * 事实表名
     */
    public String factTableName;
}
