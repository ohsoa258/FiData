package com.fisk.task.service.nifi.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.mapper.MdmTableNifiSettingMapper;
import com.fisk.task.po.mdm.MdmTableNifiSettingPO;
import com.fisk.task.service.nifi.IMdmTableNifiSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wangjian
 */
@Service
@Slf4j
public class MdmTableNifiSettingServiceImpl extends ServiceImpl<MdmTableNifiSettingMapper, MdmTableNifiSettingPO> implements IMdmTableNifiSettingService {


    @Override
    public MdmTableNifiSettingPO getByTableId(long tableId, long tableType) {
        MdmTableNifiSettingPO tableNifiSettingPO = baseMapper.getByTableId(tableId, tableType);
        return tableNifiSettingPO;
    }
}
