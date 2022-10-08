package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiParmPO;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiParmMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterApiParmManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/10/8 17:05
 */
@Service
public class BusinessFilterApiParmManageImpl  extends ServiceImpl<BusinessFilterApiParmMapper, BusinessFilterApiParmPO> implements IBusinessFilterApiParmManageService {
}
