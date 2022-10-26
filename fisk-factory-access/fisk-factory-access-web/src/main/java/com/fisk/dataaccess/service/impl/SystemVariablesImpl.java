package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.entity.SystemVariablesPO;
import com.fisk.dataaccess.map.SystemVariablesMap;
import com.fisk.dataaccess.mapper.SystemVariablesMapper;
import com.fisk.dataaccess.service.ISystemVariables;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class SystemVariablesImpl extends ServiceImpl<SystemVariablesMapper, SystemVariablesPO>
        implements ISystemVariables {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addSystemVariables(Long tableAccessId, List<DeltaTimeDTO> dtoList) {

        deleteSystemVariables(tableAccessId);

        List<SystemVariablesPO> systemVariablesPoList = SystemVariablesMap.INSTANCES.dtoListToPoList(dtoList);
        systemVariablesPoList.stream().map(e -> e.tableAccessId = tableAccessId).collect(Collectors.toList());

        return this.saveBatch(systemVariablesPoList) == true ? ResultEnum.SUCCESS : ResultEnum.DATA_SUBMIT_ERROR;
    }

    @Override
    public List<DeltaTimeDTO> getSystemVariable(Long tableAccessId) {
        List<SystemVariablesPO> poList = this.query().eq("table_access_id", tableAccessId).list();
        return SystemVariablesMap.INSTANCES.poListToDtoList(poList);
    }

    public void deleteSystemVariables(Long tableAccessId) {
        QueryChainWrapper<SystemVariablesPO> data = this.query().eq("table_access_id", tableAccessId);

        if (!this.remove(data)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

}
