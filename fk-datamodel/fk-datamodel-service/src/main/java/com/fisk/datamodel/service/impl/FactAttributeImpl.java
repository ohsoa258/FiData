package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAssociationDTO;
import com.fisk.datamodel.dto.dimensionattribute.ModelAttributeMetaDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDropDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeListDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeUpdateDTO;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.DimensionAttributeEnum;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.map.FactAttributeMap;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
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
    DimensionMapper dimensionMapper;
    @Resource
    DimensionAttributeMapper attributeMapper;
    @Resource
    DataAccessClient client;

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
            if (item.attributeType==DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue())
            {
                DimensionAttributePO dimensionAttributePO=attributeMapper.selectById(item.associateDimensionFieldId);
                if (dimensionAttributePO !=null)
                {
                    data.associateDimensionId=dimensionAttributePO.dimensionId;
                }
            }
            list.add(data);
        }
        if (isExit) {
            return ResultEnum.DATA_EXISTS;
        }
        return this.saveBatch(list) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
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
        //获取注册表相关数据
        ResultEntity<AppRegistrationDTO> appAbbreviation = client.getData(po.appId);
        if (appAbbreviation.code==ResultEnum.SUCCESS.getCode() || appAbbreviation.data !=null)
        {
            data.appbAbreviation=appAbbreviation.data.appAbbreviation;
        }
        //获取来源表相关数据
        ResultEntity<TableAccessDTO> tableAccess = client.getTableAccess(po.tableSourceId);
        if (tableAccess.code==ResultEnum.SUCCESS.getCode() || tableAccess.data !=null)
        {
            data.sourceTableName=tableAccess.data.tableName;
        }
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId,id);
        List<ModelAttributeMetaDataDTO> dtoList=new ArrayList<>();
        List<FactAttributePO> list=mapper.selectList(queryWrapper);
        for (FactAttributePO item:list) {
            ModelAttributeMetaDataDTO dto =new ModelAttributeMetaDataDTO();
            dto.attributeType = item.attributeType;
            dto.fieldEnName = item.factFieldEnName;
            dto.fieldLength = item.factFieldLength;
            dto.fieldType = item.factFieldType;
            dto.fieldCnName = item.factFieldCnName;
            //判断是否为关联维度
            if (item.attributeType == DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue())
            {
                //获取维度关联维度表名称,用于创建关联key
                DimensionAttributePO attributePO=attributeMapper.selectById(item.associateDimensionFieldId);
                if (attributePO==null)
                {
                    break;
                }
                DimensionPO dimensionPO=dimensionMapper.selectById(attributePO.dimensionId);
                if (dimensionPO==null)
                {
                    break;
                }
                dto.associationTable=dimensionPO.dimensionTabName; //维度关联表名称
                dto.associationField=attributePO.dimensionFieldEnName; //维度关联字段名称
                //获取关联维度与本表关联字段名称
                FactAttributePO factAttributePO=mapper.selectById(item.associateId);
                dto.fieldEnName=factAttributePO.factFieldEnName; //关联维度与本表字段关联名称
                dto.sourceFieldId=factAttributePO.tableSourceFieldId; //本表字段来源
            }
            dtoList.add(dto);
        }
        data.dto=dtoList;
        return data;
    }

    @Override
    public List<FactAttributeDropDTO> GetFactAttributeData(int id)
    {
        List<FactAttributeDropDTO> data=new ArrayList<>();
        FactPO po=factMapper.selectById(id);
        if (po==null)
        {
            return data;
        }
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId,id)
                .ne(FactAttributePO::getAttributeType, FactAttributeEnum.ASSOCIATED_DIMENSION.getValue());
        List<FactAttributePO> list=mapper.selectList(queryWrapper);
        for (FactAttributePO item:list) {
            FactAttributeDropDTO dto = new FactAttributeDropDTO();
            dto.id = item.id;
            dto.factFieldEnName = item.factFieldEnName;
            data.add(dto);
        }
        return data;
    }

}
