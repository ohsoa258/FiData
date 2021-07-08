package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.system.dto.ServiceRegistryDTO;
import com.fisk.system.entity.ServiceRegistryPO;
import com.fisk.system.mapper.ServiceRegistryMapper;
import com.fisk.system.service.IServiceRegistryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * @author JianWenYang
 */
@Service
public class ServiceRegistryImpl extends ServiceImpl<ServiceRegistryMapper, ServiceRegistryPO> implements IServiceRegistryService {

    /**
     * 获取服务注册树形结构
     *
     * @return 返回值
     */
    @Override
    public ResultEntity<List<ServiceRegistryDTO>> listServiceRegistry() {
        ResultEntity<List<ServiceRegistryDTO>> result=new ResultEntity<List<ServiceRegistryDTO>>();
        try {
            // 查询数据
            List<ServiceRegistryPO> list = this.query()
                    .eq("del_flag",1).list();
            String code="1";
            List<ServiceRegistryPO> list_Parent=list.stream().filter(e->code.equals(e.getParentServeCode()))
                    .collect(Collectors.toList());

            List<ServiceRegistryDTO> dtos = new ArrayList<>();

            for (ServiceRegistryPO po : list_Parent) {
                ServiceRegistryDTO dto = new ServiceRegistryDTO();
                dto.setId(po.getId());
                dto.setIcon(po.getIcon());
                dto.setServeCode(po.getServeCode());
                dto.setServeCnName(po.getServeCnName());
                dto.setServeEnName(po.getServeEnName());
                dto.setParentServeCode(po.getParentServeCode());
                dto.setSequenceNo(po.getSequenceNo());

                List<ServiceRegistryDTO> data=new ArrayList<>();
                List<ServiceRegistryPO> list_Child=list.stream().filter(e->po.getServeCode().equals(e.getParentServeCode())).collect(Collectors.toList());
                for (ServiceRegistryPO item : list_Child)
                {
                    ServiceRegistryDTO obj=new ServiceRegistryDTO();
                    obj.setId(item.getId());
                    obj.setServeCode(item.getServeCode());
                    obj.setParentServeCode(item.getParentServeCode());
                    obj.setServeEnName(item.getServeEnName());
                    obj.setServeCnName(item.getServeCnName());
                    obj.setIcon(item.getIcon());
                    obj.setSequenceNo(item.getSequenceNo());
                    obj.setServeUrl(item.getServeUrl());
                    data.add(obj);
                }
                dto.setDtos(data);
                dtos.add(dto);
            }
            result.code=ResultEnum.SUCCESS.getCode();
            result.data=dtos;
        }
        catch (Exception e)
        {
            result.code=ResultEnum.ERROR.getCode();
            result.data=null;
            result.msg="查询失败！";
        }
        return result;
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
            ServiceRegistryPO po = dto.toEntity(ServiceRegistryPO.class);

            // 数据保存需求更改: 添加应用的时候，相同的应用名称不可以再次添加
            List<String> nameList = baseMapper.getServiceName();
            String appName = po.getServeCnName();
            boolean contains = nameList.contains(appName);
            if (contains) {
                return ResultEnum.DATA_EXISTS;
            }

            // 保存tb_app_registration数据
            Date date1 = new Date(System.currentTimeMillis());
            po.setCreateTime(date1);
            po.setUpdateTime(date1);
            po.setDelFlag(1);

            String appId = UUID.randomUUID().toString();
            po.setServeCode(appId);

            // 保存
            boolean save = this.save(po);
            if (!save) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }

            return save ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;

        }
        catch (Exception e)
        {
            return  ResultEnum.ERROR;
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
            ServiceRegistryPO model = this.getById(id);
            if (model == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            model.setDelFlag(0);
            boolean updateReg = this.updateById(model);
            if (!updateReg) {
                throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
            }
            return updateReg ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        catch (Exception e)
        {
            return  ResultEnum.ERROR;
        }
    }

    /**
     * 根据id查询数据,用于数据回显
     *
     * @param id 请求参数
     * @return 返回值
     */
    @Override
    public ResultEntity<ServiceRegistryDTO> getDataDetail(int id) {
        ResultEntity<ServiceRegistryDTO> result=new ResultEntity<ServiceRegistryDTO>();
        try {
            ServiceRegistryPO po = this.query()
                    .eq("id", id)
                    .eq("del_flag", 1)
                    .one();
            if (po==null)
            {
                result.msg="数据不存在！";
                result.code=ResultEnum.DATA_NOTEXISTS.getCode();
                return result;
            }
            ServiceRegistryDTO dto = new ServiceRegistryDTO(po);
            result.data=dto;
            result.code=ResultEnum.SUCCESS.getCode();
        }
        catch (Exception e)
        {
            result.data=null;
            result.msg="保存失败！";
            result.code=ResultEnum.ERROR.getCode();
        }
        return result;
    }


    @Override
    public ResultEnum updateServiceRegistry(ServiceRegistryDTO dto) {
        try {
            int id = dto.getId();
            /*判断是否为空*/
            ServiceRegistryPO model = this.getById(id);
            if (model == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            /*判断名称是否重复*/
            ServiceRegistryPO bpo = this.query()
                    .eq("serve_cn_name", dto.getServeCnName())
                    .eq("del_flag", 1)
                    .one();
            if (bpo !=null && model.getId() != bpo.getId())
            {
                return ResultEnum.NAME_EXISTS;
            }

            /*修改数据*/
            model.setSequenceNo(dto.getSequenceNo());
            Date date = new Date(System.currentTimeMillis());
            model.setUpdateTime(date);
            model.setIcon(dto.getIcon());
            model.setServeCnName(dto.getServeCnName());
            model.setServeEnName(dto.getServeEnName());
            model.setServeUrl(dto.getServeUrl());

            return this.updateById(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        catch (Exception e){
            return  ResultEnum.ERROR;
        }
    }
}
