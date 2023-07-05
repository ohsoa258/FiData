package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * api请求参数表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-04-26 11:07:14
 */
@Data
@TableName("tb_api_parameter")
@EqualsAndHashCode(callSuper = true)
public class ApiParameterPO extends BasePO {

    /**
     * tb_api_config(id)
     */
    public long apiId;

    /**
     * Headers or Body
     */
    public String requestType;

    /**
     * form-data or raw
     */
    public String requestMethod;

    /**
     * 请求参数key
     */
    public String parameterKey;

    /**
     * 请求参数value
     */
    public String parameterValue;

    /**
     * 参数类型：1常量 2表达式 3脚本
     */
    public Integer parameterType;

    /**
     * 参数类型为表达式,表达式的类型为聚合函数,聚合字段所属的表id
     */
    public Integer tableAccessId;

    /*
    * 1字段，2表。
    * */
    public Integer attributeType;

    /**
     * 当选择body时: 字段名称
     */
    public String attributeFieldName;

    /**
     * 当选择body时: 值类型，值数组，字符串，对象组
     */
    public String attributeFieldType;

    /**
     *当选择body时: 推送规则
     */
    public String attributeFieldRule;

    /*
    * 当选择body时: 描述
    * */
    public String attributeFieldDesc;

    /*
    * 当选择body时: 字段的样例数据
    * */
    public String attributeFieldSample;

    /*当选择body时: 字段的上一级字段*/
    public String attributeFieldParent;
}
