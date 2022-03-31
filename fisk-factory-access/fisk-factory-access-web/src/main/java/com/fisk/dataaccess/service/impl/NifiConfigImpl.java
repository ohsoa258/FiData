package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.entity.NifiConfigPO;
import com.fisk.dataaccess.mapper.NifiConfigMapper;
import com.fisk.dataaccess.service.INifiConfig;
import org.springframework.stereotype.Service;

/**
 * @author Lock
 */
@Service
public class NifiConfigImpl extends ServiceImpl<NifiConfigMapper, NifiConfigPO> implements INifiConfig {
}
