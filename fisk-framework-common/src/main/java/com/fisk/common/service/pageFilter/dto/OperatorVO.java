package com.fisk.common.service.pageFilter.dto;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class OperatorVO {

    public String label;

    public String value;

    /**
     * 操作符
     */
    public String operators;

    /**
     * 适用类型
     */
    public List<String> useType;

}
