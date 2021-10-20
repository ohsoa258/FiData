package com.fisk.datafactory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lock
 */
@Data
@TableName("tb_nifi_components")
public class NifiComponentsPO implements Serializable {
    public static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    public long id;
    public String name;
}
