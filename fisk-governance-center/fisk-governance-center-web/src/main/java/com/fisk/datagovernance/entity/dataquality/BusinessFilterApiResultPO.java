package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗，API清洗返回结果
 * @date 2022/10/8 15:32
 */
@Data
@TableName("tb_bizfilter_api_result")
public class BusinessFilterApiResultPO extends BasePO {
    /**
     * tb_bizfilter_api_config表主键ID
     */
    public int apiId;

    /**
     * 结果参数类型 1：授权result参数  2：正文result参数
     */
    public int resultParmType;

    /**
     * 源字段
     */
    public String sourceField;

    /**
     * 目标字段
     */
    public String targetField;

    /**
     * 目标字段标识
     */
    public String targetFieldUnique;

    /**
     * 父级参数id
     */
    public int parentId;

    /**
     * 授权字段 1:是  2:否
     */
    public int authField;

    /**
     * 更新标识字段
     */
    public String primaryKeyField;
}
