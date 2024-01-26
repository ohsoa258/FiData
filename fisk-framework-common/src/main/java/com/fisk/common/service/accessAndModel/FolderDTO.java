package com.fisk.common.service.accessAndModel;

import lombok.Data;

@Data
public class FolderDTO {

    /**
     * 业务域id
     */
    private Integer id;

    /**
     * 业务域名称
     */
    private String name;

    /**
     * 0维度  1事实
     */
    private Integer type;

}
