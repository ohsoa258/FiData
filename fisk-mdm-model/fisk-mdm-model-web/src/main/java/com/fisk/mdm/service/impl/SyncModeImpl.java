package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.mdm.entity.SyncModePO;
import com.fisk.mdm.mapper.SyncModeMapper;
import com.fisk.mdm.service.ISyncMode;
import org.springframework.stereotype.Service;

/**
 * @author wangjian
 */
@Service
public class SyncModeImpl
        extends ServiceImpl<SyncModeMapper, SyncModePO>
        implements ISyncMode
{
}
