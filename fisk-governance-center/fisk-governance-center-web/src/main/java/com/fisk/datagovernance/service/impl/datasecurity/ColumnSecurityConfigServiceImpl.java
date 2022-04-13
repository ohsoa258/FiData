package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigUserAssignmentDTO;
import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigValidDTO;
import com.fisk.datagovernance.entity.datasecurity.ColumnSecurityConfigPO;
import com.fisk.datagovernance.map.datasecurity.ColumnSecurityConfigMap;
import com.fisk.datagovernance.mapper.datasecurity.ColumnSecurityConfigMapper;
import com.fisk.datagovernance.service.datasecurity.ColumnSecurityConfigService;
import org.junit.Test;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class ColumnSecurityConfigServiceImpl
        extends ServiceImpl<ColumnSecurityConfigMapper, ColumnSecurityConfigPO>
        implements ColumnSecurityConfigService {

    @Resource
    ColumnSecurityConfigMapper mapper;
    @Resource
    ColumnUserAssignmentServiceImpl columnUserAssignmentService;
    @Resource
    UserHelper userHelper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum saveColumnSecurityConfig(ColumnSecurityConfigUserAssignmentDTO dto) {
        //判断权限名称是否重复
        QueryWrapper<ColumnSecurityConfigPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(ColumnSecurityConfigPO::getTableId,dto.tableId)
                .eq(ColumnSecurityConfigPO::getPermissionsName,dto.permissionsName);
        ColumnSecurityConfigPO po=mapper.selectOne(queryWrapper);
        if (po!=null)
        {
            return ResultEnum.NAME_EXISTS;
        }
        dto.createUser=userHelper.getLoginUserInfo().id.toString();
        if (mapper.insertColumnSecurityConfig(dto) == 0)
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return columnUserAssignmentService.saveColumnUserAssignment(dto.id,dto.assignmentDtoList);
    }

    @Override
    public ColumnSecurityConfigUserAssignmentDTO getData(long id) {
        ColumnSecurityConfigPO po=mapper.selectById(id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        ColumnSecurityConfigUserAssignmentDTO data = ColumnSecurityConfigMap.INSTANCES.poToAssignmentDto(po);
        data.assignmentDtoList=columnUserAssignmentService.listColumnUserAssignment(po.id);
        return data;
    }

    @Override
    public List<ColumnSecurityConfigUserAssignmentDTO> listColumnSecurityConfig(String tableId)
    {
        QueryWrapper<ColumnSecurityConfigPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("id").lambda().eq(ColumnSecurityConfigPO::getTableId,tableId)
                .orderByDesc(ColumnSecurityConfigPO::getCreateTime);
        List<Integer> poIds=(List)mapper.selectObjs(queryWrapper);
        return poIds.stream().map(e->this.getData(e)).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(ColumnSecurityConfigUserAssignmentDTO dto)
    {
        ColumnSecurityConfigPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //判断权限名称是否重复
        QueryWrapper<ColumnSecurityConfigPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(ColumnSecurityConfigPO::getTableId,dto.tableId)
                .eq(ColumnSecurityConfigPO::getPermissionsName,dto.permissionsName);
        ColumnSecurityConfigPO model=mapper.selectOne(queryWrapper);
        if (model!=null && model.id!=dto.id)
        {
            return ResultEnum.NAME_EXISTS;
        }
        po=ColumnSecurityConfigMap.INSTANCES.dtoToPo(dto);
        if (mapper.updateById(po)==0)
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return columnUserAssignmentService.saveColumnUserAssignment(po.id,dto.assignmentDtoList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum deleteData(long id)
    {
        ColumnSecurityConfigPO po=mapper.selectById(id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        if (mapper.deleteByIdWithFill(po)==0)
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return columnUserAssignmentService.saveColumnUserAssignment(id,null);
    }

    @Override
    public ResultEnum updateValid(ColumnSecurityConfigValidDTO dto)
    {
        ColumnSecurityConfigPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        po.valid=dto.valid;
        return mapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }


}