package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiConfigPO;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterApiManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/10/8 17:05
 */
@Service
public class BusinessFilterApiManageImpl  extends ServiceImpl<BusinessFilterApiMapper, BusinessFilterApiConfigPO> implements IBusinessFilterApiManageService {

}
