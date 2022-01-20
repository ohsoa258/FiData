package com.fisk.chartvisual.vo;

import com.fisk.chartvisual.enums.DimensionTypeEnum;
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
    public DimensionTypeEnum dimensionType;
    public String details;
    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimension;
    public List<DataDomainVO> children;
    public List<DataDomainVO> children1;

    public DataDomainVO(){

    }

    public DataDomainVO(String name, String details) {
        this.name = name;
        this.details = details;
    }

    public DataDomainVO(Long id, String name, int dimension) {
        this.id = id;
        this.name = name;
        this.dimension = dimension;
    }
}
