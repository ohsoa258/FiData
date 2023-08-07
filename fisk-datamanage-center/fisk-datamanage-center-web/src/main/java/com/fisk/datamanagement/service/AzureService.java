package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.azure.QueryData;

import java.util.List;
import java.util.Map;

/**
 * @Author: wangjian
 * @Date: 2023-08-07
 * @Description:
 */
public interface AzureService {
    List<Map<String,Object>> getData(QueryData queryData);
}
