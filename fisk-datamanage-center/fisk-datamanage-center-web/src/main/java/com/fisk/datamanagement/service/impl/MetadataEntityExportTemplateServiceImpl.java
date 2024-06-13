package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.EditMetadataExportTemplateDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateAttributeDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateDetailDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateDto;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.fisk.datamanagement.entity.MetadataEntityExportTemplateAttributePO;
import com.fisk.datamanagement.entity.MetadataEntityExportTemplatePO;
import com.fisk.datamanagement.map.MetadataEntityExportTemplateMap;
import com.fisk.datamanagement.mapper.MetadataEntityExportTemplateAttributeMapper;
import com.fisk.datamanagement.service.IMetadataEntityExportTemplateService;
import com.fisk.datamanagement.mapper.MetadataEntityExportTemplateMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* @author JinXingWang
* @description 针对表【tb_metadata_entity_export_template】的数据库操作Service实现
* @createDate 2023-08-16 16:30:25
*/
@Service
public class MetadataEntityExportTemplateServiceImpl extends ServiceImpl<MetadataEntityExportTemplateMapper, MetadataEntityExportTemplatePO>
    implements IMetadataEntityExportTemplateService {

    @Resource
    MetadataEntityExportTemplateAttributeMapper mapper;

    @Resource

    MetadataEntityExportTemplateAttributeServiceImpl metadataEntityExportTemplateAttributeService;

    @Override
    public MetadataExportTemplateDetailDto get(Integer id) {
        LambdaQueryWrapper<MetadataEntityExportTemplatePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetadataEntityExportTemplatePO::getId,id);
        MetadataEntityExportTemplatePO exportTemplatePO = getOne(wrapper);
        List<MetadataExportTemplateAttributeDto> attributeTreeVoList = metadataEntityExportTemplateAttributeService.getTemplateAttributeTreeByTemplateId(Long.valueOf(exportTemplatePO.getId()).intValue());
        MetadataExportTemplateDetailDto metadataExportTemplateVO=new MetadataExportTemplateDetailDto();
        metadataExportTemplateVO.setName(exportTemplatePO.getName());
        metadataExportTemplateVO.setAttribute(attributeTreeVoList);
        return  metadataExportTemplateVO;

    }

    @Override
    public ResultEnum add(MetadataExportTemplateDetailDto dto) {
        //添加模板
        MetadataEntityExportTemplatePO metadataEntityExportTemplatePO = MetadataEntityExportTemplateMap.INSTANCES.dtoToPo(dto);
        this.save(metadataEntityExportTemplatePO);
        //添加模板下的属性
        List<MetadataEntityExportTemplateAttributePO> addPoList=new ArrayList<>();
        metadataEntityExportTemplateAttributeService.addBatch(Long.valueOf(metadataEntityExportTemplatePO.getId()).intValue(),dto.getAttribute());
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum edit(EditMetadataExportTemplateDto dto) {
        if (dto.getId().equals(1)){
            //不能修改默认模板
            return ResultEnum.DETAULT_EDIT_ERROR;
        }else {
            MetadataEntityExportTemplatePO metadataEntityExportTemplatePO = MetadataEntityExportTemplateMap.INSTANCES.editDtoToPo(dto);
            updateById(metadataEntityExportTemplatePO);
            metadataEntityExportTemplateAttributeService.editBatch(Long.valueOf(metadataEntityExportTemplatePO.getId()).intValue(),dto.getAttribute());
            return ResultEnum.SUCCESS;
        }
    }

    @Override
    public ResultEnum delete(Integer id) {
        if (id.equals(1)){
            //不能删除默认模板
            return ResultEnum.DETAULT_EDIT_ERROR;
        }else {
            LambdaQueryWrapper<MetadataEntityExportTemplatePO> del = new LambdaQueryWrapper<>();
            del.eq(MetadataEntityExportTemplatePO::getId, id);
            remove(del);

            metadataEntityExportTemplateAttributeService.delete(id);
            return ResultEnum.SUCCESS;
        }

    }

    @Override
    public List<MetadataExportTemplateDto> getAllTemplate(){
        List<MetadataEntityExportTemplatePO> list = list();
        return MetadataEntityExportTemplateMap.INSTANCES.poToDtoList(list);
    }
}




