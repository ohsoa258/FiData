package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author: Lock
 * @data: 2021/5/26 14:31
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
