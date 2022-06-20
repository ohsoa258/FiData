package com.fisk.datagovernance.service.dataquality;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datagovernance.dto.dataquality.rule.QueryRuleDTO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.rule.TableRuleInfoVO;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口
 * @date 2022/4/12 13:41
 */
public interface IDataQualityClientManageService {

    /**
     * 查询数据质量表规则（含字段规则）
     */
    ResultEntity<TableRuleInfoVO> getTableRuleList(QueryRuleDTO requestDTO);

    /**
     * 查询数据质量所有数据源信息，含FiData系统数据源
     */
    ResultEntity<List<DataSourceConVO>> getAllDataSource();
}
