package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.Dto.Excel.ExcelDto;
import com.fisk.common.core.utils.Dto.Excel.RowDto;
import com.fisk.common.core.utils.Dto.Excel.SheetDto;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.core.utils.office.excel.ExcelReportUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.assetschangeanalysis.AssetsChangeAnalysisDetailDTO;
import com.fisk.datamanagement.dto.assetschangeanalysis.AssetsChangeAnalysisDetailQueryDTO;
import com.fisk.datamanagement.dto.metaanalysisemailconfig.MetaAnalysisEmailConfigDTO;
import com.fisk.datamanagement.entity.EmailGroupPO;
import com.fisk.datamanagement.entity.EmailGroupUserMapPO;
import com.fisk.datamanagement.entity.EmailUserPO;
import com.fisk.datamanagement.entity.MetaAnalysisEmailConfigPO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.map.MetaAnalysisEmailMap;
import com.fisk.datamanagement.mapper.MetaAnalysisEmailConfigMapper;
import com.fisk.datamanagement.service.IMetaAnalysisEmailConfigService;
import com.fisk.system.client.UserClient;
import com.fisk.system.enums.EmailServerTypeEnum;
import com.fisk.system.vo.emailserver.EmailServerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 56263
 * @description 针对表【tb_meta_analysis_email_config】的数据库操作Service实现
 * @createDate 2024-05-22 11:01:15
 */
