package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.dimensionattribute.ModelAttributeMetaDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeListDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeUpdateDTO;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.DimensionAttributeEnum;
import com.fisk.datamodel.map.FactAttributeMap;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IFactAttribute;
import com.fisk.task.client.PublishTaskClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class FactAttributeImpl
        extends ServiceImpl<FactAttributeMapper,FactAttributePO>
        implements IFactAttribute {

    @Resource
    FactMapper factMapper;
    @Resource
    FactAttributeMapper mapper;
    @Resource
    DimensionAttributeMapper attributeMapper;
    @Resource
    UserHelper userHelper;
    @Resource
    PublishTaskClient publishTaskClient;

    @Override
    public List<FactAttributeListDTO> getFactAttributeList(int factId)
    {
        return mapper.getFactAttributeList(factId);
    }

    @Override
    public ResultEnum addFactAttribute(int factId, List<FactAttributeDTO> dto) {
        QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId, factId);
        boolean isExit = false;
        List<FactAttributePO> list = new ArrayList<>();
        for (FactAttributeDTO item : dto) {
            FactAttributePO po = mapper.selectOne(queryWrapper.lambda()
                    .eq(FactAttributePO::getFactFieldEnName, item.factFieldEnName));
            if (po != null) {
                isExit = true;
                break;
            }
            FactAttributePO data = FactAttributeMap.INSTANCES.dtoToPo(item);
            data.factId = factId;
            list.add(data);
        }
        if (isExit) {
            return ResultEnum.DATA_EXISTS;
        }
        boolean flat=this.saveBatch(list);
        if (flat)
        {
            try
            {
                DimensionAttributeAddDTO pushDto=new DimensionAttributeAddDTO();
                pushDto.dimensionId=factId;
                pushDto.createType= CreateTypeEnum.CREATE_FACT.getValue();
                pushDto.userId=userHelper.getLoginUserInfo().id;
                //发送消息
                publishTaskClient.publishBuildAtlasDorisTableTask(pushDto);
            }
            catch (Exception ex){
                log.error(ex.getMessage());
                return ResultEnum.SUCCESS;
            }

        }
        return flat ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }


    @Override
    public ResultEnum deleteFactAttribute(List<Integer> ids)
    {
        return mapper.deleteBatchIds(ids)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateFactAttribute(FactAttributeUpdateDTO dto)
    {
        FactAttributePO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId,po.factId)
                .eq(FactAttributePO::getFactFieldEnName,dto.factFieldEnName);
        FactAttributePO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        po.factFieldCnName=dto.factFieldCnName;
        po.factFieldDes=dto.factFieldDes;
        po.factFieldLength=dto.factFieldLength;
        po.factFieldEnName=dto.factFieldEnName;
        po.factFieldType=dto.factFieldType;
        ////po=DimensionAttributeMap.INSTANCES.updateDtoToPo(dto);
        return mapper.updateById(po)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ModelMetaDataDTO getFactMetaData(int id)
    {
        ModelMetaDataDTO data=new ModelMetaDataDTO();
        FactPO po=factMapper.selectById(id);
        if (po==null)
        {
            return data;
        }
        data.tableName =po.factTableEnName;
        data.id=po.id;
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId,id);
        List<ModelAttributeMetaDataDTO> dtoList=new ArrayList<>();
        List<FactAttributePO> list=mapper.selectList(queryWrapper);
        for (FactAttributePO item:list)
        {
            ModelAttributeMetaDataDTO dto=new ModelAttributeMetaDataDTO();
            //判断是否为关联维度
            if (item.attributeType== DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue())
            {
                //查看关联维度字段相关信息
                DimensionAttributePO po1=attributeMapper.selectById(item.associateDimensionId);
                if (po1 !=null)
                {
                    dto.attributeType=DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue();
                    dto.fieldEnName=po1.dimensionFieldEnName;
                    dto.fieldLength=po1.dimensionFieldLength;
                    dto.fieldType=po1.dimensionFieldType;
                    dtoList.add(dto);
                }
            }
            else {
                dto.attributeType=item.attributeType;
                dto.fieldEnName=item.factFieldEnName;
                dto.fieldLength=item.factFieldLength;
                dto.fieldType=item.factFieldType;
                dtoList.add(dto);
            }
        }
        data.dto=dtoList;
        return data;
    }

}
