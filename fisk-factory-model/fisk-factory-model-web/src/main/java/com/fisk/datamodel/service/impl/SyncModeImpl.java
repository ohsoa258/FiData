package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.syncmode.SyncModeDTO;
import com.fisk.datamodel.entity.SyncModePO;
import com.fisk.datamodel.map.SyncModeMap;
import com.fisk.datamodel.mapper.SyncModeMapper;
import com.fisk.datamodel.mapper.TableBusinessMapper;
import com.fisk.datamodel.service.ISyncMode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class SyncModeImpl
        extends ServiceImpl<SyncModeMapper,SyncModePO>
        implements ISyncMode
{
}
