package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.dataservice.entity.KsfPlantPO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-12-11 14:22:15
 */
public interface KsfPlantService extends IService<KsfPlantPO> {

    List<KsfPlantPO> getPlant();
}

