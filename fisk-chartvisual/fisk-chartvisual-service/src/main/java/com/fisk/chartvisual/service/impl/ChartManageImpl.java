package com.fisk.chartvisual.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.google.inject.internal.cglib.proxy.$FixedValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fisk.chartvisual.util.dbhelper.Base64.ByteCodeUtils.byteConvertStringFun;

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
            JSONObject charProPerty = jsonObject1.getJSONObject("charProPerty");

            ChildvisualDTO dto = new ChildvisualDTO();
            dto.setDelFlag(1);
            dto.setChartId(chartId);
            dto.setContent(jsonObject1.toJSONString());

            // 删除组件背景图
            if (charProPerty != null){
                JSONObject chart = charProPerty.getJSONObject("chart");
                if (chart != null){
                    String backgroundImage = chart.getString("componentBackground");
                    if (!StringUtils.isEmpty(backgroundImage)){
                        dto.setComponentBackground(backgroundImage);
                        chart.remove("componentBackground");
                        dto.setContent(jsonObject1.toJSONString());
                    }
                }

                JSONObject layoutPanelData = charProPerty.getJSONObject("layoutPanelData");
                if (layoutPanelData != null){
                    String backgroundImage = layoutPanelData.getString("laycomponentbackground");
                    if (!StringUtils.isEmpty(backgroundImage)){
                        dto.setLayComponentBackground(backgroundImage);
                        layoutPanelData.remove("laycomponentbackground");
                        dto.setContent(jsonObject1.toJSONString());
                    }
                }
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
                ChartPO chartPO = chartMapper.selectById((id));
                String content = this.assemblySplicing(id);
                chartPO.setContent(content);
                return ChartMap.INSTANCES.poToVo(chartPO);
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * 报表组件拼接
     * @param id
     */
    public String assemblySplicing(Integer id){
        QueryWrapper<ChartChildvisualPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(ChartChildvisualPO::getChartId,id)
                .eq(ChartChildvisualPO::getDelFlag,1);
        List<ChartChildvisualPO> childvisualList = childvisualMapper.selectList(query);
        if (!CollectionUtils.isEmpty(childvisualList)){
            String noImage = childvisualList.stream().filter(e -> e.getComponentBackground() == null && e.getLayComponentBackground() == null)
                    .map(e -> e.getContent())
                    .collect(Collectors.joining(","));

            String image = childvisualList.stream().filter(e -> e.getComponentBackground() != null || e.getLayComponentBackground() != null)
                    .map(e -> {
                        StringBuilder context = new StringBuilder(e.getContent());
                        if (e.getComponentBackground() != null){
                            String componentBackground = "\"componentBackground\"" + ":"+ "\"" +
                                    "data:image/jpg;base64," + byteConvertStringFun(e.getComponentBackground()) + "\"" + ",";
                            context.insert(context.indexOf("\"chart\":")+9,componentBackground);
                        }else if (e.getLayComponentBackground() != null){
                            String componentBackground = "\"laycomponentbackground\"" + ":"+ "\"" +
                                    "data:image/jpg;base64," + byteConvertStringFun(e.getLayComponentBackground()) + "\"" + ",";
                            context.insert(context.indexOf("\"layoutPanelData\":") + 19,componentBackground);
                        }
                        return context;
                    }).collect(Collectors.joining(","));

            return this.chartSplicing(id,noImage,image);
        }

        return null;
    }


    /**
     * 报表拼接组件json
     * @param id
     * @param noImage
     * @param image
     */
    public String chartSplicing(Integer id,String noImage,String image){
        ChartPO chartPO = chartMapper.selectById(id);
        String context = chartPO.getContent();
        String chartContent = context.substring(1, context.length() - 1);
        StringBuilder str = new StringBuilder();
        str.append("{\"listChar\":[");
        if (!StringUtils.isEmpty(noImage)){
            str.append(noImage);
        }

        if (!StringUtils.isEmpty(noImage) && !StringUtils.isEmpty(image)){
            str.append(",");
        }

        if (!StringUtils.isEmpty(image)){
            str.append(image);
        }else {
            if (StringUtils.isEmpty(image)){
                str.append("],");
                str.append(chartContent);
                str.append("}");
            }
        }

        if (StringUtils.isEmpty(noImage)){
            str.append("],");
            str.append(chartContent);
            str.append("}");
        }

        if (!StringUtils.isEmpty(noImage) && !StringUtils.isEmpty(image)){
            str.append("],");
            str.append(chartContent);
            str.append("}");
        }

        return str.toString();
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

                if (!StringUtils.isEmpty(dto.content)){
                    // 删除报表组件
                    QueryWrapper<ChartChildvisualPO> query = new QueryWrapper<>();
                    query.lambda()
                            .eq(ChartChildvisualPO::getChartId,dto.id)
                            .eq(ChartChildvisualPO::getDelFlag,1);
                    int delete = childvisualMapper.delete(query);
                    if (delete <= 0){
                        return ResultEnum.SAVE_DATA_ERROR;
                    }

                    // 保存报表组件
                    List<ChildvisualDTO> poList = this.stringSplit(dto.content, dto.id);
                    if (!CollectionUtils.isEmpty(poList)){
                        for (ChildvisualDTO po : poList) {
                            ChartChildvisualPO childvisualPO = ChartMap.INSTANCES.dtoToPo(po);
                            int insert = childvisualMapper.insert(childvisualPO);
                            if (insert == 0) {
                                return ResultEnum.SAVE_DATA_ERROR;
                            }
                        }
                    }
                }
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

                // 删除报表组件
                QueryWrapper<ChartChildvisualPO> query = new QueryWrapper<>();
                query.lambda()
                        .eq(ChartChildvisualPO::getChartId,id)
                        .eq(ChartChildvisualPO::getDelFlag,1);
                int delete = childvisualMapper.delete(query);
                if (delete <= 0){
                    return ResultEnum.SAVE_DATA_ERROR;
                }
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
