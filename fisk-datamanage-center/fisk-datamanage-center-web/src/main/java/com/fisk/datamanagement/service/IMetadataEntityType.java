package com.fisk.datamanagement.service;

/**
 * @author JianWenYang
 */
public interface IMetadataEntityType {

    /**
     * 根据类型名称获取id
     *
     * @param type
     * @return
     */
    Integer getTypeId(String type);

}
