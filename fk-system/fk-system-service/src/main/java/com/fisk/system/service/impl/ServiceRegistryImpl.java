package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.system.dto.ServiceRegistryDTO;
import com.fisk.system.entity.ServiceRegistryPO;
import com.fisk.system.map.ServiceRegistryMap;
import com.fisk.system.mapper.ServiceRegistryMapper;
import com.fisk.system.service.IServiceRegistryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * @author JianWenYang
 */
@Service
public class ServiceRegistryImpl implements IServiceRegistryService {

    @Resource
    ServiceRegistryMapper mapper;
    @Resource
    UserHelper userHelper;

    /**
     * 获取服务注册树形结构
     *
     * @return 返回值
     */
    @Override
    public List<ServiceRegistryDTO> listServiceRegistry() {
        try {
            // 查询数据
            QueryWrapper<ServiceRegistryPO> queryWrapper = new QueryWrapper<>();
            List<ServiceRegistryPO> list = mapper.selectList(queryWrapper);
            /*查询所有父节点*/
            String code="1";
            List<ServiceRegistryPO> list_Parent=list.stream().filter(e->code.equals(e.getParentServeCode()))
                    .collect(Collectors.toList());

            List<ServiceRegistryDTO> dtos = new ArrayList<>();

            for (ServiceRegistryPO po : list_Parent) {

                ServiceRegistryDTO dto=ServiceRegistryMap.INSTANCES.poToDto(po);
                List<ServiceRegistryDTO> data=new ArrayList<>();
                List<ServiceRegistryPO> list_Child=list.stream().filter(e->po.getServeCode().equals(e.getParentServeCode())).collect(Collectors.toList());
                /*查询所有子节点*/
                for (ServiceRegistryPO item : list_Child)
                {
                    ServiceRegistryDTO obj=ServiceRegistryMap.INSTANCES.poToDto(item);
                    data.add(obj);
                }
                dto.setDtos(data);
                dtos.add(dto);
            }
            return dtos;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.ERROR);
        }
    }

    /**
     * 添加服务注册
     *
     * @return 返回值
     */
    @Override
    public ResultEnum addServiceRegistry(ServiceRegistryDTO dto)
    {
        try {

            ServiceRegistryPO po=ServiceRegistryMap.INSTANCES.dtoToPo(dto);
            /*获取登录信息*/
            UserInfo userInfo = userHelper.getLoginUserInfo();
            // 数据保存需求更改: 添加应用的时候，相同的应用名称不可以再次添加
            QueryWrapper<ServiceRegistryPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(ServiceRegistryPO::getServeCnName, dto.serveCnName);

            ServiceRegistryPO data = mapper.selectOne(queryWrapper);
            if (data != null) {
                return ResultEnum.NAME_EXISTS;
            }
            po.createUser=userInfo.id.toString();
            po.serveCode=UUID.randomUUID().toString();
            // 保存
            return mapper.insert(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.ERROR);
        }
    }

    /**
     * 删除服务注册
     *
     * @return 返回值
     */
    @Override
    public  ResultEnum delServiceRegistry(int id){
        try {
            ServiceRegistryPO model = mapper.selectById(id);
            if (model == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            UserInfo userInfo = userHelper.getLoginUserInfo();
            model.updateUser=userInfo.id.toString();
            return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.ERROR);
        }
    }

    /**
     * 根据id查询数据,用于数据回显
     *
     * @param id 请求参数
     * @return 返回值
     */
    @Override
    public ServiceRegistryDTO getDataDetail(int id) {
        try {
            ServiceRegistryDTO po = ServiceRegistryMap.INSTANCES.poToDto(mapper.selectById(id));
            if (po == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            return po;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.ERROR);
        }
    }


    @Override
    public ResultEnum updateServiceRegistry(ServiceRegistryDTO dto) {
        try {
            /*判断是否存在*/
            ServiceRegistryPO model = mapper.selectById(dto.id);
            if (model == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            /*判断名称是否重复*/
            QueryWrapper<ServiceRegistryPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(ServiceRegistryPO::getServeCnName, dto.serveEnName);
            ServiceRegistryPO data = mapper.selectOne(queryWrapper);
            if (data != null && data.id != dto.id) {
                return ResultEnum.NAME_EXISTS;
            }
            ServiceRegistryPO po=ServiceRegistryMap.INSTANCES.dtoToPo(dto);
            UserInfo userInfo = userHelper.getLoginUserInfo();
            po.updateUser=userInfo.id.toString();
            return  mapper.updateById(po)>0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        catch (Exception e){
            throw new FkException(ResultEnum.ERROR);
        }
    }



}
