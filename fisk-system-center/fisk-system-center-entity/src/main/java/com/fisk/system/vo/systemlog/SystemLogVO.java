package com.fisk.system.vo.systemlog;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author lishiji
 */
@Data
public class SystemLogVO implements Serializable {

    /**
     * 文件名称
     */
    public String logName;

    /**
     * 最后更新时间
     */
    public String lastUpdateTime;

    /**
     * 当天的日志集合，集合里面装载的集合的每个数据代表日志的每一行
     */
    public List<String> logList;

}
