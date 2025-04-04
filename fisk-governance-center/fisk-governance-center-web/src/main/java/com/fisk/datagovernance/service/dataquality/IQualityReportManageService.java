package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckRulePageDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.*;
import com.fisk.datagovernance.entity.dataquality.QualityReportPO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.*;

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
     * 获取创建质量报告所需的扩展信息（邮件服务器、用户邮箱）
     *
     * @return 执行结果
     */
    QualityReportExtVO getMailServerAndUserInfo();

    /**
     * 获取质量报告数据校验规则弹窗列表
     *
     * @return 分页列表
     */
    Page<QualityReportExt_RuleVO> getQualityReportRuleList(DataCheckRulePageDTO query);

    /**
     * 数据校验质量报告日志
     *
     * @return 执行结果
     */
    Page<QualityReportLogVO> getDataCheckQualityReportLog(QualityReportLogQueryDTO dto);

    /**
     * 下载报告记录
     *
     * @return 执行结果
     */
    void downloadReportRecord(int reportLogId, HttpServletResponse response);

    /**
     * 下载附件数据
     *
     * @return 执行结果
     */
    void downloadExcelReport(int attachmentId, HttpServletResponse response);

    /**
     * 预览报告记录
     *
     * @return 执行结果
     */
    List<PreviewQualityReportVO> previewReportRecord(int reportLogId);

    /**
     * 获取Cron表达式最近3次执行时间
     *
     * @return 执行结果
     */
    List<String> getNextCronExeTime(String cron);
}