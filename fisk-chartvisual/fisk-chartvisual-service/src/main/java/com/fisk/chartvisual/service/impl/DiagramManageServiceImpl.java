package com.fisk.chartvisual.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.chartvisual.dto.ChartPropertyEditDTO;
import com.fisk.chartvisual.dto.ChildvisualDTO;
import com.fisk.chartvisual.dto.ReleaseChart;
import com.fisk.chartvisual.entity.ChartChildvisualPO;
import com.fisk.chartvisual.entity.ChartOptionPO;
import com.fisk.chartvisual.entity.ChartPO;
import com.fisk.chartvisual.entity.DraftChartPO;
import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import com.fisk.chartvisual.map.ChartMap;
import com.fisk.chartvisual.map.DraftChartMap;
import com.fisk.chartvisual.mapper.ChartMapper;
import com.fisk.chartvisual.mapper.ChartOptionMapper;
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
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Resource
    ChartOptionMapper optionMapper;

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

        // 校验同级目录报表名称是否重复
        boolean name = this.checkRepeatName(dto.name, dto.fid);
        if (name == true){
            return ResultEntityBuild.build(ResultEnum.NAME_EXISTS);
        }

        ChartPO model = ChartMap.INSTANCES.chartDtoToPo(dto);

        int res = chartMapper.insert(model);
        if (res == 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        List<ChildvisualDTO> dtoList = this.stringInterception(dto.content, Integer.parseInt(String.valueOf(model.getId())));
        dtoList.stream().filter(Objects::nonNull).forEach(e -> {
            ChartOptionPO optionPo = ChartMap.INSTANCES.dtoToOptionPo(e);
            int insert = optionMapper.insert(optionPo);
            if (insert == 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        });

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, model.id);
    }

    /**
     * 校验同级目录报表名称是否重复
     * @param name
     * @return
     */
    public boolean checkRepeatName(String name,Long id){
        QueryWrapper<ChartPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ChartPO::getName,name)
                .eq(ChartPO::getFid,id)
                .last("limit 1");
        ChartPO po = chartMapper.selectOne(queryWrapper);
        if (po != null){
            return true;
        }

        return false;
    }

    /**
     * 组件拆分
     * @param context
     */
    public List<ChildvisualDTO> stringInterception(String context,Integer chartId){
        List<ChildvisualDTO> dtoList = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(context);
        JSONArray jsonArray = jsonObject.getJSONArray("listChar");

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);

            // 保存到子表
            ChildvisualDTO dto = new ChildvisualDTO();
            dto.setDelFlag(1);
            dto.setChartId(chartId);
            dto.setContent(jsonObject1.toString());
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
     * @return
     */
    public String assemblySplicing(Integer id){
        StringBuilder str = new StringBuilder();
        ChartPO chartPo = chartMapper.selectById(id);
        str.append("{\"listChar\":[");

        // 查询子表的json
        QueryWrapper<ChartOptionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ChartOptionPO::getChartId,id);
        List<ChartOptionPO> optionPoList = optionMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(optionPoList)){
            String collect = optionPoList.stream().map(e -> {
                return e.getContent();
            }).collect(Collectors.joining(","));
            str.append(collect);
        }

        str.append("],");
        String substring = chartPo.getContent().substring(1, chartPo.getContent().length() - 1);
        str.append(substring);
        str.append("}");
        return str.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum updateChart(ChartPropertyEditDTO dto) {
        int res = 0;
        switch (dto.type) {
            case DRAFT:
                DraftChartPO draft = draftChartMapper.selectById(dto.id);
                if (draft == null) {
                    return ResultEnum.DATA_NOTEXISTS;
                }
                ChartMap.INSTANCES.editDiagDtoToPo(dto, draft);
                res = draftChartMapper.updateById(draft);
                break;
            case RELEASE:
                ChartPO release = chartMapper.selectById(dto.id);
                if (release == null) {
                    return ResultEnum.DATA_NOTEXISTS;
                }
                ChartMap.INSTANCES.editDiagDtoToPo(dto, release);
                res = chartMapper.updateById(release);

                if (!StringUtils.isEmpty(dto.content)){
                    // 删除报表组件
                    QueryWrapper<ChartOptionPO> query = new QueryWrapper<>();
                    query.lambda()
                            .eq(ChartOptionPO::getChartId,dto.id)
                            .eq(ChartOptionPO::getDelFlag,1);
                    int delete = optionMapper.delete(query);
                    if (delete <= 0){
                        return ResultEnum.SAVE_DATA_ERROR;
                    }

                    // 保存报表组件
                    List<ChildvisualDTO> dtoList = this.stringInterception(dto.content, Integer.parseInt(String.valueOf(dto.getId())));
                    dtoList.stream().filter(Objects::nonNull).forEach(e -> {
                        ChartOptionPO optionPo = ChartMap.INSTANCES.dtoToOptionPo(e);
                        int insert = optionMapper.insert(optionPo);
                        if (insert == 0) {
                            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                        }
                    });
                }
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }

        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
