package com.fisk.datafactory.dto.tasknifi;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "task无序集合")
    public List<TaskHierarchyDTO> taskHierarchyDtos;
    /**
     * pipelTraceId
     */
    @ApiModelProperty(value = "pipelTraceId")
    public String pipelTraceId;

    /**
     * 本节点特殊参数
     */
    @ApiModelProperty(value = "本节点特殊参数")
    public Map<String, String> specialParaMap;

    /**
     * 管道状态参数集合
     */
    @ApiModelProperty(value = "管道状态参数集合")
    public Map<String, String> pipelParaMap;

}
