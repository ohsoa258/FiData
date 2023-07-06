package com.fisk.datafactory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Lock
 */
@Data
@TableName("tb_nifi_custom_workflow_detail")
public class NifiCustomWorkflowDetailPO extends BasePO implements Serializable {
    /**
     * 父组件
     */
    public Long pid;
    public String workflowId;
    public Integer componentsId;
    public String appId;
    public String tableId;
    public Integer tableOrder;
    /**
     * 常规: 名称
     */
    public String componentName;
    /**
     * 常规: 类型
     */
    public String componentType;
    /**
     * 常规: 描述
     */
    public String componentDesc;
    public Double componentX;
    public Double componentY;
    /**
     * 元数据对象
     */
    public String metadataObj;
    public Integer schedule;
    public String script;
    /**
     * 左边指右边,左:outport,右:inport
     */
    public String inport;
    public String outport;

    public Boolean flag;

    public int tableType;
    /**
     * 自定义脚本
     */
    public String customScript;
    /**
     * 数据源id
     */
    public Integer dataSourceId;
    /**
     * 是否禁用
     */
    public Boolean forbidden;
    /**
     * 是否是内置参数 1是、0否
     * @TableField(select = false) 查询时忽略此字段，但修改新增时任然存在
     * @TableField(exist = false) 注解加载bean属性上，表示当前属性不是数据库的字段，但在项目中必须使用，这样在新增等使用bean的时候，mybatis-plus就会忽略这个，不会报错
     */
    @TableField(exist = false)
    public Map<String,String> taskSetting;
}
