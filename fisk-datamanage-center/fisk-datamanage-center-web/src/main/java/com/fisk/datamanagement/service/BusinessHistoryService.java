package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.entity.BusinessHistoryPO;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-03-01 14:39:17
 */
public interface BusinessHistoryService extends IService<BusinessHistoryPO> {

    List<String> getHistoryId(Integer id);
}

