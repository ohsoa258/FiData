package com.fisk.common.service.accessAndModel;

import lombok.Data;

import java.util.List;

@Data
public class ModelAreaAndFolderDTO {

    /**
     * 业务域id
     */
    private Integer id;

    /**
     * 业务域名称
     */
    private String name;

    /**
     * 维度文件夹/事实文件夹
     */
    private List<FolderDTO> children;

}
