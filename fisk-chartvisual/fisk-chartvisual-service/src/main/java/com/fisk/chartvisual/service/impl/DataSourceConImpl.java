package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.map.DataSourceConMap;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IDataSourceCon;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.constants.SqlConstants;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 数据源管理实现类
 *
 * @author gy
 */
@Service
public class DataSourceConImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataSourceCon {

    @Resource
    DataSourceConMapper mapper;

    @Override
    public List<DataSourceConVO> listDataSourceCons() {
        return mapper.listDataSourceConByUserId();
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
