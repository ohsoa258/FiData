package com.fisk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.system.dto.SystemVersionDTO;
import com.fisk.system.entity.SystemVersionPO;

/**
 * @author 56263
 * @description 针对表【tb_system_version】的数据库操作Service
 * @createDate 2023-04-07 14:05:19
 */
public interface SystemVersionService extends IService<SystemVersionPO> {

    /**
     * 获得当前平台最新的版本信息
     *
     * @return
     */
    SystemVersionDTO get();

}
