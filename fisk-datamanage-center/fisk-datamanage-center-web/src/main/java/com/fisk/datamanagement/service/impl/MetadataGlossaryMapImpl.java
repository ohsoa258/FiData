package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.metadataglossarymap.GlossaryAndMetaDatasMapDTO;
import com.fisk.datamanagement.dto.metadataglossarymap.GlossaryMapDelDTO;
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
import java.util.Set;
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

        //查询该术语下所有关联关系  如果有重复的则剔除
        Integer glossaryId = dto.getGlossaryId();
        List<MetadataEntitySimpleDTO> metadataEntitys = dto.getMetadataEntityIds();

        LambdaQueryWrapper<MetaDataGlossaryMapPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(MetaDataGlossaryMapPO::getMetadataQualifiedName)
                .eq(MetaDataGlossaryMapPO::getGlossaryId, glossaryId);
        Set<String> qNamesSet = list(wrapper).stream()
                .map(MetaDataGlossaryMapPO::getMetadataQualifiedName)
                .collect(Collectors.toSet());

        //如果这次保存时 与已绑定的重复 则剔除
        metadataEntitys.removeIf(simDto -> qNamesSet.contains(simDto.getMetadataQualifiedName()));

        // 获取本次术语要绑定的元数据集合
        // 元数据集合为空则不往下进行
        if (CollectionUtils.isEmpty(metadataEntitys)) {
            return null;
        }

        ArrayList<MetaDataGlossaryMapPO> metaDataGlossaryMapPOS = new ArrayList<>();

        //获取术语 和 元数据对象们 的map对象
        for (MetadataEntitySimpleDTO simDto : metadataEntitys) {
            MetaDataGlossaryMapPO metaDataGlossaryMapPO = new MetaDataGlossaryMapPO();

            if (simDto.getMetadataQualifiedName()==null){
                continue;
            }
            //改为关联限定名称
            metaDataGlossaryMapPO.setMetadataQualifiedName(simDto.getMetadataQualifiedName());
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
            //筛选出元数据限定名称集合
            List<String> collect = list.stream().map(MetaDataGlossaryMapPO::getMetadataQualifiedName).collect(Collectors.toList());
            //若是没绑定任何元数据则返回空集合
            if (CollectionUtils.isEmpty(collect)) {
                return metadataEntitySimpleDTOS;
            }

            List<MetadataEntityPO> metadataEntityPOS = metadataEntityImpl.list(
                    new LambdaQueryWrapper<MetadataEntityPO>()
                            .select(MetadataEntityPO::getId, MetadataEntityPO::getQualifiedName, MetadataEntityPO::getName, MetadataEntityPO::getTypeId, MetadataEntityPO::getParentId)
                            .in(MetadataEntityPO::getQualifiedName, collect)
            );

            for (MetadataEntityPO metadataEntityPO : metadataEntityPOS) {
                MetadataEntitySimpleDTO dto = new MetadataEntitySimpleDTO();
                dto.setId(metadataEntityPO.getId());
                dto.setPId(metadataEntityPO.getParentId());

                //获取元数据的父名称
                MetadataEntityPO one = metadataEntityImpl.getOne(
                        new LambdaQueryWrapper<MetadataEntityPO>()
                                .select(MetadataEntityPO::getName)
                                .eq(MetadataEntityPO::getId, metadataEntityPO.getParentId())
                );
                if (one != null) dto.setParentName(one.name);

                dto.setMetadataQualifiedName(metadataEntityPO.getQualifiedName());
                dto.setEntityName(metadataEntityPO.getName());
                dto.setType(metadataEntityPO.getTypeId());
                metadataEntitySimpleDTOS.add(dto);
            }
            return metadataEntitySimpleDTOS;
        } catch (Exception e) {
            log.error("获取术语绑定的元数据失败：" + e);
            throw new FkException(ResultEnum.GET_GLOSSARY_ASSIGN_METAS_ERROR);
        }
    }

    /**
     * 业务术语-根据术语id和元数据限定名称 删除关联关系
     *
     * @param dto
     * @return
     */
    @Override
    public Object delGlossaryMapByGIDAndQName(GlossaryMapDelDTO dto) {
        int glossaryId = dto.getGlossaryId();
        if (dto.getMetadataQualifiedName() == null) {
            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
        }

        return this.remove(
                new LambdaQueryWrapper<MetaDataGlossaryMapPO>()
                        .eq(MetaDataGlossaryMapPO::getGlossaryId, glossaryId)
                        .eq(MetaDataGlossaryMapPO::getMetadataQualifiedName, dto.getMetadataQualifiedName())
        );
    }

    /**
     * 业务术语-根据术语id批量删除和元数据的关联关系
     *
     * @param glossaryId
     * @return
     */
    @Override
    public Object delAllGlossaryMaps(Integer glossaryId) {
        return this.remove(
                new LambdaQueryWrapper<MetaDataGlossaryMapPO>()
                        .eq(MetaDataGlossaryMapPO::getGlossaryId, glossaryId)
        );
    }

}
