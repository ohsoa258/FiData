package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.dimensionattribute.*;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.map.DimensionAttributeMap;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.service.IDimensionAttribute;
import com.fisk.datamodel.utils.MySqlTableUtils;
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
    MySqlTableUtils mySqlTableUtils;
    @Resource
    DimensionMapper mapper;
    @Resource
    DimensionAttributeMapper attributeMapper;

    @Override
    public List<DimensionMetaDTO> getProjectDimensionMeta()
    {
        return mySqlTableUtils.getTable();
    }

    @Override
    public List<DimensionAttributeAssociationDTO> getProjectDimensionTable()
    {
        List<DimensionAttributeAssociationDTO> list=new ArrayList<>();
        //获取维度表
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        List<DimensionPO> data=mapper.selectList(queryWrapper);
        //获取维度字段表
        QueryWrapper<DimensionAttributePO> queryWrapper2=new QueryWrapper<>();
        List<DimensionAttributePO> list2=attributeMapper.selectList(queryWrapper2);

        for (DimensionPO po:data)
        {
            DimensionAttributeAssociationDTO model=new DimensionAttributeAssociationDTO();
            model.tableName=po.dimensionTabName;
            model.associateDimensionId=po.id;
            List<DimensionAttributePO> filter=list2.stream().filter(e->e.getDimensionId()==po.id).collect(Collectors.toList());
            List<String> ids=new ArrayList<>();
            for (DimensionAttributePO attribute:filter)
            {
                ids.add(attribute.dimensionFieldEnName);
            }
            model.field=ids;
            list.add(model);
        }
        return list;
    }

    @Override
    public ResultEnum addDimensionAttribute(int dimensionId,List<DimensionAttributeDTO> dto)
    {
        //判断是否重复添加
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,dimensionId);
        boolean isExit=false;
        List<DimensionAttributePO> list=new ArrayList<>();
        for (DimensionAttributeDTO item:dto)
        {
            DimensionAttributePO po=attributeMapper.selectOne(queryWrapper.lambda()
                    .eq(DimensionAttributePO::getDimensionFieldEnName,item.dimensionFieldEnName)
                    .eq(DimensionAttributePO::getTableSourceFieldId,item.tableSourceFieldId)
                    .eq(DimensionAttributePO::getAttributeType,item.attributeType)
                    .eq(DimensionAttributePO::getDimensionFieldType,item.dimensionFieldType)
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
        return this.saveBatch(list)==true?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDimensionAttribute(List<Integer> ids)
    {
        return attributeMapper.deleteBatchIds(ids)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
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
        po.dimensionFieldCnName=dto.dimensionFieldCnName;
        po.dimensionFieldDes=dto.dimensionFieldDes;
        po.dimensionFieldLength=dto.dimensionFieldLength;
        po.dimensionFieldEnName=dto.dimensionFieldEnName;
        po.dimensionFieldType=dto.dimensionFieldType;
        //po=DimensionAttributeMap.INSTANCES.updateDtoToPo(dto);
        return attributeMapper.updateById(po)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

}
