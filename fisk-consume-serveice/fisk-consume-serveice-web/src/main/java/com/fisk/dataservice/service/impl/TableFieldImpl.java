package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.entity.TableFieldPO;
import com.fisk.dataservice.map.TableFieldMap;
import com.fisk.dataservice.mapper.TableFieldMapper;
import com.fisk.dataservice.service.ITableField;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class TableFieldImpl
        extends ServiceImpl<TableFieldMapper, TableFieldPO>
        implements ITableField {

    @Resource
    TableFieldMapper mapper;

    @Override
    public ResultEnum addTableServiceField(Integer tableServiceId, List<TableFieldDTO> fieldDTOList) {
        fieldDTOList.forEach(e -> {
            e.setTableServiceId(tableServiceId);
        });

        List<TableFieldPO> poList = TableFieldMap.INSTANCES.dtoListToPoList(fieldDTOList);

        if (!this.saveBatch(poList)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delTableServiceField(Integer tableServiceId) {
        QueryWrapper<TableFieldPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableFieldPO::getTableServiceId, tableServiceId);
        List<TableFieldPO> poList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return ResultEnum.SUCCESS;
        }

        if (!this.remove(queryWrapper)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;

    }

    public ResultEnum tableServiceSaveConfig(Integer tableServiceId, List<TableFieldDTO> fieldDTOList) {
        delTableServiceField(tableServiceId);
        addTableServiceField(tableServiceId, fieldDTOList);
        return ResultEnum.SUCCESS;
    }

}
