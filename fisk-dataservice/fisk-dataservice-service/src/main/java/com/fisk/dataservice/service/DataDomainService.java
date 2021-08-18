package com.fisk.dataservice.service;

/**
 * @author WangYan
 * @date 2021/8/12 11:32
 */
public interface DataDomainService {

    /**
     * 获取数据域
     * @param businessName 一级的业务域名称
     * @return
     */
    Object getDataDomain(String businessName);
}
