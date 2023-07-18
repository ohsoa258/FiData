package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
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
     * 模块类型
     * 100、数据校验
     * 200、业务清洗
     * 300、生命周期
     */
    public int moduleType;

    /**
     * 模块名称
     */
    public String moduleName;

    /**
     * 模板类型
     * 101、空值检查
     * 102、值域检查
     * 103、规范检查
     * 104、重复数据检查
     * 105、波动检查
     * 106、血缘检查
     * 107、正则表达式检查
     * 108、SQL脚本检查
     */
    public int templateType;

    /**
     * 模板展示顺序
     */
    public int templateSort;

    /**
     * 模板名称
     */
    public String templateName;

    /**
     * 模板图标
     */
    public String templateIcon;

    /**
     * 模板描述
     */
    public String templateDesc;

    /**
     * 模板状态：1 启用、2 开发中、3 禁用
     */
    public int templateState;
}

