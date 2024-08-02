package com.fisk.datamanagement.dto.metadataentity;

import lombok.Data;

@Data
public class EntitiesClassificationDTO {

    private Integer id;

    /**
     * 元数据名
     */
    private String name;

    /**
     * 元数据显示名称
     */
    private String dname;

    /**
     * 元数据关联的大分类名称
     */
    private String cname;

    /**
     * 分类父级名称
     */
    private Integer pid;


}
