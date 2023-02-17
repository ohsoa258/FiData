package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.lineagemaprelation.LineageMapRelationDTO;
import com.fisk.datamanagement.entity.LineageMapRelationPO;
import com.fisk.datamanagement.map.LineageMapRelationMap;
import com.fisk.datamanagement.mapper.LineageMapRelationMapper;
import com.fisk.datamanagement.service.ILineageMapRelation;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class LineageMapRelationImpl
        extends ServiceImpl<LineageMapRelationMapper, LineageMapRelationPO>
        implements ILineageMapRelation {

    @Override
    public ResultEnum addLineageMapRelation(List<LineageMapRelationDTO> dtoList) {

        List<LineageMapRelationPO> poList = LineageMapRelationMap.INSTANCES.dtoListToPoList(dtoList);
        boolean flat = this.saveBatch(poList);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;


    }
}
