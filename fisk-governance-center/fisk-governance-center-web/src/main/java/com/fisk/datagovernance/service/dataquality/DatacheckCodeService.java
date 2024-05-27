package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.DatacheckCodePO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-05-13 14:04:19
 */
public interface DatacheckCodeService extends IService<DatacheckCodePO> {

    List<String> getCheckCodeList();
}

