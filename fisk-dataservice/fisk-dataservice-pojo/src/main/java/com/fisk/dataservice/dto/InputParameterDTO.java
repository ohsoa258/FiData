package com.fisk.dataservice.dto;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/9/4 23:11
 */
@Data
public class InputParameterDTO {
    /**
     * 维度字段id集合
     */
    public List<Integer> ids;
    /**
     * 指标id集合
     */
    public List<Integer> indicatorsIds;
}
