package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.access.DeltaTimeDTO;
import com.fisk.mdm.entity.SystemVariablesPO;
import com.fisk.mdm.enums.DeltaTimeParameterTypeEnum;
import com.fisk.mdm.enums.SystemVariableTypeEnum;
import com.fisk.mdm.mapper.SystemVariablesMapper;
import com.fisk.mdm.service.ISystemVariables;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjian
 */
@Service
public class SystemVariablesImpl extends ServiceImpl<SystemVariablesMapper, SystemVariablesPO>
        implements ISystemVariables {

    @Resource
    SystemVariablesMapper mapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addSystemVariables(Integer id, List<DeltaTimeDTO> dtoList) {

        deleteSystemVariables(id);

        List<SystemVariablesPO> systemVariablesPoList = new ArrayList<>();
        for (DeltaTimeDTO item : dtoList) {
            SystemVariablesPO po = new SystemVariablesPO();
            po.tableAccessId = id;
            po.deltaTimeParameterType = item.deltaTimeParameterTypeEnum.name();
            po.systemVariableType = item.systemVariableTypeEnum.name();
            po.variableValue = item.variableValue;
            systemVariablesPoList.add(po);
        }

        return this.saveBatch(systemVariablesPoList) ? ResultEnum.SUCCESS : ResultEnum.DATA_SUBMIT_ERROR;
    }

    @Override
    public List<DeltaTimeDTO> getSystemVariable(Integer id) {
        List<SystemVariablesPO> poList = this.query().eq("table_access_id", id).list();
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

    public void deleteSystemVariables(Integer id) {
        QueryWrapper<SystemVariablesPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SystemVariablesPO::getTableAccessId, id);
        List<SystemVariablesPO> list = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (!this.remove(queryWrapper)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

}
