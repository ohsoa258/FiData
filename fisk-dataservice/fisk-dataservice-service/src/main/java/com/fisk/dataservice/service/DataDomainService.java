package com.fisk.dataservice.service;

import com.fisk.dataservice.dto.DataDoFieldDTO;

import java.util.List;
import java.util.Map;

/**
 * @author WangYan
 * @date 2021/8/23 16:36
 */
public interface DataDomainService {

    Object query(List<DataDoFieldDTO> apiConfigureFieldList, Integer currentPage, Integer pageSize);
}
