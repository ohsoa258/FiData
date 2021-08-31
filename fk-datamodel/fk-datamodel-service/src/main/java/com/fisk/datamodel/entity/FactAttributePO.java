package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@TableName("tb_fact_attribute")
@Data
@EqualsAndHashCode(callSuper = true)
public class FactAttributePO extends BasePO {
    /**
     * 事实表id
     */
    public int factId;
    /**
     *表来源字段id
     */
    public int tableSourceFieldId;
    /**
     * 业务过程字段表中文字段名称
     */
    public String factFieldCnName;
    /**
     * 业务过程字段表字段类型
     */
    public  String factFieldType;
    /**
     * 业务过程字段表字段长度
     */
    public int factFieldLength;
    /**
     * 业务过程字段表字段描述
     */
    public String factFieldDes;
    /**
     * 业务过程字段表英文字段名称
     */
    public String factFieldEnName;
    /**
     * 属性类型：1、事实属性,2、关联维度,3、度量
     */
    public int attributeType;
    /**
     * 关联维度id
     */
    public int associateDimensionId;
    /**
     * 关联维度字段id
     */
    public int associateDimensionFieldId;
    /**
     * 关联维度id
     */
    public int associateId;

}
