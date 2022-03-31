package com.fisk.system.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.dto.ServiceRegistryDTO;
import com.fisk.system.dto.ServiceRegistryDataDTO;

import java.util.List;

/**
 * @author JianwenYang
 */
public interface IServiceRegistryService {

    /**
     * 获取服务注册树形结构
     *
     * @return 返回值
     */
    List<ServiceRegistryDTO> listServiceRegistry();

    /**
     * 添加服务注册
     *
     * @param dto dto
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
    ServiceRegistryDTO getDataDetail(int id);

    /**
     * 修改服务注册树形结构
     * @param dto dto
     * @return 返回值
     */
    ResultEnum updateServiceRegistry(ServiceRegistryDTO dto);

    /**
     * 获取菜单中文名称列表
     * @return
     */
    List<ServiceRegistryDataDTO> getServiceRegistryList();

}
