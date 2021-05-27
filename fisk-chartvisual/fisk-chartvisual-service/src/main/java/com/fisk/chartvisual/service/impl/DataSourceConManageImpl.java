package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.dto.DataSourceConQuery;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.map.DataSourceConMap;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IDataSourceConManage;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.constants.SqlConstants;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 数据源管理实现类
 *
 * @author gy
 */
@Service
public class DataSourceConManageImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataSourceConManage {

    //TODO: 未获取登录人信息

    @Resource
    DataSourceConMapper mapper;

    @Override
    public Page<DataSourceConVO> listDataSourceCons(Page<DataSourceConVO> page, DataSourceConQuery query) {
        return mapper.listDataSourceConByUserId(page, query);
    }

    @Override
    public ResultEnum saveDataSourceCon(DataSourceConDTO dto) {
        DataSourceConPO model = DataSourceConMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateDataSourceCon(DataSourceConEditDTO dto) {
        DataSourceConPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        DataSourceConMap.INSTANCES.editDtoToPo(dto, model);
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataSourceCon(int id) {
        DataSourceConPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        model.delFlag = Integer.parseInt(SqlConstants.DEL);
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
