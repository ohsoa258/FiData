package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.entity.BusinessTargetinfoHistoryPO;
import org.apache.ibatis.annotations.Param;

/**
 *
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-03-01 14:35:45
 */
public interface BusinessTargetinfoHistoryService extends IService<BusinessTargetinfoHistoryPO> {

    BusinessTargetinfoHistoryPO selectClassification(@Param("historyId") String historyId);
}

