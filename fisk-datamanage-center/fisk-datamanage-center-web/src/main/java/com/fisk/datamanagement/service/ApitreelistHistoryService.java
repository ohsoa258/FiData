package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.entity.ApitreelistHistoryPO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-09-25 16:13:25
 */
public interface ApitreelistHistoryService extends IService<ApitreelistHistoryPO> {

    List<ApitreelistHistoryPO> selectHistoryId(String historyId);
}

