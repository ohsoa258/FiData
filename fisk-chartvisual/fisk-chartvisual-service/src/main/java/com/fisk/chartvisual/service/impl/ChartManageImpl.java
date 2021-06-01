package com.fisk.chartvisual.service.impl;

import com.fisk.chartvisual.dto.ChartPropertyDTO;
import com.fisk.chartvisual.dto.ReleaseChart;
import com.fisk.chartvisual.entity.ChartPO;
import com.fisk.chartvisual.entity.DraftChartPO;
import com.fisk.chartvisual.map.ChartMap;
import com.fisk.chartvisual.map.DraftChartMap;
import com.fisk.chartvisual.mapper.ChartMapper;
import com.fisk.chartvisual.mapper.DraftChartMapper;
import com.fisk.chartvisual.service.IChartManage;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author gy
 */
@Service
public class ChartManageImpl implements IChartManage {

    @Resource
    ChartMapper chartMapper;
    @Resource
    DraftChartMapper draftChartMapper;

    //TODO: 登录人

    @Override
    public ResultEnum saveChartToDraft(ChartPropertyDTO dto) {
        DraftChartPO model = DraftChartMap.INSTANCES.dtoToPo(dto);
        return draftChartMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional
    @Override
    public ResultEnum saveChart(ReleaseChart dto) {
        ChartPO model = new ChartPO();
        DraftChartPO draftModel = null;
        //判断是不是发布草稿
        if (dto.draftId != null) {
            draftModel = draftChartMapper.selectById(dto.draftId);
            if (draftModel == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            ChartMap.INSTANCES.draftToRelease(draftModel, model);
        } else {
            model = ChartMap.INSTANCES.dtoToPo(dto);
        }

        int res = chartMapper.insert(model);
        if (res == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        if (draftModel != null) {
            if (draftChartMapper.deleteById(draftModel.id) == 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        return ResultEnum.SUCCESS;
    }
}
