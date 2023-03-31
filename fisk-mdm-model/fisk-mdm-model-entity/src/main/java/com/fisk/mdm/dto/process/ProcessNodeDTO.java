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
    public String name;
    /**
     * 节点下标
     */
    public int levels;
    /**
     * 设置类型
     */
    public int settype;

    public List<ProcessPersonDTO> personList;
}
