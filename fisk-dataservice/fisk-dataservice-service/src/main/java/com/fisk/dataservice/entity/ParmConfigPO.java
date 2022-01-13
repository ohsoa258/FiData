package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
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
    public String apiId;

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
}
