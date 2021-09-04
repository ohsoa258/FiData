package com.fisk.task.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.entity.OlapDimensionPO;
import com.fisk.task.mapper.OlapDimensionMapper;
import com.fisk.task.service.IOlapDimension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 建模维度
 * @author JinXingWang
 */
@Service
@Slf4j
public class OlapDimensionmpl extends ServiceImpl<OlapDimensionMapper,OlapDimensionPO> implements IOlapDimension {
    @Resource
    OlapDimensionMapper mapper;

    @Override
    public boolean deleteByBusinessAreaId(int businessAreaId) {
         mapper.deleteByBusinessId(businessAreaId);
         return true;
    }

    @Override
    public boolean batchAdd(List<OlapDimensionPO> pos) {
        return  saveBatch(pos);
    }
}
