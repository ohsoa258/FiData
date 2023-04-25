package com.fisk.datamodel.entity.fact;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
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
     * 属性类型：0:退化维度，1:维度建，2:度量字段
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
     * 源表名称
     */
    public String sourceTableName;
    /**
     * 源字段名称
     */
    public String sourceFieldName;

    /**
     * 配置详情(维度key的json配置详情)
     */
    public String configDetails;

    /**
     * 是否是业务覆盖表示字段 1：是  0：不是
     */
    public int isBusinessKey;
}
