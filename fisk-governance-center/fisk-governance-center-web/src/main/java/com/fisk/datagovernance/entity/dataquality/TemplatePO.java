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
     * 100、数据校验 200、业务清洗
     * 300、生命周期 400、告警设置
     */
    public int moduleType;

    /**
     * 模块名称
     */
    public String moduleName;

    /**
     * 模板应用场景
     * 100、页面校验
     * 101、同步校验
     * 102、质量报告
     * 200、同步清洗
     * 201、清洗报告
     * 300、生命周期报告
     * 400、数据校验告警
     * 401、业务清洗告警
     * 402、生命周期告警
     */
    public  int templateScene;

    /**
     * 应用场景名称
     */
    public String sceneName;

    /**
     * 应用场景描述
     */
    public String sceneDesc;

    /**
     * 模板名称
     */
    public String templateName;

    /**
     * 模板类型
     * 100、字段规则模板
     * 101、字段聚合波动阈值模板
     * 102、表行数波动阈值模板
     * 103、空表校验模板
     * 104、表更新校验模板
     * 105、表血缘断裂校验模板
     * 106、业务验证模板
     * 200、业务清洗模板
     * 300、指定时间回收模板
     * 301、空表回收模板
     * 302、数据无刷新回收模板
     * 303、数据血缘断裂回收模板
     * 400、邮件通知模板
     * 401、站内消息模板
     */
    public int templateType;

    /**
     * 模板描述
     */
    public String templateDesc;
}

