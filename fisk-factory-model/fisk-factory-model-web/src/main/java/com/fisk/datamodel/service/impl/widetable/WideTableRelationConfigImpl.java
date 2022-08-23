package com.fisk.datamodel.service.impl.widetable;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamodel.dto.widetablerelationconfig.WideTableRelationConfigDTO;
import com.fisk.datamodel.entity.widetable.WideTableRelationConfigPO;
import com.fisk.datamodel.map.widetable.WideTableRelationConfigMap;
import com.fisk.datamodel.mapper.widetable.WideTableRelationConfigMapper;
import com.fisk.datamodel.service.IWideTableRelationConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class WideTableRelationConfigImpl
        extends ServiceImpl<WideTableRelationConfigMapper, WideTableRelationConfigPO>
        implements IWideTableRelationConfig {

    @Resource
    WideTableRelationConfigMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum wideTableRelationConfig(int wideTableId, List<WideTableRelationConfigDTO> dtoList) {
        if (deleteWideTableRelationConfig(wideTableId) != ResultEnum.SUCCESS) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return addWideTableRelationConfig(wideTableId, dtoList);
    }

    public ResultEnum deleteWideTableRelationConfig(int wideTableId) {
        QueryWrapper<WideTableRelationConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableRelationConfigPO::getWideTableId, wideTableId);
        List<WideTableRelationConfigPO> poList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return ResultEnum.SUCCESS;
        }
        if (!this.remove(queryWrapper)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    public ResultEnum addWideTableRelationConfig(int wideTableId, List<WideTableRelationConfigDTO> dtoList) {
        List<WideTableRelationConfigPO> pos = WideTableRelationConfigMap.INSTANCES.dtoToPo(dtoList);
        pos.stream().map(e -> e.wideTableId = wideTableId).collect(Collectors.toList());
        boolean flat = this.saveBatch(pos);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }


}
