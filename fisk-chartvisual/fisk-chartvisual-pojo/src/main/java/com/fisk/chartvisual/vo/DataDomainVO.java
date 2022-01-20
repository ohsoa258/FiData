package com.fisk.chartvisual.vo;

import com.fisk.chartvisual.enums.NodeTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author gy
 */
@Data
public class DataDomainVO {

    public Long id;
    public String name;
    public String uniqueName;
    public NodeTypeEnum dimensionType;
    public String details;
    public List<DataDomainVO> children;

    public DataDomainVO(){

    }

    public DataDomainVO(String name, String details) {
        this.name = name;
        this.details = details;
    }

    public DataDomainVO(Long id, String name, NodeTypeEnum dimensionType) {
        this.id = id;
        this.name = name;
        this.dimensionType = dimensionType;
    }
}
