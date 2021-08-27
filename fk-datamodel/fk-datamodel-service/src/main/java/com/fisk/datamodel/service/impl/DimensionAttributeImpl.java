package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.enums.DimensionAttributeEnum;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.dto.dimensionattribute.*;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.map.DimensionAttributeMap;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.service.IDimensionAttribute;
import com.fisk.task.client.PublishTaskClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class DimensionAttributeImpl
        extends ServiceImpl<DimensionAttributeMapper, DimensionAttributePO>
        implements IDimensionAttribute {

    @Resource
    DimensionMapper mapper;
    @Resource
    DimensionAttributeMapper attributeMapper;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    UserHelper userHelper;

    @Override
    public List<DimensionMetaDTO> getProjectDimensionTable()
    {
        List<DimensionMetaDTO> list=new ArrayList<>();
        //获取维度表
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        List<DimensionPO> data=mapper.selectList(queryWrapper);
        //获取维度字段表
        QueryWrapper<DimensionAttributePO> queryWrapper2=new QueryWrapper<>();
        List<DimensionAttributePO> list2=attributeMapper.selectList(queryWrapper2);
        for (DimensionPO po:data)
        {
            DimensionMetaDTO model=new DimensionMetaDTO();
            model.tableName=po.dimensionTabName;
            model.associateDimensionId=po.id;
            List<DimensionAttributePO> filter=list2.stream().filter(e->e.getDimensionId()==po.id && e.getAttributeType() ==DimensionAttributeEnum.BUSINESS_KEY.getValue()).collect(Collectors.toList());
            List<DimensionAttributeAssociationDTO> associationList=new ArrayList<>();
            for (DimensionAttributePO attribute:filter)
            {
                DimensionAttributeAssociationDTO dto=new DimensionAttributeAssociationDTO();
                dto.id=attribute.id;
                dto.dimensionFieldEnName=attribute.dimensionFieldEnName;
                associationList.add(dto);
            }
            model.field=associationList;
            list.add(model);
        }
        return list;
    }

    @Override
    public ResultEnum addDimensionAttribute(int dimensionId,List<DimensionAttributeDTO> dto)
    {
        //判断列名是否重复
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,dimensionId);
        boolean isExit=false;
        List<DimensionAttributePO> list=new ArrayList<>();
        for (DimensionAttributeDTO item:dto)
        {
            DimensionAttributePO po=attributeMapper.selectOne(queryWrapper.lambda()
                    .eq(DimensionAttributePO::getDimensionFieldEnName,item.dimensionFieldEnName)
            );
            if (po !=null)
            {
                isExit=true;
                break;
            }
            DimensionAttributePO data= DimensionAttributeMap.INSTANCES.dtoToPo(item);
            data.dimensionId=dimensionId;
            list.add(data);
        }
        if (isExit)
        {
            return ResultEnum.DATA_EXISTS;
        }
        boolean flat=this.saveBatch(list);
        if (flat)
        {
            try{
                DimensionAttributeAddDTO pushDto=new DimensionAttributeAddDTO();
                pushDto.dimensionId=dimensionId;
                pushDto.createType=CreateTypeEnum.CREATE_DIMENSION.getValue();
                pushDto.userId=userHelper.getLoginUserInfo().id;
                //发送消息
                publishTaskClient.publishBuildAtlasDorisTableTask(pushDto);
            }
            catch (Exception ex){
                log.error(ex.getMessage());
                return ResultEnum.SUCCESS;
            }
        }
        return flat?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<Integer> deleteDimensionAttribute(List<Integer> ids)
    {
        DimensionAttributePO po=attributeMapper.selectById(ids.get(0));
        int flat=attributeMapper.deleteBatchIds(ids);
        return ResultEntityBuild.build(flat>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR,po.dimensionId);
    }

    @Override
    public List<DimensionAttributeListDTO> getDimensionAttributeList(int dimensionId)
    {
        return attributeMapper.getDimensionAttributeList(dimensionId);
    }

    @Override
    public ResultEnum updateDimensionAttribute(DimensionAttributeUpdateDTO dto)
    {
        DimensionAttributePO po=attributeMapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,po.dimensionId)
                .eq(DimensionAttributePO::getDimensionFieldEnName,dto.dimensionFieldEnName);
        DimensionAttributePO model=attributeMapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        po.dimensionFieldCnName=dto.dimensionFieldCnName;
        po.dimensionFieldDes=dto.dimensionFieldDes;
        po.dimensionFieldLength=dto.dimensionFieldLength;
        po.dimensionFieldEnName=dto.dimensionFieldEnName;
        po.dimensionFieldType=dto.dimensionFieldType;
        ////po=DimensionAttributeMap.INSTANCES.updateDtoToPo(dto);
        return attributeMapper.updateById(po)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ModelMetaDataDTO getDimensionMetaData(int id)
    {
        ModelMetaDataDTO data=new ModelMetaDataDTO();
        DimensionPO po=mapper.selectById(id);
        if (po==null)
        {
            return data;
        }
        data.tableName =po.dimensionTabName;
        data.id=po.id;
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,id);
        List<ModelAttributeMetaDataDTO> dtoList=new ArrayList<>();
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        for (DimensionAttributePO item:list) {
            ModelAttributeMetaDataDTO dto = new ModelAttributeMetaDataDTO();
            //判断是否为关联维度
            if (item.attributeType==DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue())
            {
                //获取维度关联维度表名称,用于创建关联key
                DimensionAttributePO attributePO=attributeMapper.selectById(item.associateDimensionId);
                if (attributePO==null)
                {
                    break;
                }
                DimensionPO dimensionPO=mapper.selectById(attributePO.dimensionId);
                dto.associationTable=dimensionPO==null?"":dimensionPO.dimensionTabName;
            }
            dto.attributeType = item.attributeType;
            dto.fieldEnName = item.dimensionFieldEnName;
            dto.fieldLength = item.dimensionFieldLength;
            dto.fieldType = item.dimensionFieldType;
            dtoList.add(dto);
        }
        data.dto=dtoList;
        return data;
    }

    @Override
    public List<DimensionAttributeAssociationDTO> getDimensionAttributeData(int id)
    {
        List<DimensionAttributeAssociationDTO> data=new ArrayList<>();
        DimensionPO po=mapper.selectById(id);
        if (po==null)
        {
            return data;
        }
        QueryWrapper <DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,id)
                .ne(DimensionAttributePO::getAttributeType,DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue());
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        for (DimensionAttributePO item:list) {
            DimensionAttributeAssociationDTO dto = new DimensionAttributeAssociationDTO();
            dto.id = item.id;
            dto.dimensionFieldEnName = item.dimensionFieldEnName;
            data.add(dto);
        }
        return data;
    }

}
