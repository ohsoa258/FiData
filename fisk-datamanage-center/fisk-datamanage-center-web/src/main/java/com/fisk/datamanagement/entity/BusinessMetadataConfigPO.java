package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_business_metadata_config")
@EqualsAndHashCode(callSuper = true)
public class BusinessMetadataConfigPO extends BasePO {

    /**
     * 业务元数据名称
     */
    public String businessMetadataName;

    /**
     * 业务元数据中文名称
     */
    public String businessMetadataCnName;

    /**
     * 业务元数据属性名称
     */
    public String attributeName;

    /**
     * 业务元数据属性中文名称
     */
    public String attributeCnName;

    /**
     * 适用元数据类型:rdbms_instance、rdbms_db、rdbms_table、rdbms_column
     */
    public String suitableType;

    /**
     * 属性是否支持多值
     */
    public Boolean multipleValued;

    /**
     * 属性类型:string、int、boolean
     */
    public String attributeType;

}
