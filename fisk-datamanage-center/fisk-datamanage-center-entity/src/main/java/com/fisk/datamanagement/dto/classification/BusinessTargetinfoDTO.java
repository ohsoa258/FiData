package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author xgf
 * @date 2023年11月20日 16:08
 */
@Data
public class BusinessTargetinfoDTO {
    public long  id;
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
}
