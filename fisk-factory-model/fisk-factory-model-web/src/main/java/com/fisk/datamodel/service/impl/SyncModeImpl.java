package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamodel.entity.SyncModePO;
import com.fisk.datamodel.mapper.SyncModeMapper;
import com.fisk.datamodel.service.ISyncMode;
import org.springframework.stereotype.Service;

/**
 * @author JianWenYang
 */
@Service
public class SyncModeImpl
        extends ServiceImpl<SyncModeMapper,SyncModePO>
        implements ISyncMode
{
}
