package com.fisk.datagovernance.vo.dataquality.businessfilter;

import com.fisk.datagovernance.enums.dataquality.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗规则VO
 * @date 2022/3/22 15:36
 */
@Data
public class BusinessFilterVO {
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
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 表名称，页面展示
     */
    @ApiModelProperty(value = "表名称，页面展示")
    public String tableName;

    /**
     * 实际引用表名称
     */
    @ApiModelProperty(value = "实际引用表名称")
    public String useTableName;

    /**
     * 生成规则（清洗脚本）
     */
    @ApiModelProperty(value = "生成规则（清洗脚本）")
    public String createRule;

    /**
     * 规则执行顺序
     */
    @ApiModelProperty(value = "规则执行顺序")
    public int ruleSort;

    /**
     * 规则状态
     */
    @ApiModelProperty(value = "规则状态")
    public RuleStateEnum ruleState;

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
