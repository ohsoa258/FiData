package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.datasecurity.columnuserassignment.ColumnUserAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.ColumnUserAssignmentPO;
import com.fisk.datagovernance.map.datasecurity.ColumnUserAssignmentMap;
import com.fisk.datagovernance.mapper.datasecurity.ColumnUserAssignmentMapper;
import com.fisk.datagovernance.service.datasecurity.ColumnUserAssignmentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class ColumnUserAssignmentServiceImpl
        extends ServiceImpl<ColumnUserAssignmentMapper, ColumnUserAssignmentPO>
        implements ColumnUserAssignmentService {

    @Resource
    ColumnUserAssignmentMapper mapper;

    @Override
    public ResultEnum saveColumnUserAssignment(long columnSecurityId, List<ColumnUserAssignmentDTO> dtoList)
    {
        //先删除之前配置
        QueryWrapper<ColumnUserAssignmentPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(ColumnUserAssignmentPO::getColumnSecurityId,columnSecurityId);
        List<ColumnUserAssignmentPO> poList=mapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(poList))
        {
            if (!this.remove(queryWrapper))
            {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        dtoList.stream().map(e->e.columnSecurityId=columnSecurityId).collect(Collectors.toList());;
        List<ColumnUserAssignmentPO> data = ColumnUserAssignmentMap.INSTANCES.listDtoToListPo(dtoList);
        if (!this.saveBatch(data))
        {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<ColumnUserAssignmentDTO> listColumnUserAssignment(long columnSecurityId)
    {
        QueryWrapper<ColumnUserAssignmentPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(ColumnUserAssignmentPO::getColumnSecurityId,columnSecurityId);
        return ColumnUserAssignmentMap.INSTANCES.poListToDto(mapper.selectList(queryWrapper));
    }


}