package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiParamPO;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiParamMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterApiParamManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/10/8 17:05
 */
@Service
public class BusinessFilterApiParamManageImpl extends ServiceImpl<BusinessFilterApiParamMapper, BusinessFilterApiParamPO> implements IBusinessFilterApiParamManageService {
}
