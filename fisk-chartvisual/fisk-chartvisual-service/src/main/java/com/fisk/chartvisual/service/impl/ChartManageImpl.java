package com.fisk.chartvisual.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.*;
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
import com.fisk.chartvisual.service.IChartManageService;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gy
 */
@Service
@Slf4j
public class ChartManageImpl implements IChartManageService {

    @Resource
    ChartMapper chartMapper;
    @Resource
    DraftChartMapper draftChartMapper;
    @Resource
    UserHelper userHelper;
    @Resource
    FolderMapper folderMapper;
    @Resource
    ChartChildvisualMapper childvisualMapper;

    @Override
    public ResultEnum saveChartToDraft(ChartPropertyDTO dto) {
                DraftChartPO model = DraftChartMap.INSTANCES.dtoToPo(dto);
                return draftChartMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;

    }

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

        ChartPO model = ChartMap.INSTANCES.dtoToPo(dto);

        int res = chartMapper.insert(model);
        if (res == 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        List<ChildvisualDTO> poList = this.stringSplit(dto.content, (int) model.getId());
        if (!CollectionUtils.isEmpty(poList)){
            for (ChildvisualDTO po : poList) {
                ChartChildvisualPO childvisualPO = ChartMap.INSTANCES.dtoToPo(po);
                int insert = childvisualMapper.insert(childvisualPO);
                if (insert == 0) {
                    return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
                }
            }
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, model.id);
    }

    /**
     * json字符串拆分
     * @param context
     * @return
     */
    public List<ChildvisualDTO> stringSplit(String context,Integer chartId){
        List<ChildvisualDTO> dtoList = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(context);
        JSONArray jsonArray = jsonObject.getJSONArray("listChar");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            String backgroundImage = jsonObject1.getString("componentBackground");

            ChildvisualDTO dto = new ChildvisualDTO();
            dto.setDelFlag(1);
            dto.setChartId(chartId);
            dto.setContent(jsonObject1.toJSONString());
            if (!StringUtils.isEmpty(backgroundImage)){
                dto.setComponentBackground(backgroundImage);
                jsonObject1.remove("componentBackground");
                dto.setContent(jsonObject1.toJSONString());
            }
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public ChartPropertyVO getDataById(int id, ChartQueryTypeEnum type) {
        switch (type) {
            case DRAFT:
                DraftChartPO po=  draftChartMapper.selectById(id);
                return DraftChartMap.INSTANCES.poToVo(po);
            case RELEASE:
                return ChartMap.INSTANCES.poToVo(chartMapper.selectById((id)));
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    @Override
    public ResultEnum updateChart(ChartPropertyEditDTO dto) {
        int res = 0;
        switch (dto.type) {
            case DRAFT:
                DraftChartPO draft = draftChartMapper.selectById(dto.id);
                if (draft == null) {
                    return ResultEnum.DATA_NOTEXISTS;
                }
                ChartMap.INSTANCES.editDtoToPo(dto, draft);
                res = draftChartMapper.updateById(draft);
                break;
            case RELEASE:
                ChartPO release = chartMapper.selectById(dto.id);
                if (release == null) {
                    return ResultEnum.DATA_NOTEXISTS;
                }
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
                DraftChartPO draft = draftChartMapper.selectById(id);
                if (draft == null) {
                    return ResultEnum.DATA_NOTEXISTS;
                }
                res = draftChartMapper.deleteByIdWithFill(draft);
                break;
            case RELEASE:
                ChartPO chart = chartMapper.selectById(id);
                if (chart == null) {
                    return ResultEnum.DATA_NOTEXISTS;
                }
                res = chartMapper.deleteByIdWithFill(chart);
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<ChartPropertyVO> listData(Page<ChartPropertyVO> page, ChartQueryDTO query) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        query.id = userInfo.id;
        return chartMapper.listChartDataByUserId(page, query);
    }
}
