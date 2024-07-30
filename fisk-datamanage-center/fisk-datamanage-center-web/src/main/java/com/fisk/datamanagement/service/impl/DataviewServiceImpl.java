package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.entity.DataviewFilterPO;
import com.fisk.datamanagement.entity.DataviewPO;
import com.fisk.datamanagement.map.DataviewMap;
import com.fisk.datamanagement.mapper.DataviewFilterMapper;
import com.fisk.datamanagement.mapper.DataviewMapper;
import com.fisk.datamanagement.service.DataviewService;
import com.fisk.datamanagement.dto.dataview.DataViewAddDTO;
import com.fisk.datamanagement.dto.dataview.DataViewDTO;
import com.fisk.datamanagement.dto.dataview.DataViewEditDTO;
import com.fisk.datamanagement.enums.serverModuleTypeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.fisk.common.core.response.ResultEnum.DELETE_ERROR;
import static com.fisk.datamanagement.utils.paging.PagingUtils.startPage;

/**
 * @Author: wangjian
 * @Date: 2024-06-18
 * @Description:
 */
@Service
public class DataviewServiceImpl implements DataviewService {

    @Resource
    DataviewMapper dataviewMapper;
    @Resource
    DataviewFilterMapper dataviewFilterMapper;

    @Override
    public Page<DataViewDTO> queryAll(Integer currentPage, Integer pageSize, serverModuleTypeEnum type, String filterName) {
        QueryWrapper<DataviewPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .orderByDesc(DataviewPO::getCreateTime);

        List<DataViewDTO> dtoList = new ArrayList<>();
        // 根据当前登录人筛选数据
        QueryWrapper<DataviewPO> queryView = new QueryWrapper<>();
        queryView.lambda().eq(DataviewPO::getViewName,filterName);
        if (type == null){
            queryView.lambda().eq(DataviewPO::getViewType,"1");
        }else {
            queryView.lambda()
                    .eq(DataviewPO::getServerModule,type.getValue());
        }

        // 查询filter条件
        List<DataviewPO> viewFilterList = dataviewMapper.selectList(queryView);
        if (CollectionUtils.isNotEmpty(viewFilterList)){
            viewFilterList.stream().forEach(e -> {
                DataViewDTO dto = DataviewMap.INSTANCES.poToDto(e);
                QueryWrapper<DataviewFilterPO> query = new QueryWrapper<>();
                queryWrapper.lambda()
                        .orderByDesc(DataviewPO::getCreateTime);
                query.lambda().eq(DataviewFilterPO::getDataviewId,e.getId());
                List<DataviewFilterPO> filterList = dataviewFilterMapper.selectList(query);
                if (CollectionUtils.isNotEmpty(filterList)){
                    dto.setFilterList(DataviewMap.INSTANCES.filterPoToDto(filterList));
                }
                dtoList.add(dto);
            });
        }

        return this.paging(dtoList,currentPage,pageSize);
    }

    /**
     * 分页
     * @param dtoList
     * @param currentPage
     * @param pageSize
     */
    public Page<DataViewDTO> paging(List<DataViewDTO> dtoList,Integer currentPage,Integer pageSize){
        // 总条数
        int total = dtoList.size();

        Page<DataViewDTO> dtoPage = new Page<>();
        dtoPage.setRecords(startPage(dtoList, currentPage, pageSize));
        dtoPage.setCurrent(currentPage);
        dtoPage.setSize(pageSize);
        dtoPage.setTotal(total);
        return dtoPage;
    }

    @Override
    public ResultEnum saveView(DataViewAddDTO dto) {
        if (dto == null){
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 判断是否已经存在
        QueryWrapper<DataviewPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(DataviewPO::getViewName, dto.getViewName())
                .last("limit 1");
        DataviewPO po = dataviewMapper.selectOne(query);
        if (po != null) {
            return ResultEnum.DATA_EXISTS;
        }

        // 不存在进行保存
        DataviewPO dataviewPo = DataviewMap.INSTANCES.dtoToPo(dto);
        int insert = dataviewMapper.insert(dataviewPo);
        if (insert <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return this.saveFilter(DataviewMap.INSTANCES.filterDtoToPo(dto.getFilterDTO()), (int) dataviewPo.getId());
    }

    @Override
    public ResultEnum updateView(DataViewEditDTO dto) {
        if (dto == null){
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 修改是视图表数据
        int i = dataviewMapper.updateById(DataviewMap.INSTANCES.dtoToPo(dto));
        if (i <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 删除视图表下的过滤条件
        ResultEnum resultEnum = this.deleteFilter(dto.getId());
        if (resultEnum.getCode() == DELETE_ERROR.getCode()){
            return ResultEnum.DELETE_ERROR;
        }

        return this.saveFilter(DataviewMap.INSTANCES.filterDtoToPo(dto.getViewFilterDTOList()),dto.getId());
    }

    /**
     * 保存过滤条件
     * @param viewFilterList 过滤条件
     * @param id 数据视图id
     */
    public ResultEnum saveFilter(List<DataviewFilterPO> viewFilterList,Integer id){
        if (CollectionUtils.isNotEmpty(viewFilterList)) {
            for (DataviewFilterPO filterPo : viewFilterList) {
                filterPo.setDataviewId(id);
                int insert = dataviewFilterMapper.insert(filterPo);
                if (insert <= 0) {
                    return ResultEnum.SAVE_DATA_ERROR;
                }
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteView(Integer id) {
        if (id == null){
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 删除视图表
        int i = dataviewMapper.deleteById(id);
        if (i <= 0){
            return DELETE_ERROR;
        }

        return this.deleteFilter(id);
    }

    /**
     * 删除过滤条件
     * @param id
     * @return
     */
    public ResultEnum deleteFilter(Integer id){
        // 判断视图表下是否有过滤条件
        QueryWrapper<DataviewFilterPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(DataviewFilterPO::getDataviewId,id);
        List<DataviewFilterPO> dataViewFilterList = dataviewFilterMapper.selectList(query);
        if (CollectionUtils.isNotEmpty(dataViewFilterList)){
            // 该视图存在过滤条件
            int i1 = dataviewFilterMapper.delete(query);
            if (i1 <= 0){
                return DELETE_ERROR;
            }
        }
        return ResultEnum.SUCCESS;
    }
}
