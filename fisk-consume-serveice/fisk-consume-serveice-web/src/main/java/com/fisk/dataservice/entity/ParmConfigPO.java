package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 参数实体类
 * @date 2022/1/6 14:51
 */
@Data
@TableName("tb_parm_config")
public class ParmConfigPO extends BasePO
{
    /**
     * apiId
     */
    public int apiId;

    /**
     * 参数名称
     */
    public String parmName;

    /**
     * 参数描述
     */
    public String parmDesc;

    /**
     * 参数值
     */
    public String parmValue;

    /**
     * 是否是内置参数 1是、0否
     * @TableField(select = false) 查询时忽略此字段，但修改新增时任然存在
     * @TableField(exist = false) 注解加载bean属性上，表示当前属性不是数据库的字段，但在项目中必须使用，这样在新增等使用bean的时候，mybatis-plus就会忽略这个，不会报错
     */
    @TableField(exist = false)
    public int parmIsbuiltin;
}
