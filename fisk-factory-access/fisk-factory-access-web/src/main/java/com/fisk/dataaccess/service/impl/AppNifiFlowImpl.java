package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.entity.AppNifiFlowPO;
import com.fisk.dataaccess.mapper.AppNifiFlowMapper;
import com.fisk.dataaccess.service.IAppNifiFlow;
import org.springframework.stereotype.Service;

/**
 * @author Lock
 */
@Service
public class AppNifiFlowImpl extends ServiceImpl<AppNifiFlowMapper, AppNifiFlowPO> implements IAppNifiFlow {
}
