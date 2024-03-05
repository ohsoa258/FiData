package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsHistoryPO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-03-01 14:32:54
 */
public interface BusinessExtendedfieldsHistoryService extends IService<BusinessExtendedfieldsHistoryPO> {

    List<BusinessExtendedfieldsHistoryPO> selectHistoryId( String historyId);
}

