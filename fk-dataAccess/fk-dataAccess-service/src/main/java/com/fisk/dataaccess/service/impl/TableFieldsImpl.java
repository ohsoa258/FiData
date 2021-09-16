package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.dto.datareview.DataReviewPageDTO;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.mapper.TableFieldsMapper;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.service.ITableFields;
import com.fisk.dataaccess.vo.TableAccessVO;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Service
public class TableFieldsImpl extends ServiceImpl<TableFieldsMapper, TableFieldsPO> implements ITableFields {
    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private ITableAccess iTableAccess;
    @Resource
    private IAppRegistration iAppRegistration;
    @Override
    public Page<DataReviewVO> listData(DataReviewQueryDTO query) {

        StringBuilder querySql = new StringBuilder();
        querySql.append(generateCondition.getCondition(query.dto));
        DataReviewPageDTO data = new DataReviewPageDTO();
        data.page = query.page;
        data.where = querySql.toString();

        return baseMapper.filter(query.page, data);
    }

    @Override
    public TableFieldsDTO getTableField(int id) {
        //name 类别  简称
        TableFieldsPO tableFieldsPO = baseMapper.selectById(id);
        TableAccessNonDTO data = iTableAccess.getData(tableFieldsPO.tableAccessId);
        AppRegistrationDTO data1 = iAppRegistration.getData(data.appId);
        TableFieldsDTO tableFieldsDTO = new TableFieldsDTO();
        tableFieldsDTO.appbAbreviation=data1.appAbbreviation;
        tableFieldsDTO.fieldName=tableFieldsPO.fieldName;
        tableFieldsDTO.fieldType=tableFieldsPO.fieldType;
        tableFieldsDTO.originalTableName=data.tableName;
        return tableFieldsDTO;
    }

}
