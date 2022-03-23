package com.fisk.task.service.nifi.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.dto.task.AppNifiSettingPO;
import com.fisk.task.mapper.AppNifiSettingMapper;
import com.fisk.task.service.nifi.IAppNifiSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AppNifiSettingServiceImpl extends ServiceImpl<AppNifiSettingMapper, AppNifiSettingPO> implements IAppNifiSettingService {

}
