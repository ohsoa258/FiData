package com.fisk.datamanagement.dto.classification;

import lombok.Data;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author xgf
 * @date 2023年11月20日 16:08
 */
@Data
public class BusinessTargetinfoDTO {
    public long id;
    public String pid;
    public String responsibleDept;
    public String indicatorCode;
    public String indicatorName;
    public String indicatorDescription;
    public String indicatorLevel;
    public String unitMeasurement;
    public String statisticalCycle;
    public String indicatorformula;
    public String indicatorStatus;
    public String filteringCriteria;
    public String largeScreenLink;
    public String dataGranularity;
    public String operationalAttributes;
    public String sourceSystem;
    public String sourceDataTable;
    public String sourceIndicators;
    public String orderChannel;
    public String indicatorType;
    public String attributesNumber;
    public String name;
    public String sqlScript;
    public String dimdomaintype;
    public String dimdomainid;
    public String dimdomain;
    public String dimtableid;
    public String dimtable;
    public String attributeid;
    public String attribute;
    public String indexid;
    public List<BusinessExtendedfieldsDTO> dimensionData;
    public List<FacttreeListDTO> facttreeListData;
    public DateTime createdTime;
    public String createdUser;

    public Integer parentBusinessId;

}
