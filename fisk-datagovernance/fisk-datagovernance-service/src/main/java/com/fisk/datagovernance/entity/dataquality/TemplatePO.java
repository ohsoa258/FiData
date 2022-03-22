package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 模板配置表
 * @date 2022/3/22 14:51
 */
@Data
@TableName("tb_template_config")
public class TemplatePO extends BasePO
{
    /**
     * 模板名称
     */
    public String templatenName;

    /**
     * 模板模块
     */
    public int templateModules;

    /**
     * 模板类型
     */
    public int templateType;

    /**
     * 模板描述
     */
    public String templateDesc;
}

