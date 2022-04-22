package com.fisk.datagovernance.entity.dataops;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维PO
 * @date 2022/4/22 11:32
 */
@Data
@TableName("tb_dataops_logs")
public class DataOpsLogPO extends BasePO {
    /**
     * ip
     */
    public String conIp;

    /**
     * 数据库名称
     */
    public String conDbname;

    /**
     * 执行的sql
     */
    public String executeSql;

    /**
     * 执行结果 200:成功 500:失败
     */
    public int executeResult;

    /**
     * 执行消息
     */
    public String executeMsg;

    /**
     * 执行人
     */
    public String executeUser;
}
