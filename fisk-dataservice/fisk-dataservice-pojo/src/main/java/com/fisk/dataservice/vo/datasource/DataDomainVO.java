package com.fisk.dataservice.vo.datasource;

import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description DataDomainVO
 * @date 2022/1/6 14:51
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
