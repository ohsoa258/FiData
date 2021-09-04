package com.fisk.datamodel.service;

/**
 * @author WangYan
 * @date 2021/8/12 11:32
 */
public interface DataDomainService {

    /**
     * 获取数据域
     * @param
     * @return
     */
    Object getDataDomain();

    /**
     * 获取维度
     * @param
     * @return
     */
    Object getDimension();

    /**
     * 获取业务板块
     * @param
     * @return
     */
    Object getBusiness();
}
