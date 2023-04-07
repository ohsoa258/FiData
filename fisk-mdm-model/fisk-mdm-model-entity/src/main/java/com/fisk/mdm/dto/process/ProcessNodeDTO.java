package com.fisk.mdm.dto.process;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程节点
 */
@Data
@NoArgsConstructor
public class ProcessNodeDTO {
    /**
     * 节点名称
     */
    private String name;
    /**
     * 节点下标
     */
    private Integer levels;
    /**
     * 设置类型
     */
    private Integer settype;

    private List<ProcessPersonDTO> personList;
}
