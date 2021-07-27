package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lock
 */
@Data
@TableName("tb_nifi_config")
public class NifiConfigPO implements Serializable {

    @TableId(type = IdType.AUTO)
    public long id;

    /**
     * 唯一key
     */
    public String key;

    /**
     * cfgDbPoolComponentId
     */
    public String value;

}
