package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_wide_table_config")
@EqualsAndHashCode(callSuper = true)
public class WideTableConfigPO extends BasePO {
    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 宽表名称
     */
    public String name;
    /**
     * 宽表sql脚本
     */
    public String sqlScript;
    /**
     * 配置详情
     */
    public String configDetails;
}
