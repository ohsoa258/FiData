package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateAttributeDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateDto;
import com.fisk.datamanagement.entity.MetadataEntityExportTemplateAttributePO;
import com.fisk.datamanagement.map.MetadataEntityExportTemplateAttributeMap;
import com.fisk.datamanagement.map.MetadataEntityExportTemplateMap;
import com.fisk.datamanagement.mapper.MetadataEntityExportTemplateAttributeMapper;
import com.fisk.datamanagement.service.IMetadataEntityExportTemplateAttributeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JinXingWang
 * @description 针对表【tb_metadata_entity_export_template_attribute】的数据库操作Service实现
 * @createDate 2023-08-16 16:18:32
 */
@Service
public class MetadataEntityExportTemplateAttributeServiceImpl extends ServiceImpl<MetadataEntityExportTemplateAttributeMapper, MetadataEntityExportTemplateAttributePO>
        implements IMetadataEntityExportTemplateAttributeService {

    @Override
    public List<MetadataExportTemplateAttributeDto> getTemplateAttributeTreeByTemplateId(Integer templateId) {
        LambdaQueryWrapper<MetadataEntityExportTemplateAttributePO> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(MetadataEntityExportTemplateAttributePO::getTemplateId,templateId);
        List<MetadataEntityExportTemplateAttributePO> allAttributeList = list(wrapper);

        List<MetadataEntityExportTemplateAttributePO> oneAttributeList = allAttributeList.stream().filter(e -> e.getAttributePid().equals(0)).collect(Collectors.toList());
        List<MetadataExportTemplateAttributeDto> attributeTreeList=new ArrayList<>();
        for (MetadataEntityExportTemplateAttributePO metadataExportTemplateAttributePO : oneAttributeList) {
            MetadataExportTemplateAttributeDto metadataExportTemplateAttributeTreeVO = MetadataEntityExportTemplateAttributeMap.INSTANCES.poToDto(metadataExportTemplateAttributePO);
            List<MetadataEntityExportTemplateAttributePO> twoAttributeList = allAttributeList.stream()
                    .filter(e -> e.getAttributePid().equals(metadataExportTemplateAttributePO.getAttributeId()))
                    .collect(Collectors.toList());
            metadataExportTemplateAttributeTreeVO.children=MetadataEntityExportTemplateAttributeMap.INSTANCES.poToDtoList(twoAttributeList);
            attributeTreeList.add(metadataExportTemplateAttributeTreeVO);
        }
        return attributeTreeList;
    }

    @Override
    public List<MetadataExportTemplateAttributeDto> getTemplateValidityAttributeByTemplateId(Integer templateId){
        LambdaQueryWrapper<MetadataEntityExportTemplateAttributePO> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(MetadataEntityExportTemplateAttributePO::getTemplateId,templateId);
        wrapper.eq(MetadataEntityExportTemplateAttributePO::getChecked,true);
        List<MetadataEntityExportTemplateAttributePO> allAttributeList = list(wrapper);
        return  MetadataEntityExportTemplateAttributeMap.INSTANCES.poToDtoList(allAttributeList);
    }

    @Override
    public ResultEnum addBatch(Integer templateId, List<MetadataExportTemplateAttributeDto> dtoList) {
        List<MetadataEntityExportTemplateAttributePO> metadataEntityExportTemplateAttributePOList=new ArrayList<>();
        for (MetadataExportTemplateAttributeDto metadataExportTemplateAttributeDto : dtoList) {
            MetadataEntityExportTemplateAttributePO oneAttributePO = MetadataEntityExportTemplateAttributeMap.INSTANCES.dtoToPo(metadataExportTemplateAttributeDto);
            //一级默认为选中状态
            oneAttributePO.setChecked(true);
            metadataEntityExportTemplateAttributePOList.add(oneAttributePO);
            List<MetadataEntityExportTemplateAttributePO> twoLevelAttributeList = MetadataEntityExportTemplateAttributeMap.INSTANCES.dtoToPoList(metadataExportTemplateAttributeDto.getChildren());
            metadataEntityExportTemplateAttributePOList.addAll(twoLevelAttributeList);
        }
        metadataEntityExportTemplateAttributePOList.stream().forEach(e->e.setTemplateId(templateId));
        saveBatch(metadataEntityExportTemplateAttributePOList);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editBatch(Integer templateId , List<MetadataExportTemplateAttributeDto> dtoList){
        //删除模板下所有属性
        LambdaQueryWrapper<MetadataEntityExportTemplateAttributePO> delete = new LambdaQueryWrapper<>();
        delete.eq(MetadataEntityExportTemplateAttributePO::getTemplateId,templateId);
        remove(delete);
        addBatch(templateId,dtoList);
        return ResultEnum.SUCCESS;
    }

}




