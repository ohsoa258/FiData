package com.fisk.dataservice.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.dataservice.dto.dataanalysisview.*;
import com.fisk.dataservice.entity.ViewFieldsPO;
import com.fisk.dataservice.mapper.DataViewFieldsMapper;
import com.fisk.dataservice.service.IDataViewFieldsService;
import com.fisk.dataservice.service.IDataViewService;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Service
@Slf4j
public class DataViewFieldsServiceImpl
        extends ServiceImpl<DataViewFieldsMapper, ViewFieldsPO>
        implements IDataViewFieldsService {

    @Resource
    private IDataViewService dataViewService;


    @Override
    @Transactional
    public void saveViewFields(SaveDataViewDTO dto, Integer dataViewId, DataSourceDTO dsDto) {
        // 获取sql执行结果数据
        SelSqlResultDTO selDto = new SelSqlResultDTO();
        selDto.setViewName(dto.getName());
        selDto.setQuerySql(dto.getViewScript());
        selDto.setTargetDbId(dto.getTargetDbId());
        selDto.setViewThemeId(dto.getViewThemeId());
        selDto.setDataSourceTypeEnum(dsDto.conType.getName());
        OdsResultDTO resultDTO = dataViewService.getDataAccessQueryList(selDto);
        log.info("字段数据集,[{}]", JSON.toJSONString(resultDTO));
        if (Objects.isNull(resultDTO)){
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据视图字段信息查询失败");
        }

        List<FieldNameDTO> fieldList = resultDTO.getFieldNameDTOList();
        if (CollectionUtils.isEmpty(fieldList)){
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据视图字段信息查询失败");
        }
        List<ViewFieldsPO> poList = new ArrayList<>();

        for (FieldNameDTO item : fieldList){
            ViewFieldsPO model = new ViewFieldsPO();
            model.setViewId(dataViewId);
            model.setFieldName(item.getFieldName());
            model.setShowName(item.getFieldName());
            model.setFieldDesc(item.getFieldDes());
            model.setFieldPrecision(item.getFieldPrecision());
            model.setFieldLength(item.getFieldLength());
            model.setFieldType(item.getFieldType());
            model.setDataType(item.getFieldType());
            poList.add(model);
        }

        boolean flag = this.saveBatch(poList);
        if (!flag){
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据视图字段信息保存失败");
        }

    }
}
