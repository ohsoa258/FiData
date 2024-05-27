package com.fisk.datagovernance.service.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.DataSourceInfoDTO;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.service.dbMetaData.dto.ColumnQueryDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.entity.dataquality.DataCheckPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckLogsVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;

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
    List<DataCheckVO> getAllRule(DataCheckQueryDTO query);

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
    ResultEnum addData(DataCheckDTO dto);

    /**
     * 编辑数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(DataCheckEditDTO dto);

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
     * @param dbId
     * @return
     */
    List<DataSourceInfoDTO> getDataSourceTree(Integer dbId);

    /**
     * 获取表字段信息
     * @param dto
     * @return
     */
    List<TableColumnDTO> getColumn(ColumnQueryDTO dto);
}