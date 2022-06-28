package com.fisk.datafactory.dto.tasknifi;

import lombok.Data;

import java.util.List;

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
    public List<NifiPortsHierarchyDTO> nifiPortsHierarchyDtos;
}
