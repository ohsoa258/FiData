package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.service.INifiCustomWorkflowDetail;
import com.fisk.datafactory.mapper.NifiCustomWorkflowDetailMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wangyan and Lock
 */
@Service
@Slf4j
public class NifiCustomWorkflowDetailImpl extends ServiceImpl<NifiCustomWorkflowDetailMapper, NifiCustomWorkflowDetailPO> implements INifiCustomWorkflowDetail {
}
