package com.fisk.datafactory.dto.tasknifi;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 * @version 2.6
 * @description 管道dag结构
 * @date 2022/6/27 16:38
 */
@Data
public class PipeDagDTO {

    /**
     * task无序集合
     */
    public List<TaskHierarchyDTO> taskHierarchyDtos;
    /**
     * pipelTraceId
     */
    public String pipelTraceId;

    /**
     * 本节点特殊参数
     */
    public Map<String, String> specialParaMap;

    /**
     * 管道状态参数集合
     */
    public Map<String, String> pipelParaMap;

}
