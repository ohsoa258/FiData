package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.lineagemaprelation.LineageMapRelationDTO;
import com.fisk.datamanagement.entity.LineageMapRelationPO;
import com.fisk.datamanagement.enums.ProcessTypeEnum;
import com.fisk.datamanagement.map.LineageMapRelationMap;
import com.fisk.datamanagement.mapper.LineageMapRelationMapper;
import com.fisk.datamanagement.service.ILineageMapRelation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class LineageMapRelationImpl
        extends ServiceImpl<LineageMapRelationMapper, LineageMapRelationPO>
        implements ILineageMapRelation {

    @Resource
    LineageMapRelationMapper mapper;
    @Resource
    MetadataEntityImpl metadataEntity;

    @Override
    public ResultEnum addLineageMapRelation(List<LineageMapRelationDTO> dtoList) {

        List<LineageMapRelationPO> poList = LineageMapRelationMap.INSTANCES.dtoListToPoList(dtoList);
        boolean flat = this.saveBatch(poList);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultEnum delLineageMapRelationProcess(Integer toEntityId, ProcessTypeEnum processTypeEnum) {

        QueryWrapper<LineageMapRelationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(LineageMapRelationPO::getToEntityId, toEntityId)
                .eq(LineageMapRelationPO::getProcessType, processTypeEnum.getValue());

        List<LineageMapRelationPO> poList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return ResultEnum.SUCCESS;
        }

        boolean remove = this.remove(queryWrapper);
        if (!remove) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        //删除process
        List<Integer> collect = poList.stream().map(e -> e.metadataEntityId).collect(Collectors.toList());
        metadataEntity.delMetadataEntity(collect);

        return ResultEnum.SUCCESS;
    }
}
