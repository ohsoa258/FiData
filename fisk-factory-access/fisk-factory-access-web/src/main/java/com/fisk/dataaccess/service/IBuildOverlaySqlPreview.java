package com.fisk.dataaccess.service;

import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;

import java.util.Objects;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
public interface IBuildOverlaySqlPreview {

    /**
     * 构建数据接入预览SQL语句
     * @param dataSourceDTO
     * @param dataModel
     * @param tableAccessPO
     * @param appRegistrationPO
     * @return
     */
    Object buildStgToOdsSql(DataSourceDTO dataSourceDTO, OverLoadCodeDTO dataModel, TableAccessPO tableAccessPO, AppRegistrationPO appRegistrationPO);
}
