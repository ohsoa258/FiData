package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessAssociationDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDropDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.BusinessProcessPO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.BusinessProcessMap;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.BusinessProcessMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IBusinessProcess;
import com.fisk.task.client.PublishTaskClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class BusinessProcessImpl implements IBusinessProcess {

    @Resource
    BusinessProcessMapper mapper;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    UserHelper userHelper;
    @Resource
    FactMapper factMapper;
    @Resource
    FactAttributeImpl factAttribute;
    @Resource
    BusinessAreaMapper businessAreaMapper;
    @Resource
    BusinessProcessMapper businessProcessMapper;

    @Override
    public IPage<BusinessProcessDTO> getBusinessProcessList(QueryDTO dto)
    {
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        if (dto.id !=0)
        {
            queryWrapper.lambda().eq(BusinessProcessPO::getBusinessId,dto.id);
        }
        Page<BusinessProcessPO> data=new Page<>(dto.getPage(),dto.getSize());
        return BusinessProcessMap.INSTANCES.pagePoToDto(mapper.selectPage(data,queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public ResultEnum addBusinessProcess(BusinessProcessDTO dto)
    {
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessProcessPO::getBusinessProcessCnName,dto.businessProcessCnName);
        BusinessProcessPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        dto.isPublish= PublicStatusEnum.UN_PUBLIC.getValue();
        BusinessProcessPO model=BusinessProcessMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public BusinessProcessAssociationDTO getBusinessProcessDetail(int id)
    {
        return mapper.getBusinessProcessDetail(id);
    }

    @Override
    public ResultEnum updateBusinessProcess(BusinessProcessDTO dto)
    {
        BusinessProcessPO model=mapper.selectById(dto.id);
        if (model==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessProcessPO::getBusinessProcessCnName,dto.businessProcessCnName);
        BusinessProcessPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        model=BusinessProcessMap.INSTANCES.dtoToPo(dto);
        return mapper.updateById(model)>0?ResultEnum.SUCCESS: ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteBusinessProcess(int id)
    {
        BusinessProcessPO model=mapper.selectById(id);
        if (model==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<BusinessProcessDropDTO> getBusinessProcessDropList()
    {
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        return BusinessProcessMap.INSTANCES.poToDropPo(mapper.selectList(queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public ResultEnum businessProcessPublish(int id)
    {
        try{
            BusinessProcessPO po=businessProcessMapper.selectById(id);
            if (po==null)
            {
                return ResultEnum.DATA_NOTEXISTS;
            }
            BusinessAreaPO businessAreaPO=businessAreaMapper.selectById(po.businessId);
            if (businessAreaPO==null)
            {
                return ResultEnum.DATA_NOTEXISTS;
            }
            DimensionAttributeAddDTO pushDto=new DimensionAttributeAddDTO();
            pushDto.dimensionId=id;
            pushDto.dimensionName=po.businessProcessEnName;
            pushDto.businessAreaName=businessAreaPO.getBusinessName();
            pushDto.createType= CreateTypeEnum.CREATE_FACT.getValue();
            pushDto.userId=userHelper.getLoginUserInfo().id;
            //发送消息
            publishTaskClient.publishBuildAtlasDorisTableTask(pushDto);
        }
        catch (Exception ex){
            log.error(ex.getMessage());
            return ResultEnum.PUBLISH_FAILURE;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<ModelMetaDataDTO> businessProcessPush(int businessProcessId)
    {
        List<ModelMetaDataDTO> list=new ArrayList<>();
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("id").lambda().eq(FactPO::getBusinessProcessId,businessProcessId);
        List<Object> data=factMapper.selectObjs(queryWrapper);
        List<Integer> ids = (List)data;
        //循环获取事实表
        for (Integer id:ids)
        {
            //获取事实表先关字段
            ModelMetaDataDTO metaDataDTO= factAttribute.getFactMetaData(id);
            if (metaDataDTO==null)
            {
                break;
            }
            list.add(metaDataDTO);
        }
        return list;
    }

    @Override
    public void updatePublishStatus(int id,int isSuccess)
    {
        BusinessProcessPO po=mapper.selectById(id);
        if (po==null)
        {
            log.info(id+":数据不存在");
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        po.isPublish=isSuccess;
        int flat=mapper.updateById(po);
        String msg=flat>0?"发布成功":"发布失败";
        log.info(po.businessProcessCnName+":"+msg);
    }

}
