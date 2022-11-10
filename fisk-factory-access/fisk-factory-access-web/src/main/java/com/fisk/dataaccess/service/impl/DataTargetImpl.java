package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetAddDTO;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetPageResultDTO;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetQueryDTO;
import com.fisk.dataaccess.entity.DataTargetPO;
import com.fisk.dataaccess.map.ApiOutputParameterMap;
import com.fisk.dataaccess.map.DataTargetMap;
import com.fisk.dataaccess.mapper.DataTargetMapper;
import com.fisk.dataaccess.service.IDataTarget;
import com.fisk.dataaccess.utils.filterfield.FilterFieldUtils;
import com.fisk.dataaccess.vo.output.datatarget.DataTargetVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class DataTargetImpl implements IDataTarget {

    @Resource
    UserClient client;
    @Resource
    GenerateCondition generateCondition;
    @Resource
    DataTargetMapper mapper;
    @Resource
    ApiOutputParameterImpl apiOutputParameter;
    @Resource
    UserHelper userHelper;

    @Resource
    FilterFieldUtils utils;

    @Override
    public Page<DataTargetPageResultDTO> getDataList(DataTargetQueryDTO dto) {
        String query = null;
        if (!CollectionUtils.isEmpty(dto.queryDTOList)) {
            query = generateCondition.getCondition(dto.queryDTOList);
        }
        Page<DataTargetPageResultDTO> data = mapper.queryList(dto.page, query, dto.dataTargetAppId);
        //创建人/更新人id替换为名称
        ReplenishUserInfo.replenishUserName(data.getRecords(), client, UserFieldEnum.USER_NAME);
        return data;
    }

    @Override
    public List<FilterFieldDTO> getDataTargetColumn() {
        return utils.getDataTargetColumn("tb_data_target", FilterSqlConstants.DATA_TARGET);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addDataTarget(DataTargetAddDTO dto) {
        dto.createUserId = userHelper.getLoginUserInfo().id;
        dto.createTime = LocalDateTime.now();
        Integer flat = mapper.insertDataTarget(dto);
        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return apiOutputParameter.addApiOutputParameter(Long.valueOf(dto.id), dto.parameters);
    }

    /**
     * 根据数据目标应用批量删除数据目标
     *
     * @param dataTargetAppId
     * @return
     */
    public ResultEnum deleteBatchByAppId(long dataTargetAppId) {
        QueryWrapper<DataTargetPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").lambda().eq(DataTargetPO::getDataTargetAppId, dataTargetAppId);
        List<Integer> poList = (List) mapper.selectObjs(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return ResultEnum.SUCCESS;
        }
        for (Integer id : poList) {
            this.delete(Long.valueOf(id));
        }
        return ResultEnum.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum delete(Long id) {
        DataTargetPO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        int i = mapper.deleteByIdWithFill(po);
        if (i > 0) {
            return apiOutputParameter.delApiOutputParameter(id);
        }
        return ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public DataTargetVO getDataTarget(Long id) {
        DataTargetPO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        DataTargetVO dataTargetVO = DataTargetMap.INSTANCES.poToVo(po);
        dataTargetVO.parameters = apiOutputParameter.getApiOutputParameter(id);
        return dataTargetVO;
    }

    @Override
    public ResultEnum updateDataTarget(DataTargetVO vo) {
        DataTargetPO po = mapper.selectById(vo.id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        po = DataTargetMap.INSTANCES.voToPo(vo);
        int i = mapper.updateById(po);
        if (i > 0) {
            return apiOutputParameter.addApiOutputParameter(Long.valueOf(vo.id), ApiOutputParameterMap.INSTANCES.voListToDtoList(vo.parameters));
        }
        return ResultEnum.UPDATE_DATA_ERROR;
    }

}
