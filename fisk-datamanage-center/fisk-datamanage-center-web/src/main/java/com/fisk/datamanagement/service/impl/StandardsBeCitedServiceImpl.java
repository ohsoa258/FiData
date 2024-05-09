package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.entity.StandardsBeCitedPO;
import com.fisk.datamanagement.mapper.StandardsBeCitedMapper;
import com.fisk.datamanagement.service.StandardsBeCitedService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service("standardsBeCitedService")
public class StandardsBeCitedServiceImpl extends ServiceImpl<StandardsBeCitedMapper, StandardsBeCitedPO> implements StandardsBeCitedService {


    /**
     * 检查标准是否被引用
     *
     * @param standardsId 标准的ID
     * @param dbId 数据库的ID
     * @param tableId 表的ID
     * @param fieldId 字段的ID
     * @return 返回检查结果，如果指定标准ID未被引用则返回SUCCESS，如果被引用则返回CHECK_STANDARD_BE_CITED_EXITS
     */
    @Override
    public ResultEnum checkStandardBeCited(Integer standardsId, Integer dbId, Integer tableId, Integer fieldId) {
        // 查询当前数据库、表、字段下所有被引用的标准ID
        List<Integer> standardsIds = this.baseMapper.checkStandardBeCited(dbId, tableId, fieldId);
        // 过滤掉指定的标准ID，只保留其他被引用的标准ID
        List<Integer> ids = standardsIds.stream().filter(id -> !id.equals(standardsId)).collect(Collectors.toList());
        // 如果过滤后的列表为空，表示指定标准未被引用，返回SUCCESS；否则，返回CHECK_STANDARD_BE_CITED_EXITS
        if (CollectionUtils.isEmpty(ids)) {
            return ResultEnum.SUCCESS;
        }else {
            return ResultEnum.CHECK_STANDARD_BE_CITED_EXITS;
        }
    }

}
