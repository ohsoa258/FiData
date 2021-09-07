package com.fisk.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.entity.OlapDimensionPO;
import com.fisk.task.entity.OlapKpiPO;
import com.fisk.task.mapper.OlapDimensionMapper;
import com.fisk.task.mapper.OlapKpiMapper;
import com.fisk.task.service.IOlapKpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 建模度量
 * @author JinXingWang
 */
@Service
@Slf4j
public class OlapKpiImpl extends ServiceImpl<OlapKpiMapper, OlapKpiPO> implements IOlapKpi {
    @Resource
    OlapKpiMapper mapper;
    @Override
    public boolean deleteByBusinessAreaId(int businessAreaId) {
        mapper.deleteByBusinessId(businessAreaId);
        return true;
    }

    @Override
    public boolean batchAdd(List<OlapKpiPO> pos) {
       return  saveBatch(pos);
    }
}
