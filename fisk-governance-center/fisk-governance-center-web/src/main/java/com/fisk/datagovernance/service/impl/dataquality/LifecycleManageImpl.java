package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.LifecyclePO;
import com.fisk.datagovernance.mapper.dataquality.LifecycleMapper;
import com.fisk.datagovernance.service.dataquality.ILifecycleManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期实现类
 * @date 2022/3/23 12:56
 */
@Service
public class LifecycleManageImpl extends ServiceImpl<LifecycleMapper, LifecyclePO> implements ILifecycleManageService {

}