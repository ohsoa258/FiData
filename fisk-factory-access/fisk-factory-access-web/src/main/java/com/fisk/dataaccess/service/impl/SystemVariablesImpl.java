package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.entity.SystemVariablesPO;
import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.dataaccess.mapper.SystemVariablesMapper;
import com.fisk.dataaccess.service.ISystemVariables;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class SystemVariablesImpl extends ServiceImpl<SystemVariablesMapper, SystemVariablesPO>
        implements ISystemVariables {

    @Resource
    SystemVariablesMapper mapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addSystemVariables(Long tableAccessId, List<DeltaTimeDTO> dtoList) {

        deleteSystemVariables(tableAccessId);

        List<SystemVariablesPO> systemVariablesPoList = new ArrayList<>();
        for (DeltaTimeDTO item : dtoList) {
            SystemVariablesPO po = new SystemVariablesPO();
            po.tableAccessId = tableAccessId;
            po.deltaTimeParameterType = item.deltaTimeParameterTypeEnum.name();
            po.systemVariableType = item.systemVariableTypeEnum.name();
            po.variableValue = item.variableValue;
            systemVariablesPoList.add(po);
        }

        return this.saveBatch(systemVariablesPoList) == true ? ResultEnum.SUCCESS : ResultEnum.DATA_SUBMIT_ERROR;
    }

    @Override
    public List<DeltaTimeDTO> getSystemVariable(Long tableAccessId) {
        List<SystemVariablesPO> poList = this.query().eq("table_access_id", tableAccessId).list();
        if (CollectionUtils.isEmpty(poList)) {
            return new ArrayList<>();
        }
        List<DeltaTimeDTO> data = new ArrayList<>();
        for (SystemVariablesPO po : poList) {
            DeltaTimeDTO dto = new DeltaTimeDTO();
            dto.variableValue = po.variableValue;
            dto.systemVariableTypeEnum = SystemVariableTypeEnum.getName(po.systemVariableType);
            dto.deltaTimeParameterTypeEnum = DeltaTimeParameterTypeEnum.getName(po.deltaTimeParameterType);
            data.add(dto);
        }
        return data;
    }

    public void deleteSystemVariables(Long tableAccessId) {
        QueryWrapper<SystemVariablesPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SystemVariablesPO::getTableAccessId, tableAccessId);
        List<SystemVariablesPO> list = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (!this.remove(queryWrapper)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

}
