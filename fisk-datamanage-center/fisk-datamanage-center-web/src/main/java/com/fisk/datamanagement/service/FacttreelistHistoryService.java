package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.entity.FacttreelistHistoryPO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-03-01 14:37:30
 */
public interface FacttreelistHistoryService extends IService<FacttreelistHistoryPO> {

    List<FacttreelistHistoryPO> selectHistoryId(String historyId);
}

