package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportEditDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportLogQueryDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportQueryDTO;
import com.fisk.datagovernance.entity.dataquality.QualityReportPO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.PreviewQualityReportVO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportExtVO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportLogVO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告接口
 * @date 2022/3/23 12:22
 */
public interface IQualityReportManageService extends IService<QualityReportPO> {
    /**
     * 分页查询
     *
     * @return 分页列表
     */
    Page<QualityReportVO> getAll(QualityReportQueryDTO query);

    /**
     * 过滤器
     *
     * @return 字段
     */
    List<FilterFieldDTO> getColumn();

    /**
     * 添加数据
     *
     * @return 执行结果
     */
    ResultEnum addData(QualityReportDTO dto);

    /**
     * 编辑数据
     *
     * @return 执行结果
     */
    ResultEnum editData(QualityReportEditDTO dto);

    /**
     * 启用禁用
     *
     * @return 执行结果
     */
    ResultEnum editState(int id);

    /**
     * 删除数据
     *
     * @return 执行结果
     */
    ResultEnum deleteData(int id);

    /**
     * 执行发送
     *
     * @return 执行结果
     */
    ResultEnum collReport(int id);

    /**
     * 报告相关数据
     *
     * @return 执行结果
     */
    QualityReportExtVO getReportExt();

    /**
     * 报告日志
     *
     * @return 执行结果
     */
    Page<QualityReportLogVO> getAllReportLog(QualityReportLogQueryDTO dto);

    /**
     * 下载报告记录
     *
     * @return 执行结果
     */
    HttpServletResponse downloadReportRecord(int reportLogId, HttpServletResponse response);

    /**
     * 预览报告记录
     *
     * @return 执行结果
     */
    List<PreviewQualityReportVO> previewReportRecord(int reportLogId);
}