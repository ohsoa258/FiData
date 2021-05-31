package com.fisk.chartvisual.vo;

import lombok.Data;

import java.util.List;

/**
 * @author gy
 */
@Data
public class DataDomainVO {

    public String name;
    public String details;

    public List<DataDomainVO> children;

    public DataDomainVO(){

    }

    public DataDomainVO(String name, String details) {
        this.name = name;
        this.details = details;
    }
}
