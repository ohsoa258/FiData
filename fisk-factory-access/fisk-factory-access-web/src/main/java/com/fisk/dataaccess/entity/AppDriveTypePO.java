package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_app_drivetype")
@EqualsAndHashCode(callSuper = true)
public class AppDriveTypePO extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    private long id;

    /**
     * 数据源名称: key
     */
    private String name;

    /**
     * 模板
     */
    public String connectStr;

    /**
     * 类型: 0数据库 1非数据库
     */
    public Integer type;

}
