package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 数据脱敏字段配置表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Data
@TableName("tb_columnsecurity_config")
@EqualsAndHashCode(callSuper = true)
public class ColumnsecurityConfigPO extends BasePO {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    public long id;

    /**
     * 缺省设置(0: 所有可读  1: 所有不可读)
     */
    public long defaultConfig;

    /**
     * 数据源id
     */
    public long datasourceId;

    /**
     * 表id
     */
    public long tableId;

    /**
     * 权限名称
     */
    public String permissionsName;

    /**
     * 权限描述
     */
    public String permissionsDes;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 是否有效
     */
    public Integer valid;

    /**
     * 创建人
     */
    public String createUser;

            }
