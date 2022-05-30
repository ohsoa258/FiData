package com.fisk.mdm.vo.masterdata;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class OperatorVO {

    private String label;

    private String value;

    /**
     * 操作符
     */
    private String operators;

    /**
     * 适用类型
     */
    private List<String> useType;

}
