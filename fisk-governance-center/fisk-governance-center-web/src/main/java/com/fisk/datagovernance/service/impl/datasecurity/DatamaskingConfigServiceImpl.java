package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.datasecurity.DatamaskingConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.datamasking.DataSourceIdDTO;
import com.fisk.datagovernance.entity.datasecurity.DatamaskingConfigPO;
import com.fisk.datagovernance.map.datasecurity.DatamaskingConfigMap;
import com.fisk.datagovernance.mapper.datasecurity.DatamaskingConfigMapper;
import com.fisk.datagovernance.service.datasecurity.DatamaskingConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:48
 */
@Service
public class DatamaskingConfigServiceImpl extends ServiceImpl<DatamaskingConfigMapper, DatamaskingConfigPO> implements DatamaskingConfigService {

    @Override
    public DatamaskingConfigDTO getData(long id) {

        DatamaskingConfigPO po = this.getById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // po -> dto
        return DatamaskingConfigMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum addData(DatamaskingConfigDTO dto) {

        // TODO NAME为需要判断的字段名
        // 当前字段名不可重复
        List<String> list = this.list().stream().map(e -> e.fieldName).collect(Collectors.toList());
        if (list.contains(dto.fieldName)) {
            return ResultEnum.FIELD_NAME_IS_SELECTED;
        }

        // dto -> po
        DatamaskingConfigPO model = DatamaskingConfigMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        //保存
        return this.save(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(DatamaskingConfigDTO dto) {
        // TODO NAME为需要判断的字段名
        // 判断名称是否重复
        QueryWrapper<DatamaskingConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DatamaskingConfigPO::getFieldName, dto.fieldName);
        DatamaskingConfigPO po = baseMapper.selectOne(queryWrapper);
        if (po != null && po.id != dto.id) {
            return ResultEnum.FIELD_NAME_IS_SELECTED;
        }

        // 参数校验
        DatamaskingConfigPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        // 执行修改
        return this.updateById(DatamaskingConfigMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(long id) {

        // 参数校验
        DatamaskingConfigPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 执行删除
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DatamaskingConfigDTO> getList(DataSourceIdDTO dto) {

        List<DatamaskingConfigPO> listPo = this.query()
                .eq("datasource_id", dto.datasourceId)
                .eq("table_id", dto.tableId)
                .orderByDesc("create_time").list();

        return DatamaskingConfigMap.INSTANCES.listPoToDto(listPo);
    }

}