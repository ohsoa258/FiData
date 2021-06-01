package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;

/**
 * @author: Lock
 * @data: 2021/5/26 14:31
 */
@Data
@TableName("tb_app_drivetype")
public class AppDriveTypePO extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    private String id;

    /**
     * 数据源名称: key
     */
    private String name;

    /**
     * 数据源类型标识: value,到时候用的也是type
     */
    private String type;
}
