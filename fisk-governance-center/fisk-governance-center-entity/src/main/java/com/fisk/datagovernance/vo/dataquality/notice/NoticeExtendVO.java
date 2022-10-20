package com.fisk.datagovernance.vo.dataquality.notice;

import com.fisk.datagovernance.enums.dataquality.ModuleTypeEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateSceneEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateTypeEnum;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 通知扩展VO
 * @date 2022/3/22 15:37
 */
public class NoticeExtendVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 通知id
     */
    @ApiModelProperty(value = "通知id")
    public int noticeId;

    /**
     * 模块类型
     * 100、数据校验 200、业务清洗
     * 300、生命周期
     */
    @ApiModelProperty(value = "模块类型")
    public ModuleTypeEnum moduleType;

    /**
     * 规则id
     */
    @ApiModelProperty(value = "规则id")
    public int ruleId;

    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 模块名称
     */
    @ApiModelProperty(value = "模块名称")
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
    @ApiModelProperty(value = "模板应用场景")
    public TemplateSceneEnum templateScene;

    /**
     * 应用场景描述
     */
    @ApiModelProperty(value = "应用场景描述")
    public String sceneDesc;

    /**
     * 模板名称
     */
    @ApiModelProperty(value = "模板名称")
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
    @ApiModelProperty(value = "模板类型")
    public TemplateTypeEnum templateType;

    /**
     * 模板描述
     */
    @ApiModelProperty(value = "模板描述")
    public String templateDesc;
}
