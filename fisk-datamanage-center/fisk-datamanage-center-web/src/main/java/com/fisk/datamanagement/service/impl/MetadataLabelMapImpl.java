package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.metadatalabelmap.MetadataLabelMapParameter;
import com.fisk.datamanagement.entity.CategoryPO;
import com.fisk.datamanagement.entity.LabelPO;
import com.fisk.datamanagement.entity.MetadataLabelMapPO;
import com.fisk.datamanagement.mapper.MetadataLabelMapper;
import com.fisk.datamanagement.service.IMetadataLabelMap;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class MetadataLabelMapImpl
        extends ServiceImpl<MetadataLabelMapper, MetadataLabelMapPO>
        implements IMetadataLabelMap {

    @Resource
    MetadataLabelMapper mapper;
    @Resource
    CategoryImpl category;
    @Resource
    LabelImpl label;

    public ResultEnum operationMetadataLabelMap(MetadataLabelMapParameter dto) {
        delMetadataLabelMap(dto.metadataEntityId);

        addMetadataLabelMap(dto);

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum addMetadataLabelMap(MetadataLabelMapParameter dto) {
        if (CollectionUtils.isEmpty(dto.labelIds)) {
            return ResultEnum.SUCCESS;
        }

        List<MetadataLabelMapPO> list = new ArrayList<>();
        for (Integer id : dto.labelIds) {
            MetadataLabelMapPO po = new MetadataLabelMapPO();
            po.labelId = id;
            po.metadataEntityId = dto.metadataEntityId;
            list.add(po);
        }

        boolean flat = this.saveBatch(list);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delMetadataLabelMap(Integer metadataEntityId) {

        QueryWrapper<MetadataLabelMapPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataLabelMapPO::getMetadataEntityId, metadataEntityId);

        List<MetadataLabelMapPO> poList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return ResultEnum.SUCCESS;
        }

        boolean remove = this.remove(queryWrapper);
        if (!remove) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public List<Integer> getLabelIdList(Integer metadataEntityId) {
        List<MetadataLabelMapPO> list = this.query()
                .eq("metadata_entity_id", metadataEntityId)
                .select("label_id").list();

        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        return list.stream().map(e -> e.labelId).collect(Collectors.toList());

    }

    public List<Integer> getEntityLabelIdList(String labelName, Integer offset, Integer pageSize) {
        List<CategoryPO> poList = category.query().eq("category_cn_name", labelName).list();
        if (CollectionUtils.isEmpty(poList)) {
            return new ArrayList<>();
        }
        List<Long> collect = poList.stream().map(e -> e.id).collect(Collectors.toList());
        List<LabelPO> list = label.query().select("id").in("category_id", collect).list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<Long> collect1 = list.stream().map(e -> e.id).collect(Collectors.toList());
        List<MetadataLabelMapPO> entityIds = this.query().in("label_id", collect1).list();

        return entityIds.stream().map(e -> e.metadataEntityId).skip(offset)
                .limit(pageSize).collect(Collectors.toList());


    }

}
