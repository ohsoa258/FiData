package com.fisk.datagovernance.service.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.DataSourceInfoDTO;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.service.dbMetaData.dto.ColumnQueryDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.entity.dataquality.DataCheckPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.*;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验接口
 * @date 2022/3/23 12:22
 */
public interface IDataCheckManageService extends IService<DataCheckPO> {
    /**
     * 查询所有规则
     *
     * @return 规则列表
     */
    PageDTO<DataCheckVO> getAllRule(DataCheckQueryDTO query);

    /**
     * 获取规则搜索条件
     *
     * @return 规则列表
     */
    DataCheckRuleSearchWhereVO getRuleSearchWhere();

    /**
     * 获取数据检查结果日志搜索条件
     *
     * @return 规则列表
     */
    DataCheckRuleSearchWhereVO getDataCheckLogSearchWhere();

    /**
     * 查询所有规则
     *
     * @return 规则列表
     */
    List<DataCheckVO> getRuleByIds(List<Integer> ids);

    /**
     * 添加数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(DataCheckDTO dto, boolean isPreVerification);

    /**
     * 编辑数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(DataCheckEditDTO dto, boolean isPreVerification);

    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(int id);

    /**
     * 界面/接口验证
     *
     * @return 执行结果
     */
    ResultEntity<List<DataCheckResultVO>> interfaceCheckData(DataCheckWebDTO dto);

    /**
     * 同步验证
     *
     * @return 执行结果
     */
    ResultEntity<List<DataCheckResultVO>> nifiSyncCheckData(DataCheckSyncDTO dto);

    /**
     * 检查日志
     *
     * @return 分页列表
     */
    Page<DataCheckLogsVO> getDataCheckLogsPage(DataCheckLogsQueryDTO dto);

    /**
     * 检查日志结果
     *
     * @return 检查结果
     */
    JSONArray getDataCheckLogsResult(long logId);

    /**
     * 删除检查日志
     *
     * @return 检查结果
     */
    ResultEnum deleteDataCheckLogs(long ruleId);

    /**
     * 检查规则日志增加质量分析
     *
     * @return 结果
     */
    ResultEnum dataCheckLogAddQualityAnalysis(DataCheckLogCommentDTO dto);

    /**
     * 生成数据检查结果——Excel
     *
     * @return 检查结果
     */
    String createDataCheckResultExcel(String logIds);

    /**
     * 删除数据检查结果
     *
     * @return 操作结果
     */
    ResultEnum deleteCheckResult();

    /**
     * 查看数据源结构树
     *
     * @param dbId
     * @return
     */
    List<DataSourceInfoDTO> getDataSourceTree(Integer dbId);

    /**
     * 获取表字段信息
     *
     * @param dto
     * @return
     */
    List<TableColumnDTO> getColumn(ColumnQueryDTO dto);

    /**
     * 获取所有数据校验规则数量
     *
     * @return
     */
    Integer getDataCheckRoleTotal();

    /**
     * 查询所有数据集规则
     *
     * @return 规则列表
     */
    PageDTO<DataCheckVO> getAllDataSetRule(DataSetQueryDTO query);

    /**
     * 预览sql
     * @param dto
     * @return
     */
    DataSetPreviewVO dataSetPreview(DataSetPreviewDTO dto);
}