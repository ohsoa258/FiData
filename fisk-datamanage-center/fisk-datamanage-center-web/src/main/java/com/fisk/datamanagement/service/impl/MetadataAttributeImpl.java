package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.ObjectInfoUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.entity.MetadataAttributePO;
import com.fisk.datamanagement.mapper.MetadataAttributeMapper;
import com.fisk.datamanagement.service.IMetadataAttribute;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Service
public class MetadataAttributeImpl
        extends ServiceImpl<MetadataAttributeMapper, MetadataAttributePO>
        implements IMetadataAttribute {

    @Resource
    MetadataAttributeMapper mapper;

    /**
     * 操作元数据属性
     *
     * @param object
     * @param entityId
     * @return
     */
    public ResultEnum operationMetadataAttribute(Object object, Integer entityId) {
        delMetadataAttribute(entityId);

        addMetadataAttribute(object, entityId);

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum addMetadataAttribute(Object object, Integer entityId) {
        Map<String, Object> map = ObjectInfoUtils.getObjectKeyAndValue(object);

        List<MetadataAttributePO> dataList = new ArrayList<>();

        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            MetadataAttributePO po = new MetadataAttributePO();
            po.metadataEntityId = entityId;
            String key = iterator.next();
            po.name = key;
            po.value = map.get(key).toString();

            dataList.add(po);
        }

        boolean flat = this.saveBatch(dataList);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delMetadataAttribute(Integer entityId) {
        QueryWrapper<MetadataAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataAttributePO::getMetadataEntityId, entityId);

        List<MetadataAttributePO> metadataAttributePOS = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(metadataAttributePOS)) {
            return ResultEnum.SUCCESS;
        }

        boolean remove = this.remove(queryWrapper);
        if (!remove) {
            throw new FkException(ResultEnum.DATA_SUBMIT_ERROR);
        }

        return ResultEnum.SUCCESS;

    }

}
