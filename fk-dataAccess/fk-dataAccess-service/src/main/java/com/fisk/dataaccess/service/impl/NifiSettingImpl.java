package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.entity.NifiSettingPO;
import com.fisk.dataaccess.mapper.NifiSettingMapper;
import com.fisk.dataaccess.service.INifiSetting;
import org.springframework.stereotype.Service;

/**
 * @author Lock
 */
@Service
public class NifiSettingImpl extends ServiceImpl<NifiSettingMapper, NifiSettingPO> implements INifiSetting {
}
