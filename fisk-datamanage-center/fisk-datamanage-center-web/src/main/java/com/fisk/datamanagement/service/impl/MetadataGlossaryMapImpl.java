package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.metadataglossarymap.GlossaryAndMetaDatasMapDTO;
import com.fisk.datamanagement.dto.metadataglossarymap.MetadataEntitySimpleDTO;
import com.fisk.datamanagement.entity.MetaDataGlossaryMapPO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import com.fisk.datamanagement.mapper.MetaDataGlossaryMapMapper;
import com.fisk.datamanagement.service.IMetadataGlossaryMap;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetadataGlossaryMapImpl
        extends ServiceImpl<MetaDataGlossaryMapMapper, MetaDataGlossaryMapPO>
        implements IMetadataGlossaryMap {

    @Resource
    private MetadataEntityImpl metadataEntityImpl;

    /**
     * 业务术语-关联元数据id
     *
     * @param dto
     * @return
     */
    @Override
    public Object mapGlossaryWithMetaEntity(GlossaryAndMetaDatasMapDTO dto) {
        //先根据术语id删除所有关联关系
        Integer glossaryId = dto.getGlossaryId();
        LambdaQueryWrapper<MetaDataGlossaryMapPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaDataGlossaryMapPO::getGlossaryId, glossaryId);
        remove(wrapper);

        // 获取本次术语要绑定的元数据集合
        List<MetadataEntitySimpleDTO> metadataEntityIds = dto.getMetadataEntityIds();
        // 元数据集合为空则不往下进行
        if (CollectionUtils.isEmpty(metadataEntityIds)) {
            return null;
        }

        ArrayList<MetaDataGlossaryMapPO> metaDataGlossaryMapPOS = new ArrayList<>();

        //获取术语 和 元数据对象们 的map对象
        for (MetadataEntitySimpleDTO simDto : metadataEntityIds) {
            MetaDataGlossaryMapPO metaDataGlossaryMapPO = new MetaDataGlossaryMapPO();

            metaDataGlossaryMapPO.setMetadataEntityId((int) simDto.getId());
            metaDataGlossaryMapPO.setGlossaryId(glossaryId);
            metaDataGlossaryMapPO.setTypeId(simDto.getType());
            metaDataGlossaryMapPOS.add(metaDataGlossaryMapPO);
        }

        //保存
        return saveBatch(metaDataGlossaryMapPOS);
    }

    /**
     * 业务术语-回显该术语关联的所有元数据信息
     *
     * @param glossaryId
     * @return
     */
    @Override
    public List<MetadataEntitySimpleDTO> getMetaEntitiesByGlossary(Integer glossaryId) {
        List<MetadataEntitySimpleDTO> metadataEntitySimpleDTOS = new ArrayList<>();
        try {
            //获取术语下绑定的所有元数据
            LambdaQueryWrapper<MetaDataGlossaryMapPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MetaDataGlossaryMapPO::getGlossaryId, glossaryId);
            List<MetaDataGlossaryMapPO> list = list(wrapper);
            //筛选出元数据id集合
            List<Integer> collect = list.stream().map(MetaDataGlossaryMapPO::getMetadataEntityId).collect(Collectors.toList());
            //若是没绑定任何元数据则返回空集合
            if (CollectionUtils.isEmpty(collect)){
                return metadataEntitySimpleDTOS;
            }

            List<MetadataEntityPO> metadataEntityPOS = metadataEntityImpl.listByIds(collect);
            for (MetadataEntityPO metadataEntityPO : metadataEntityPOS) {
                MetadataEntitySimpleDTO dto = new MetadataEntitySimpleDTO();
                dto.setId(metadataEntityPO.getId());
                dto.setEntityName(metadataEntityPO.getName());
                dto.setType(metadataEntityPO.getTypeId());
                metadataEntitySimpleDTOS.add(dto);
            }
            return metadataEntitySimpleDTOS;
        }catch (Exception e){
            log.error("获取术语绑定的元数据失败："+e.getMessage());
            log.error("获取术语绑定的元数据失败堆栈："+e);
            throw new FkException(ResultEnum.GET_GLOSSARY_ASSIGN_METAS_ERROR);
        }
    }

}
