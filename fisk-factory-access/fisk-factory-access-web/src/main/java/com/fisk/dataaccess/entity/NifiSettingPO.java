package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lock
 */
@Data
public class NifiSettingPO implements Serializable {
    /**
     * 应用注册id
     */
    @TableId(type = IdType.INPUT)
    public long tableId;

    /**
     * 物理表id
     */
    public long appId;

    /**
     * 应用注册componentId
     */
    public String appGroupId;
    /**
     * 物理表componentId
     */
    public String tableGroupId;

    /**
     * doris SelectSql
     */
    public String selectSql;

    /**
     * doris tableName
     */
    public String tableName;


}