@Service
@Slf4j
public class MetaAnalysisEmailConfigServiceImpl extends ServiceImpl<MetaAnalysisEmailConfigMapper, MetaAnalysisEmailConfigPO>
        implements IMetaAnalysisEmailConfigService {

    @Resource
    private IEmailGroupServiceImpl iEmailGroupService;

    @Resource
    private IEmailGroupUserMapServiceImpl emailGroupUserMapService;

    @Resource
    private EmailUserPOServiceImpl emailUserPOService;

    @Resource
    private MetadataEntityAuditLogImpl auditLog;

    @Resource
    private UserClient userClient;


    /**
     * 获取变更分析邮箱配置的详情
     * tb_meta_analysis_email_config 表只允许有一条数据！
     */
    @Override
    public MetaAnalysisEmailConfigDTO getMetaAnalysisEmailConfig() {
        List<MetaAnalysisEmailConfigPO> list = this.list();
        if (CollectionUtils.isEmpty(list)){
            return new MetaAnalysisEmailConfigDTO();
        }

        MetaAnalysisEmailConfigPO metaAnalysisEmailConfigPO = list.get(0);

        return MetaAnalysisEmailMap.INSTANCES.poToDto(metaAnalysisEmailConfigPO);
    }

    /**
     * 编辑变更分析邮箱配置的详情
     * 注意：tb_meta_analysis_email_config只允许有一条数据
     *
     * @param dto
     * @return
     */
    @Override
    public Object editMetaAnalysisEmailConfig(MetaAnalysisEmailConfigDTO dto) {
        return this.saveOrUpdate(MetaAnalysisEmailMap.INSTANCES.dtoToPo(dto));
    }

    /**
     * 变更分析发送邮件
     */
    @Override
    public void sendEmailOfMetaAudit() {
        log.info("**********变更分析发送邮件任务开始执行**********");

        //1.查询变更分析配置的邮件发送规则及数据查询周期
        List<MetaAnalysisEmailConfigPO> list = this.list();
        if (CollectionUtils.isEmpty(list)) {
            log.error("没有获取到变更分析的邮件配置信息");
            return;
        }
        MetaAnalysisEmailConfigPO metaAnalysisEmailConfigPO = list.get(0);

        //邮箱组id
        int emailGroupId = metaAnalysisEmailConfigPO.getEmailGroupId();
        //查询审计日志的周期
        /*
        审计变更日志查询周期（单位：天）：1  3  7（1周内）  30（1个月内）  365（一年内）
         */
        int queryTime = metaAnalysisEmailConfigPO.getQueryTime();
        //实体类型 0全部 3表 6字段
        EntityTypeEnum value = EntityTypeEnum.getValue(metaAnalysisEmailConfigPO.getEntityType());

        //获取开始时间和结束时间
        LocalDateTime now = LocalDateTime.now();

        AssetsChangeAnalysisDetailQueryDTO dto = new AssetsChangeAnalysisDetailQueryDTO();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        switch (queryTime) {
            case 1:
                dto.setStartTime(now.minusDays(1).format(formatter));
                dto.setEndTime(now.format(formatter));
                break;
            case 3:
                dto.setStartTime(now.minusDays(3).format(formatter));
                dto.setEndTime(now.format(formatter));
                break;
            case 7:
                dto.setStartTime(now.minusWeeks(1).format(formatter));
                dto.setEndTime(now.format(formatter));
                break;
            case 30:
                dto.setStartTime(now.minusMonths(1).format(formatter));
                dto.setEndTime(now.format(formatter));
                break;
            case 365:
                dto.setStartTime(now.minusYears(1).format(formatter));
                dto.setEndTime(now.format(formatter));
                break;
        }
        dto.setEntityType(value);
        //2.查询周期内审计日志需要生成的excel数据 然后生成excel
        List<AssetsChangeAnalysisDetailDTO> metaChangesChartsDetailWithoutPage = auditLog.getMetaChangesChartsDetailWithoutPage(dto);

        String currentFileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
        String uploadUrl = "D:/Excel/";

        List<SheetDto> sheetList = new ArrayList<>();
        SheetDto sheet = new SheetDto();
        List<RowDto> singRows = new ArrayList<>();
        RowDto rowDto = new RowDto();
        rowDto.setRowIndex(0);
        List<String> headerNames = new ArrayList<>();
        headerNames.add("父元数据名称");
        headerNames.add("元数据名称");
        headerNames.add("变更时间");
        headerNames.add("变更内容");
        headerNames.add("影响到的元数据");
        headerNames.add("元数据类型名称");
        headerNames.add("变更类型");
        headerNames.add("元数据id");
        headerNames.add("创建人id/名称");
        headerNames.add("元数据类型编码");


        rowDto.setColumns(headerNames);
        singRows.add(rowDto);
        String sheetName = "Sheet01";
        sheet.setSheetName(sheetName);
        sheet.setSingRows(singRows);


        JSONArray jsonArray = null;
        String data = JSON.toJSONString(metaChangesChartsDetailWithoutPage);
        if (StringUtils.isNotEmpty(data)) {
            jsonArray = JSON.parseArray(data);
        }
        List<List<String>> dataRowList = createDataCheckResultExcel_GetDataRows(jsonArray);
        sheet.setDataRows(dataRowList);
        sheetList.add(sheet);
        ExcelDto excelDto = new ExcelDto();
        excelDto.setExcelName(currentFileName);
        excelDto.setSheets(sheetList);
        ExcelReportUtil.createExcel(excelDto, uploadUrl, currentFileName, true);

        //3.发送邮件 携带刚生成的excel附件
        sendEmail(emailGroupId, uploadUrl, currentFileName, dto.getStartTime(), dto.getEndTime());
    }

    /**
     * 发送邮件
     *
     * @return
     */
    private void sendEmail(int groupId, String excelPath, String fileName, String startTime, String endTime) {
        //获取邮件组
        EmailGroupPO groupPO = iEmailGroupService.getById(groupId);

        int id = (int) groupPO.getId();
        //获取当前邮件组跟用户的关联信息
        List<EmailGroupUserMapPO> list = emailGroupUserMapService.list(new LambdaQueryWrapper<EmailGroupUserMapPO>().eq(EmailGroupUserMapPO::getGroupId, id));
        //通过关联信息获取用户id list
        List<Integer> collect = list.stream().map(EmailGroupUserMapPO::getUserId).collect(Collectors.toList());
        //获取当前邮件组下的用户邮件信息
        List<EmailUserPO> emailUserPOS = emailUserPOService.listByIds(collect);

        //获取system 邮件服务器id
        Integer emailServerId = groupPO.getEmailServerId();

        //第一步：查询邮件服务器设置
        ResultEntity<EmailServerVO> resultEntity = userClient.getEmailServerById(emailServerId);
        if (resultEntity == null || resultEntity.getCode() != ResultEnum.SUCCESS.getCode() ||
                resultEntity.getData() == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        EmailServerVO data = resultEntity.getData();
        if (data.getEmailServerType().equals(EmailServerTypeEnum.SMTP)) {
            MailServeiceDTO mailServeiceDTO = new MailServeiceDTO();
            mailServeiceDTO.setOpenAuth(true);
            mailServeiceDTO.setOpenDebug(true);
            mailServeiceDTO.setHost(data.getEmailServer());
            mailServeiceDTO.setProtocol(data.getEmailServerType().getName());
            mailServeiceDTO.setUser(data.getEmailServerAccount());
            mailServeiceDTO.setPassword(data.getEmailServerPwd());
            mailServeiceDTO.setPort(data.getEmailServerPort());

            for (EmailUserPO emailUserPO : emailUserPOS) {
                MailSenderDTO mailSenderDTO = new MailSenderDTO();
                mailSenderDTO.setUser(data.getEmailServerAccount());
                //邮件标题
                mailSenderDTO.setSubject("FiData【数据资产 - 元数据变更分析】");
                //邮件正文
                mailSenderDTO.setBody("【监测周期：" + startTime + " - " + endTime + " 】" + " 此邮件为数据资产元数据变更分析通知邮件，变更明细请参照附件。");
                mailSenderDTO.setToAddress(emailUserPO.getEmailAddress());
                mailSenderDTO.setSendAttachment(true);
                //文件实际名称  不要被实体类的注释骗了！
                mailSenderDTO.setAttachmentName(fileName);
                //附件名称
                mailSenderDTO.setAttachmentActualName("元数据变更分析明细.xlsx");
                mailSenderDTO.setAttachmentPath(excelPath);
                try {
                    //第二步：调用邮件发送方法
                    log.info("元数据变更分析邮件参数-mailServeiceDTO：" + JSON.toJSONString(mailServeiceDTO));
                    log.info("元数据变更分析邮件参数-mailSenderDTO：" + JSON.toJSONString(mailSenderDTO));
                    log.info("FiData 【数据资产】【元数据变更分析】开始发送邮件");
                    MailSenderUtils.send(mailServeiceDTO, mailSenderDTO);
                } catch (Exception e) {
                    log.error("FiData 【数据资产】【元数据变更分析】发送邮件失败" + e);
                    throw new FkException(ResultEnum.EMAIL_NOT_SEND, e.getMessage());
                }
            }

        }

    }

    /**
     * 转换数据行
     * @param jsonArray
     * @return
     */
    public List<List<String>> createDataCheckResultExcel_GetDataRows(JSONArray jsonArray) {
        List<List<String>> dataRowList = new ArrayList<>();
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(jsonArray)) {
            for (int i = 0; i < jsonArray.size(); i++) {
                Set<Map.Entry<String, Object>> entrySet = jsonArray.getJSONObject(i).entrySet();
                List<String> dataRow = new ArrayList<>();
                for (Map.Entry<String, Object> entry : entrySet) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    dataRow.add(value != null ? value.toString() : "");
                }
                dataRowList.add(dataRow);
            }
        }
        return dataRowList;
    }

}




