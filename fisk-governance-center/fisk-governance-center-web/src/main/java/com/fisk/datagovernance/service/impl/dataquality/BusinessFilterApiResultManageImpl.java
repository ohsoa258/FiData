package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiResultPO;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiResultMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterApiResultManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/10/8 17:06
 */
@Service
public class BusinessFilterApiResultManageImpl  extends ServiceImpl<BusinessFilterApiResultMapper, BusinessFilterApiResultPO> implements IBusinessFilterApiResultManageService {
}
