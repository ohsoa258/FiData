package com.fisk.task.service.nifi.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.mapper.MdmNifiSettingMapper;
import com.fisk.task.po.mdm.MdmNifiSettingPO;
import com.fisk.task.service.nifi.IMdmNifiSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wangjian
 */
@Service
@Slf4j
public class MdmNifiSettingServiceImpl extends ServiceImpl<MdmNifiSettingMapper, MdmNifiSettingPO> implements IMdmNifiSettingService {

}
