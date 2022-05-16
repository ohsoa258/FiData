package com.fisk.datagovernance.vo.dataquality.notice;

import com.fisk.datagovernance.enums.dataquality.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 通知VO
 * @date 2022/3/22 15:38
 */
@Data
public class NoticeVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 通知名称
     */
    @ApiModelProperty(value = "通知名称")
    public String noticeName;

    /**
     * 通知类型
     */
    @ApiModelProperty(value = "通知类型")
    public NoticeTypeEnum noticeType;

    /**
     * 邮件配置表id
     */
    @ApiModelProperty(value = "邮件配置表id")
    public int emailServerId;

    /**
     * 邮件主题
     */
    @ApiModelProperty(value = "邮件主题")
    public String emailSubject;

    /**
     * 邮件收件人
     */
    @ApiModelProperty(value = "邮件收件人")
    public String emailConsignee;

    /**
     * 邮件抄送人
     */
    @ApiModelProperty(value = "邮件抄送人")
    public String emailCc;

    /**
     * 通知正文
     */
    @ApiModelProperty(value = "通知正文")
    public String body;

    /**
     * 运行时间cron表达式
     */
    @ApiModelProperty(value = "运行时间cron表达式")
    public String runTimeCron;

    /**
     * 通知状态：1、启用 0、禁用
     */
    @ApiModelProperty(value = "通知状态")
    public RuleStateEnum noticeState;

    /**
     * 模块类型
     * 100、数据校验 200、业务清洗
     * 300、生命周期 400、告警设置
     */
    @ApiModelProperty(value = "模块类型")
    public ModuleTypeEnum moduleType;

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
    public String templatenName;

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

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;
}
