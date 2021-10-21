package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamodel.dto.businessprocess.*;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.fact.FactDataDTO;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.AtomicIndicatorsMap;
import com.fisk.datamodel.map.BusinessProcessMap;
import com.fisk.datamodel.map.FactAttributeMap;
import com.fisk.datamodel.map.FactMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IBusinessProcess;
import com.fisk.task.client.PublishTaskClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class BusinessProcessImpl
        extends ServiceImpl<BusinessProcessMapper,BusinessProcessPO>
        implements IBusinessProcess {

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
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    IndicatorsMapper indicatorsMapper;

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
    public ResultEnum deleteBusinessProcess(List<Integer> ids)
    {
        return this.removeByIds(ids)?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<BusinessProcessDropDTO> getBusinessProcessDropList()
    {
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        return BusinessProcessMap.INSTANCES.poToDropPo(mapper.selectList(queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public ResultEnum businessProcessPublish(BusinessProcessPublishDTO dto)
    {
        try{
            BusinessAreaPO businessAreaPO=businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPO==null)
            {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.in("business_process_id",dto.businessProcessIds);
            List<FactPO> factPOList=factMapper.selectList(queryWrapper);
            if (factPOList==null || factPOList.size()==0)
            {
                throw new FkException(ResultEnum.PUBLISH_FAILURE,"事实表为空");
            }
            List<DimensionAttributeAddDTO> list=new ArrayList<>();
            for (FactPO item:factPOList)
            {
                DimensionAttributeAddDTO pushDto=new DimensionAttributeAddDTO();
                pushDto.dimensionId=Integer.parseInt(String.valueOf(item.id));;
                pushDto.dimensionName=item.factTableEnName;
                pushDto.businessAreaName=businessAreaPO.getBusinessName();
                pushDto.createType= CreateTypeEnum.CREATE_FACT.getValue();
                pushDto.userId=userHelper.getLoginUserInfo().id;
                list.add(pushDto);
            }
            //发送消息
            publishTaskClient.publishBuildAtlasDorisTableTask(list);
        }
        catch (Exception ex){
            log.error(ex.getMessage());
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
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

    @Override
    public BusinessAreaContentDTO getBusinessId(int factId)
    {
        BusinessAreaContentDTO dto=new BusinessAreaContentDTO();
        //查询事实表信息
        FactPO po=factMapper.selectById(factId);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "事实表不存在");
        }
        dto.factTableName=po.factTableEnName;
        //查询业务过程id
        BusinessProcessPO businessProcessPO=businessProcessMapper.selectById(po.businessProcessId);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "业务过程不存在");
        }
        dto.businessAreaId=businessProcessPO.businessId;
        return  dto;
    }

    @Override
    public List<BusinessProcessListDTO> getBusinessProcessList(int businessAreaId)
    {
        BusinessAreaPO po=businessAreaMapper.selectById(businessAreaId);
        if (po==null)
        {}
        List<BusinessProcessListDTO> dtoList=new ArrayList<>();
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda()
                .eq(BusinessProcessPO::getBusinessId,businessAreaId);
        List<BusinessProcessPO> list=mapper.selectList(queryWrapper);
        if (list==null || list.size()==0)
        {
            return dtoList;
        }
        dtoList=BusinessProcessMap.INSTANCES.poListToDtoList(list);
        //获取业务过程id集合
        List<Integer> businessProcessIds=(List)mapper.selectObjs(queryWrapper.select("id"));
        if (businessProcessIds==null || businessProcessIds.size()==0)
        {
            return dtoList;
        }
        //根据业务过程id集合,获取事实表数据
        QueryWrapper<FactPO> factPOQueryWrapper=new QueryWrapper<>();
        factPOQueryWrapper.in("business_process_id",businessProcessIds);
        List<FactPO> factPOList=factMapper.selectList(factPOQueryWrapper);
        for (BusinessProcessListDTO item:dtoList)
        {
            //获取业务过程下所有事实表
            item.factList= FactMap.INSTANCES.poListToDtoList(factPOList.stream()
                    .filter(e->e.businessProcessId==item.id)
                    .sorted(Comparator.comparing(FactPO::getCreateTime))
                    .collect(Collectors.toList()));
            Collections.reverse(item.factList);
            if (item.factList.size()==0)
            {
                continue;
            }
            //查询业务过程下事实表id集合
            List<Integer> factIds=(List)factMapper.selectObjs(factPOQueryWrapper.select("id"));
            if (factIds==null || factIds.size()==0)
            {
                continue;
            }
            //根据事实表id集合,获取事实字段列表以及指标列表
            QueryWrapper<FactAttributePO> attributePOQueryWrapper=new QueryWrapper<>();
            attributePOQueryWrapper.in("fact_id",factIds);
            List<FactAttributePO> factAttributePOList=factAttributeMapper.selectList(attributePOQueryWrapper);
            //根据事实表id集合,获取指标列表
            QueryWrapper<IndicatorsPO> indicatorsPOQueryWrapper=new QueryWrapper<>();
            indicatorsPOQueryWrapper.in("fact_id",factIds);
            List<IndicatorsPO> indicatorsPOList=indicatorsMapper.selectList(indicatorsPOQueryWrapper);
            //获取每个事实表下字段列表、指标列表
            for (FactDataDTO fact:item.factList)
            {
                List<FactAttributePO> attributePOS=factAttributePOList.stream()
                        .filter(e->e.factId==fact.id).collect(Collectors.toList());
                if (attributePOS!=null && attributePOS.size()>0)
                {
                    fact.attributeList=FactAttributeMap.INSTANCES.poListToDtoList(attributePOS);
                    Collections.reverse(fact.attributeList);
                }
                List<IndicatorsPO> indicatorsPOS=indicatorsPOList.stream()
                        .filter(e->e.factId==fact.id).collect(Collectors.toList());
                if (indicatorsPOS!=null && indicatorsPOS.size()>0)
                {
                    fact.indicatorsList= AtomicIndicatorsMap.INSTANCES.poListToDtoList(indicatorsPOS);
                    Collections.reverse(fact.indicatorsList);
                }
            }

        }
        return dtoList;
    }

}
