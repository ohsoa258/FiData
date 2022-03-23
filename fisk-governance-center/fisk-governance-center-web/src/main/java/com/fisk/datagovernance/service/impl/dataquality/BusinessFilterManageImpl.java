package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗
 * @date 2022/3/23 12:56
 */
@Service
public class BusinessFilterManageImpl extends ServiceImpl<BusinessFilterMapper, BusinessFilterPO> implements IBusinessFilterManageService {

}
