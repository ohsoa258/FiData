package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.entity.StandardsBeCitedPO;
import org.springframework.web.bind.annotation.RequestParam;


/**
 *
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-11-20 13:56:24
 */
public interface StandardsBeCitedService extends IService<StandardsBeCitedPO> {

    ResultEnum checkStandardBeCited(Integer standardsId, Integer dbId, Integer tableId, Integer fieldId);
}

