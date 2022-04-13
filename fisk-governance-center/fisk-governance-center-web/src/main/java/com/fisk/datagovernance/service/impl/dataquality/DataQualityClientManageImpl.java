package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.DataQualityRequestDTO;
import com.fisk.datagovernance.enums.dataquality.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.ModuleDataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateModulesTypeEnum;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataQualityClientManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口实现类
 * @date 2022/4/12 13:47
 */
@Service
public class DataQualityClientManageImpl implements IDataQualityClientManageService {

    @Resource
    UserHelper userHelper;

    @Resource
    TemplateMapper templateMapper;

    @Resource
    DataSourceConMapper dataSourceConMapper;

    @Resource
    DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    DataCheckMapper dataCheckMapper;

    @Resource
    BusinessFilterMapper businessFilterMapper;

    @Resource
    LifecycleMapper lifecycleMapper;

    @Resource
    NoticeMapper noticeMapper;

    @Resource
    NoticeManageImpl noticeManageImpl;

    @Resource
    EmailServerMapper emailServerMapper;

    @Resource
    EmailServerManageImpl emailServerManageImpl;

    @Resource
    ComponentNotificationMapper componentNotificationMapper;

    @Override
    public ResultEntity<Object> buildFieldStrongRule(DataQualityRequestDTO requestDTO) {
        // 第一步：验证请求参数是否合法
        ResultEntity<Object> result = null;
        try {
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.DATACHECK_MODULE);
            if (result.code != 0) {
                return result;
            }
            // 第二步：查询配置的强类型校验规则
            DataCheckPO dataCheckPO = dataCheckMapper.selectById(requestDTO.getId());
            if (dataCheckPO == null) {
                return ResultEntityBuild.buildData(ResultEnum.ERROR, "强类型模板规则消费失败，模板组件规则不存在");
            }
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
            if (templatePO == null) {
                return ResultEntityBuild.buildData(ResultEnum.ERROR, "强类型模板规则消费失败，模板不存在");
            }
            // 第三步:查询数据源信息，执行校验语句
            ModuleDataSourceTypeEnum dataSourceTypeEnum = ModuleDataSourceTypeEnum.getEnum(dataCheckPO.getDatasourceType());
            DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(dataCheckPO.getDatasourceId(), dataSourceTypeEnum);
            if (dataSourceConPO == null) {
                return ResultEntityBuild.buildData(ResultEnum.ERROR, "强类型模板规则消费失败，数据源不存在");
            }
            // 校验语句
            String moduleRuleSql = dataCheckPO.getModuleRule();
            if (moduleRuleSql == null || moduleRuleSql.isEmpty()) {
                return ResultEntityBuild.buildData(ResultEnum.ERROR, "强类型模板规则消费失败，校验规则不存在");
            }
            // 获取校验结果
            List<DataCheckResultVO> resultVOS = resultSetToJsonArray(dataSourceConPO, moduleRuleSql);
            if (CollectionUtils.isEmpty(resultVOS)) {
                return ResultEntityBuild.buildData(ResultEnum.ERROR, "强类型模板规则消费失败，执行校验规则查询无结果");
            }
            // 循环校验结果，拼接提示语句
//            StringBuilder stringBuilder = new StringBuilder();
//            for (DataCheckResultVO resultVO : resultVOS) {
//                if (resultVO.getCheckResult() != "success") {
//                    stringBuilder.append(String.format("数据库名称：%s，表名称：%s，%s字段执行%s不通过，请检查表字段数据是否符合规范", // &nbsp;<br/>
//                            resultVO.getCheckDataBase(), resultVO.getCheckTable(), resultVO.getCheckField(), resultVO.getCheckDesc()));
//                }
//            }
            List<DataCheckResultVO> collect = resultVOS.stream().filter(t -> t.getCheckResult() == "fail").collect(Collectors.toList());
            // 检查结果无异常，返回操作结果
            if (CollectionUtils.isEmpty(collect)) {
                return result;
            }
//            if (stringBuilder == null && stringBuilder.length() == 0) {
//                return result;
//            }
            // 检查结果有异常，发送通知
            sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), "");

        } catch (Exception ex) {
            return ResultEntityBuild.buildData(ResultEnum.ERROR, ex.getMessage());
        }
        return result;
    }

    @Override
    public ResultEntity<Object> buildFieldAggregateRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildTableRowThresholdRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildEmptyTableCheckRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildUpdateTableRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildTableBloodKinshipRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildBusinessCheckRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildSimilarityRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildBusinessFilterRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildSpecifyTimeRecyclingRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildEmptyTableRecoveryRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildNoRefreshDataRecoveryRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildDataBloodKinshipRecoveryRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.Object>
     * @description 发送告警通知
     * @author dick
     * @date 2022/4/12 20:54
     * @version v1.0
     * @params templateId
     * @params moduleId
     * @params body
     */
    public ResultEntity<Object> sendNotice(int templateId, long moduleId, String body) {
        // 检查结果异常，发送提示邮件
        QueryWrapper<ComponentNotificationPO> notificationPOQueryWrapper = new QueryWrapper<>();
        notificationPOQueryWrapper.lambda().eq(ComponentNotificationPO::getDelFlag, 1)
                .eq(ComponentNotificationPO::getTemplateId, templateId)
                .eq(ComponentNotificationPO::getModuleId, moduleId);
        List<ComponentNotificationPO> componentNotificationPOS = componentNotificationMapper.selectList(notificationPOQueryWrapper);
        if (CollectionUtils.isEmpty(componentNotificationPOS)) {
            return ResultEntityBuild.buildData(ResultEnum.ERROR, "强类型模板规则消费失败，校验结果已生成，但未配置告警通知");
        }
        // 查询告警通知方式
        List<Integer> noticeIdList = componentNotificationPOS.stream().map(ComponentNotificationPO::getNoticeId).collect(Collectors.toList());
        QueryWrapper<NoticePO> noticePOQueryWrapper = new QueryWrapper<>();
        noticePOQueryWrapper.lambda().eq(NoticePO::getDelFlag, 1)
                .in(NoticePO::getId, noticeIdList);
        List<NoticePO> noticePOS = noticeMapper.selectList(noticePOQueryWrapper);
        if (CollectionUtils.isEmpty(noticePOS)) {
            return ResultEntityBuild.buildData(ResultEnum.ERROR, "强类型模板规则消费失败，校验结果已生成，告警方式不存在");
        }
        // 邮件通知
        List<NoticePO> emailNoticePOS = noticePOS.stream().filter(t -> t.getNoticeType() == 1).collect(Collectors.toList());
        // 系统通知
        List<NoticePO> systemNoticePOS = noticePOS.stream().filter(t -> t.getNoticeType() == 2).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(emailNoticePOS)) {
            for (NoticePO emailNoticePO : emailNoticePOS) {
                NoticeDTO noticeDTO = new NoticeDTO();
                noticeDTO.emailServerId = emailNoticePO.getEmailServerId();
                noticeDTO.emailSubject = emailNoticePO.getEmailSubject();
                noticeDTO.body = emailNoticePO.getBody();
                noticeDTO.emailConsignee = emailNoticePO.getEmailConsignee();
                noticeDTO.emailCc = emailNoticePO.getEmailCc();
                noticeManageImpl.sendEmialNotice(noticeDTO);
            }
        }
        if (CollectionUtils.isNotEmpty(systemNoticePOS)) {
            noticeManageImpl.sendSystemNotice(systemNoticePOS);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "强类型模板规则消费成功");
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO>
     * @description 执行sql，返回结果
     * @author dick
     * @date 2022/4/12 18:50
     * @version v1.0
     * @params dataSourceConPO
     * @params sql
     */
    public List<DataCheckResultVO> resultSetToJsonArray(DataSourceConPO dataSourceConPO, String sql) {
        List<DataCheckResultVO> resultVOS = new ArrayList<>();
        try {
            JSONArray array = new JSONArray();
            // 数据源类型
            DataSourceTypeEnum sourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];
            // 数据库连接对象
            Statement st = null;
            Connection conn = dataSourceConManageImpl.getStatement(sourceTypeEnum.getDriverName(), dataSourceConPO.getConStr(),
                    dataSourceConPO.getConAccount(), dataSourceConPO.getConPassword());
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(columnName);
                    jsonObj.put(columnName, value);
                }
                array.add(jsonObj);
            }
            rs.close();
            if (array != null && array.size() > 0) {
                resultVOS = array.toJavaList(DataCheckResultVO.class);
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, ex.getMessage());
        }
        return resultVOS;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.Object>
     * @description 验证请求参数是否合法
     * @author dick
     * @date 2022/4/12 17:00
     * @version v1.0
     * @params requestDTO
     * @params typeEnum
     */
    public ResultEntity<Object> paramterVerification(DataQualityRequestDTO requestDTO, TemplateModulesTypeEnum typeEnum) {
        if (requestDTO == null || requestDTO.getId() == 0
                || requestDTO.getTemplateModulesType() != typeEnum) {
            return ResultEntityBuild.buildData(ResultEnum.ERROR, "强类型模板规则消费失败，请求参数异常");
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "强类型模板规则消费成功");
    }
}
