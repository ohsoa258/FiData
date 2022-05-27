package com.fisk.dataaccess.dto.api.doc;

import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API文档目录
 * @date 2022/2/3 20:22
 */
@Data
public class ApiCatalogueDTO {
    /**
     * 目录等级
     * 一级
     * 二级
     * 三级
     */
    public int grade;

    /**
     * 目录序号
     */
    public String catalogueIndex;

    /**
     * 目录名称
     */
    public  String catalogueName;
}
