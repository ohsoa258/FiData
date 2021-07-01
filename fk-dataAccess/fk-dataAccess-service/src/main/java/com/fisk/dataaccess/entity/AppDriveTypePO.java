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
@TableName("tb_app_drivetype")
public class AppDriveTypePO implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id",type = IdType.INPUT)
    private long id;

    /**
     * 数据源名称: key
     */
    private String name;

}
