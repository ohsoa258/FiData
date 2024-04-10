package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.datamanagement.dto.classification.BusinessExtendedfieldsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author xgf
 * @date 2023年11月22日 16:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_business_targetinfo")
public class BusinessTargetinfoPO extends BasePO {
    public String  pid;
    public String  responsibleDept;
    public String  indicatorCode;
    public String  indicatorName;
    public String  indicatorDescription;
    public String  indicatorLevel;
    public String  unitMeasurement;
    public String  statisticalCycle;
    public String  indicatorformula;
    public String  indicatorStatus;
    public String  filteringCriteria;
    public String  largeScreenLink;
    public String  dataGranularity;
    public String  operationalAttributes;
    public String  sourceSystem;
    public String  sourceDataTable;
    public String  sourceIndicators;
    public String  orderChannel;
    public String  indicatorType;
    public String  attributesNumber;
    public String  name;
    public String  sqlScript;
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    public Integer parentBusinessId;
    /*public List<BusinessExtendedfieldsPO> businessExtendedfieldsPOdata;
    public List<FactTreePOs> factTreePOsdata;*/


}
