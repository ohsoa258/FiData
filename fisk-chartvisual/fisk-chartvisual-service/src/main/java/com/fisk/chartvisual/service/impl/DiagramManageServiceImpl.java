package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.chartvisual.dto.ChildvisualDTO;
import com.fisk.chartvisual.dto.ReleaseChart;
import com.fisk.chartvisual.entity.ChartChildvisualPO;
import com.fisk.chartvisual.entity.ChartPO;
import com.fisk.chartvisual.entity.DraftChartPO;
import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import com.fisk.chartvisual.map.ChartMap;
import com.fisk.chartvisual.map.DraftChartMap;
import com.fisk.chartvisual.mapper.ChartChildvisualMapper;
import com.fisk.chartvisual.mapper.ChartMapper;
import com.fisk.chartvisual.mapper.DraftChartMapper;
import com.fisk.chartvisual.mapper.FolderMapper;
import com.fisk.chartvisual.service.DiagramManageService;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.fisk.chartvisual.service.impl.ChartManageImpl.stringSplit;

/**
 * @author WangYan
 * @date 2022/2/21 11:43
 */
@Service
public class DiagramManageServiceImpl implements DiagramManageService {

    @Resource
    FolderMapper folderMapper;
    @Resource
    DraftChartMapper draftChartMapper;
    @Resource
    ChartMapper chartMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<Long> saveChart(ReleaseChart dto) {
        //判断是不是发布草稿
        if (dto.draftId != null) {
            DraftChartPO draftModel = draftChartMapper.selectById(dto.draftId);
            if (draftModel == null) {
                return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS, "草稿不存在");
            }
            if (draftChartMapper.deleteByIdWithFill(draftModel) == 0) {
                return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR, "草稿清除失败");
            }
        }

        if (folderMapper.selectById(dto.fid) == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS, "文件夹不存在");
        }

        ChartPO model = ChartMap.INSTANCES.chartDtoToPo(dto);

        int res = chartMapper.insert(model);
        if (res == 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, model.id);
    }

    @Override
    public ChartPropertyVO getDataById(int id, ChartQueryTypeEnum type) {
        switch (type) {
            case DRAFT:
                DraftChartPO po=  draftChartMapper.selectById(id);
                return DraftChartMap.INSTANCES.poToVo(po);
            case RELEASE:
                ChartPO chartPO = chartMapper.selectById((id));
                return ChartMap.INSTANCES.poToVo(chartPO);
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
