package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.DataCheckLogsPO;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataCheckLogsManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验日志实现类
 * @date 2022/3/23 12:56
 */
@Service
@Slf4j
public class DataCheckLogsManageImpl extends ServiceImpl<DataCheckLogsMapper, DataCheckLogsPO> implements IDataCheckLogsManageService {

    @Async
    @Override
    public void saveLog(DataCheckLogsPO po) {
        baseMapper.insert(po);
    }
}