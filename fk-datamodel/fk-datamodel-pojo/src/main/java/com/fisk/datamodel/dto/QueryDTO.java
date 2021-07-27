package com.fisk.datamodel.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class QueryDTO {
    /**
     *当前页数
     */
    public int page;
    /**
     *每页条数
     */
    public int size;
    /**
    *业务域id
     */
    public int businessId;
}
