package com.fisk.taskfactory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.taskfactory.entity.NifiComponentsPO;
import com.fisk.taskfactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.taskfactory.mapper.NifiComponentsMapper;
import com.fisk.taskfactory.mapper.NifiCustomWorkflowDetailMapper;
import com.fisk.taskfactory.service.INifiComponent;
import com.fisk.taskfactory.service.INifiCustomWorkflowDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wangyan and Lock
 */
@Service
@Slf4j
public class NifiCustomWorkflowDetailImpl extends ServiceImpl<NifiCustomWorkflowDetailMapper, NifiCustomWorkflowDetailPO> implements INifiCustomWorkflowDetail {
}
