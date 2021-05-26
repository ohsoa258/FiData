package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Lock
 * @data: 2021/5/26 14:31
 */
@Data
@TableName("tb_app_drivetype")
public class AppDriveTypePO {

    /**
     * 主键
     */
    @TableId
    private String id;

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据源类型标识
     */
    private String type;
}
