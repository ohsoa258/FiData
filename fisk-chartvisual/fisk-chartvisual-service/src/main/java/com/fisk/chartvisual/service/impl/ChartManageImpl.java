package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.ChartPropertyDTO;
import com.fisk.chartvisual.dto.ChartPropertyEditDTO;
import com.fisk.chartvisual.dto.ChartQuery;
import com.fisk.chartvisual.dto.ReleaseChart;
import com.fisk.chartvisual.entity.BaseChartProperty;
import com.fisk.chartvisual.entity.ChartPO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.entity.DraftChartPO;
import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import com.fisk.chartvisual.map.ChartMap;
import com.fisk.chartvisual.map.DraftChartMap;
import com.fisk.chartvisual.mapper.ChartMapper;
import com.fisk.chartvisual.mapper.DraftChartMapper;
import com.fisk.chartvisual.service.IChartManage;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.util.List;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum saveChart(ReleaseChart dto) {
        //判断是不是发布草稿
        if (dto.draftId != null) {
            DraftChartPO draftModel = getDraftChartById(dto.draftId);
            if (draftChartMapper.deleteByIdWithFill(draftModel) == 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }

        ChartPO model = ChartMap.INSTANCES.dtoToPo(dto);

        int res = chartMapper.insert(model);
        if (res == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ChartPropertyVO getDataById(int id, ChartQueryTypeEnum type) {
        switch (type) {
            case DRAFT:
                return DraftChartMap.INSTANCES.poToVo(getDraftChartById(id));
            case RELEASE:
                return ChartMap.INSTANCES.poToVo(getReleaseChartById(id));
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    @Override
    public ResultEnum updateChart(ChartPropertyEditDTO dto) {
        int res = 0;
        switch (dto.type) {
            case DRAFT:
                DraftChartPO draft = getDraftChartById(dto.id);
                ChartMap.INSTANCES.editDtoToPo(dto, draft);
                res = draftChartMapper.updateById(draft);
                break;
            case RELEASE:
                ChartPO release = getReleaseChartById(dto.id);
                ChartMap.INSTANCES.editDtoToPo(dto, release);
                res = chartMapper.updateById(release);
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }

        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataById(int id, ChartQueryTypeEnum type) {
        int res = 0;
        switch (type) {
            case DRAFT:
                res = draftChartMapper.deleteByIdWithFill(getDraftChartById(id));
                break;
            case RELEASE:
                res = chartMapper.deleteByIdWithFill(getReleaseChartById(id));
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<ChartPropertyVO> listData(Page<ChartPropertyVO> page, ChartQuery query) {
        return chartMapper.listChartDataByUserId(page, query);
    }



    /*-----------------*/

    private DraftChartPO getDraftChartById(int id) {
        DraftChartPO draft = draftChartMapper.selectById(id);
        if (draft == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return draft;
    }

    private ChartPO getReleaseChartById(int id) {
        ChartPO release = chartMapper.selectById(id);
        if (release == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return release;
    }
}
