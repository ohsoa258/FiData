package com.fisk.datamodel.service.impl.widetable;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamodel.dto.widetablefieldconfig.WideTableFieldConfigsDTO;
import com.fisk.datamodel.entity.widetable.WideTableFieldConfigPO;
import com.fisk.datamodel.map.widetable.WideTableFieldConfigMap;
import com.fisk.datamodel.mapper.widetable.WideTableFieldConfigMapper;
import com.fisk.datamodel.service.IWideTableFieldConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class WideTableFieldConfigImpl
        extends ServiceImpl<WideTableFieldConfigMapper, WideTableFieldConfigPO>
        implements IWideTableFieldConfig {

    @Resource
    WideTableFieldConfigMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum wideTableFieldConfig(int wideTableId, List<WideTableFieldConfigsDTO> dtoList) {
        if (deleteWideTableFieldConfig(wideTableId) != ResultEnum.SUCCESS) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return addWideTableFieldConfig(dtoList);
    }

    public ResultEnum deleteWideTableFieldConfig(int wideTableId) {
        QueryWrapper<WideTableFieldConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableFieldConfigPO::getWideTableId, wideTableId);
        List<WideTableFieldConfigPO> poList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return ResultEnum.SUCCESS;
        }
        if (!this.remove(queryWrapper)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    public ResultEnum addWideTableFieldConfig(List<WideTableFieldConfigsDTO> dtoList) {
        List<WideTableFieldConfigPO> pos = WideTableFieldConfigMap.INSTANCES.dtoListToPoList(dtoList);
        boolean flat = this.saveBatch(pos);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<WideTableFieldConfigsDTO> getWideTableFieldConfig(int wideTableId) {
        QueryWrapper<WideTableFieldConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableFieldConfigPO::getWideTableId, wideTableId);
        return WideTableFieldConfigMap.INSTANCES.poListToDtoList(mapper.selectList(queryWrapper));
    }

}
