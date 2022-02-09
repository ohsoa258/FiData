package com.fisk.task.service.nifi.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.dto.task.TableNifiSettingPO;
import com.fisk.task.mapper.TableNifiSettingMapper;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TableNifiSettingServiceImpl extends ServiceImpl<TableNifiSettingMapper, TableNifiSettingPO> implements ITableNifiSettingService {
}
