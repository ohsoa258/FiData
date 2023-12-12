package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.entity.KsfPlantPO;
import com.fisk.dataservice.mapper.KsfPlantMapper;
import com.fisk.dataservice.service.KsfPlantService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ksfPlantService")
public class KsfPlantServiceImpl extends ServiceImpl<KsfPlantMapper, KsfPlantPO> implements KsfPlantService {


    @Override
    public List<KsfPlantPO> getPlant() {
        List<KsfPlantPO> list = this.list();
        return list;
    }
}
