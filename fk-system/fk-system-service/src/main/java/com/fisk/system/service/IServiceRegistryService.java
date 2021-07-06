package com.fisk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.system.dto.ServiceRegistryDTO;
import com.fisk.system.entity.ServiceRegistryPO;

import java.util.List;

/**
 * @author JianwenYang
 */
public interface IServiceRegistryService extends IService<ServiceRegistryPO> {

    /**
     * 获取服务注册树形结构
     *
     * @return 返回值
     */
    ResultEntity<List<ServiceRegistryDTO>> listServiceRegistry();

    /**
     * 添加服务注册
     *
     * @return 返回值
     */
    ResultEnum addServiceRegistry(ServiceRegistryDTO dto);

    /**
     * 删除服务注册
     * @param id 服务id
     * @return 返回值
     */
    ResultEnum delServiceRegistry(int id);

    /**
     * 获取服务注册树形结构
     * @param id 服务id
     *
     * @return 返回值
     */
    ResultEntity<ServiceRegistryDTO> getDataDetail(int id);
}
