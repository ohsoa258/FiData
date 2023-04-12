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
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResultEnum addTableServiceField(long tableServiceId, List<TableFieldDTO> fieldDTOList) {
        if (tableServiceId != 0) {
            fieldDTOList.forEach(e -> {
                e.setTableServiceId(Math.toIntExact(tableServiceId));
            });
        }
        List<TableFieldPO> poList = TableFieldMap.INSTANCES.dtoListToPoList(fieldDTOList);
        return saveBatch(poList) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum delTableServiceField(long tableServiceId, long tableFieldId) {
        List<TableFieldPO> poList = getTableServiceFieldList(tableServiceId, tableFieldId);
        if (CollectionUtils.isEmpty(poList)) {
            return ResultEnum.SUCCESS;
        }
        List<Long> idList = poList.stream().map(TableFieldPO::getId).collect(Collectors.toList());
        // 修改的是del_flag状态
        return removeByIds(idList) ? ResultEnum.SUCCESS : ResultEnum.DELETE_ERROR;
    }

    @Override
    public List<TableFieldDTO> getTableServiceField(long tableServiceId, long tableFieldId) {
        List<TableFieldDTO> fieldDTOList = new ArrayList<>();
        List<TableFieldPO> poList = getTableServiceFieldList(tableServiceId, tableFieldId);
        if (CollectionUtils.isNotEmpty(poList)) {
            fieldDTOList = TableFieldMap.INSTANCES.poListToDtoList(poList);
        }
        return fieldDTOList;
    }

    public List<TableFieldPO> getTableServiceFieldList(long tableServiceId, long tableFieldId) {
        QueryWrapper<TableFieldPO> queryWrapper = new QueryWrapper<>();
        if (tableServiceId != 0) {
            queryWrapper.lambda().eq(TableFieldPO::getTableServiceId, tableServiceId)
                    .eq(TableFieldPO::getDelFlag, 1);
        } else if (tableFieldId != 0) {
            queryWrapper.lambda().eq(TableFieldPO::getId, tableFieldId)
                    .eq(TableFieldPO::getDelFlag, 1);
        }
        List<TableFieldPO> poList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return new ArrayList<>();
        }
        return poList;
    }

    public ResultEnum tableServiceSaveConfig(long tableServiceId, long tableFieldId, List<TableFieldDTO> fieldDTOList) {
        delTableServiceField(tableServiceId, tableFieldId);
        addTableServiceField(tableServiceId, fieldDTOList);
        return ResultEnum.SUCCESS;
    }
}
