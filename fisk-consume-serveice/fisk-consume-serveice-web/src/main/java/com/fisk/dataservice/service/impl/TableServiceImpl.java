package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datafactory.enums.SendModeEnum;
import com.fisk.dataservice.dto.datasource.DataSourceConfigInfoDTO;
import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.dto.tableservice.*;
import com.fisk.dataservice.entity.AppServiceConfigPO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.entity.TableRecipientsPO;
import com.fisk.dataservice.entity.TableServicePO;
import com.fisk.dataservice.enums.ApiStateTypeEnum;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.map.DataSourceConMap;
import com.fisk.dataservice.map.TableServiceMap;
import com.fisk.dataservice.mapper.AppServiceConfigMapper;
import com.fisk.dataservice.mapper.TableRecipientsMapper;
import com.fisk.dataservice.mapper.TableServiceMapper;
import com.fisk.dataservice.service.ITableService;
import com.fisk.dataservice.vo.tableservice.TableRecipientsVO;
import com.fisk.dataservice.vo.tableservice.WechatUserVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.BuildDeleteTableServiceDTO;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.fisk.dataservice.util.HttpUtils.HttpGet;
import static com.fisk.dataservice.util.HttpUtils.HttpPost;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class TableServiceImpl
        extends ServiceImpl<TableServiceMapper, TableServicePO>
        implements ITableService {

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private TableFieldImpl tableField;

    @Resource
    private TableSyncModeImpl tableSyncMode;

    @Resource
    private UserClient userClient;

    @Resource
    private UserHelper userHelper;

    @Resource
    private TableServiceMapper mapper;

    @Resource
    private AppServiceConfigMapper appServiceConfigMapper;

    @Resource
    private TableAppManageImpl tableAppManage;

    @Resource
    private TableRecipientsMapper tableRecipientsMapper;

    @Resource
    private TableRecipientsManageImpl tableRecipientsManage;

    @Override
    public Page<TableServicePageDataDTO> getTableServiceListData(TableServicePageQueryDTO dto) {
        return mapper.getTableServiceListData(dto.page, dto);
    }

    @Override
    public ResultEntity<Object> addTableServiceData(TableServiceDTO dto) {
        TableServicePO po = this.query().eq("table_name", dto.tableName).one();
        if (po != null) {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }
        TableServicePO data = TableServiceMap.INSTANCES.dtoToPo(dto);
        if (!this.save(data)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        AppServiceConfigPO appServiceConfigPO = new AppServiceConfigPO();
        appServiceConfigPO.setAppId(dto.getTableAppId());
        appServiceConfigPO.setServiceId(Math.toIntExact(data.getId()));
        appServiceConfigPO.setApiState(ApiStateTypeEnum.Enable.getValue());
        appServiceConfigPO.setType(AppServiceTypeEnum.TABLE.getValue());
        int insert = appServiceConfigMapper.insert(appServiceConfigPO);
        if (insert <= 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data.id);
    }

    @Override
    public List<DataSourceConfigInfoDTO> getDataSourceConfig() {

        ResultEntity<List<DataSourceDTO>> allExternalDataSource = userClient.getAllExternalDataSource();
        if (allExternalDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        return DataSourceConMap.INSTANCES.voListToDtoInfo(allExternalDataSource.data);
    }

    @Override
    public List<DataSourceConfigInfoDTO> getAllDataSourceConfig() {

        ResultEntity<List<DataSourceDTO>> all = userClient.getAll();
        if (all.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        return DataSourceConMap.INSTANCES.voListToDtoInfo(all.data);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum TableServiceSave(TableServiceSaveDTO dto) {

        //修改表服务数据
        updateTableService(dto.tableService);

        //表字段
        tableField.tableServiceSaveConfig((int) dto.tableService.id, 0, dto.tableFieldList);

        //覆盖方式
        dto.tableSyncMode.typeTableId = (int) dto.tableService.id;
        dto.tableSyncMode.type = AppServiceTypeEnum.TABLE.getValue();
        tableSyncMode.tableServiceTableSyncMode(dto.tableSyncMode);

        BuildTableServiceDTO buildTableServiceDTO = buildParameter(dto);

        UserInfo userInfo = userHelper.getLoginUserInfo();
        buildTableServiceDTO.userId = userInfo.id;

        //推送task
        publishTaskClient.publishBuildDataServices(buildTableServiceDTO);

        return ResultEnum.SUCCESS;
    }

    @Override
    public TableServiceSaveDTO getTableServiceById(long id) {
        TableServicePO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        TableServiceSaveDTO data = new TableServiceSaveDTO();
        data.tableService = TableServiceMap.INSTANCES.poToDto(po);
        data.tableFieldList = tableField.getTableServiceField(id, 0);
        data.tableSyncMode = tableSyncMode.getTableServiceSyncMode(id);
        return data;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum delTableServiceById(long id) {
        TableServicePO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        if (mapper.deleteByIdWithFill(po) == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        tableField.delTableServiceField((int) id, 0);

        tableSyncMode.delTableServiceSyncMode(id, AppServiceTypeEnum.TABLE.getValue());

        QueryWrapper<AppServiceConfigPO> appServiceConfigPOQueryWrapper = new QueryWrapper<>();
        appServiceConfigPOQueryWrapper.lambda()
                .eq(AppServiceConfigPO::getServiceId, id)
                .eq(AppServiceConfigPO::getType, AppServiceTypeEnum.TABLE.getValue());
        List<AppServiceConfigPO> appServiceConfigPOS = appServiceConfigMapper.selectList(appServiceConfigPOQueryWrapper);
        if (CollectionUtils.isNotEmpty(appServiceConfigPOS)) {
            if (appServiceConfigMapper.deleteByIdWithFill(appServiceConfigPOS.get(0)) <= 0) {
                throw new FkException(ResultEnum.DELETE_ERROR);
            }
            LambdaQueryWrapper<TableRecipientsPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TableRecipientsPO::getTableAppId,appServiceConfigPOS.get(0).appId);
            if (tableRecipientsManage.remove(queryWrapper)){
                throw new FkException(ResultEnum.DELETE_ERROR);
            }
        }
        BuildDeleteTableServiceDTO buildDeleteTableService = new BuildDeleteTableServiceDTO();
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        buildDeleteTableService.appId = String.valueOf(appServiceConfigPOS.get(0).getAppId());
        buildDeleteTableService.userId = userHelper.getLoginUserInfo().id;
        buildDeleteTableService.ids = ids;
        buildDeleteTableService.olapTableEnum = OlapTableEnum.DATASERVICES;
        buildDeleteTableService.delBusiness = false;
        publishTaskClient.publishBuildDeleteDataServices(buildDeleteTableService);
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<BuildTableServiceDTO> getTableListByPipelineId(Integer pipelineId) {
        List<Integer> tableListByPipelineId = tableSyncMode.getTableListByPipelineId(pipelineId);
        if (CollectionUtils.isEmpty(tableListByPipelineId)) {
            return new ArrayList<>();
        }

        List<BuildTableServiceDTO> list = new ArrayList<>();

        for (Integer id : tableListByPipelineId) {
            TableServiceSaveDTO tableService = getTableServiceById(id);
            list.add(buildParameter(tableService));
        }
        return list;
    }

    @Override
    public void updateTableServiceStatus(TableServicePublishStatusDTO dto) {
        TableServicePO po = mapper.selectById(dto.id);
        if (po == null) {
            log.error("【表服务修改状态失败,原因:表不存在】");
            return;
        }
        po.publish = dto.status;
        if (mapper.updateById(po) > 0) {
            log.error("表服务修改状态失败,原因表:修改异常");
        }

    }

    @Override
    public BuildTableServiceDTO getBuildTableServiceById(long id) {

        TableServiceSaveDTO data = getTableServiceById(id);

        return buildParameter(data);
    }

    @Override
    public ResultEnum addTableServiceField(TableFieldDTO dto) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        List<TableFieldDTO> dtoList = new ArrayList<>();
        dtoList.add(dto);
        return tableField.addTableServiceField(0, dtoList);
    }

    @Override
    public ResultEnum editTableServiceField(TableFieldDTO dto) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        List<TableFieldDTO> dtoList = new ArrayList<>();
        dtoList.add(dto);
        return tableField.tableServiceSaveConfig(0, dto.getId(), dtoList);
    }

    @Override
    public ResultEnum deleteTableServiceField(long tableFieldId) {
        return tableField.delTableServiceField(0, tableFieldId);
    }

    @Override
    public ResultEnum editTableServiceSync(long tableId) {
        TableServicePO tableServicePO = mapper.selectById(tableId);
        //判断表状态是否已发布
        if (tableServicePO.getPublish() != 1) {
            log.info("手动同步失败，原因：表未发布");
            return ResultEnum.TABLE_NOT_PUBLISHED;
        }
        //获取远程调用接口中需要的参数KafkaReceiveDTO
        KafkaReceiveDTO kafkaReceiveDTO = getKafkaReceive(tableId);
        log.info(JSON.toJSONString(kafkaReceiveDTO));

        //参数配置完毕，远程调用接口，发送参数，执行同步
        ResultEntity<Object> resultEntity = publishTaskClient.universalPublish(kafkaReceiveDTO);
        if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public TableRecipientsVO getTableServiceAlarmNoticeByAppId(int tableAppId) {
        TableRecipientsVO tableRecipientsVO = null;
        QueryWrapper<TableRecipientsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableRecipientsPO::getDelFlag, 1)
                .eq(TableRecipientsPO::getTableAppId, tableAppId);
        List<TableRecipientsPO> tableRecipientsPOList = tableRecipientsMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(tableRecipientsPOList)) {
            ResultEntity<EmailServerVO> emailServerById = userClient.getEmailServerById(tableRecipientsPOList.get(0).getNoticeServerId());
            if (emailServerById == null || emailServerById.getCode() != ResultEnum.SUCCESS.getCode() || emailServerById.getData() == null) {
                throw new FkException(ResultEnum.DS_THE_MESSAGE_NOTIFICATION_METHOD_DOES_NOT_EXIST);
            }

            int noticeServerType = emailServerById.getData().getServerConfigType();
            List<WechatUserVO> wechatUserList = new ArrayList<>();
            if (noticeServerType == 2) {
                tableRecipientsPOList.forEach(t -> {
                    if (org.apache.commons.lang.StringUtils.isNotEmpty(t.getWechatUserId())) {
                        WechatUserVO wechatUserVO = new WechatUserVO();
                        wechatUserVO.setWechatUserId(t.getWechatUserId());
                        wechatUserVO.setWechatUserName(t.getWechatUserName());
                        wechatUserList.add(wechatUserVO);
                    }
                });
            }
            tableRecipientsVO = new TableRecipientsVO();
            tableRecipientsVO.setTableAppId(tableRecipientsPOList.get(0).getTableAppId());
            tableRecipientsVO.setNoticeServerId(tableRecipientsPOList.get(0).getNoticeServerId());
            tableRecipientsVO.setAlarmConditions(tableRecipientsPOList.get(0).getAlarmConditions());
            tableRecipientsVO.setUserEmails(tableRecipientsPOList.get(0).getUserEmails());
            tableRecipientsVO.setWechatUserList(wechatUserList);
        }
        return tableRecipientsVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveTableServiceAlarmNotice(TableRecipientsDTO dto) {
        List<TableRecipientsPO> tableRecipientsPOS = new ArrayList<>();
        if (dto.getNoticeServerType() == 1) {
            TableRecipientsPO tableRecipientsPO = new TableRecipientsPO();
            tableRecipientsPO.setTableAppId(dto.getTableAppId());
            tableRecipientsPO.setNoticeServerId(dto.getNoticeServerId());
            tableRecipientsPO.setAlarmConditions(dto.getAlarmConditions());
            tableRecipientsPO.setUserEmails(dto.getUserEmails());
            tableRecipientsPO.setType(dto.getNoticeServerType());
            tableRecipientsPOS.add(tableRecipientsPO);
        } else if (dto.getNoticeServerType() == 2 && CollectionUtils.isNotEmpty(dto.getWechatUserList())) {
            dto.getWechatUserList().forEach(t -> {
                TableRecipientsPO tableRecipientsPO = new TableRecipientsPO();
                tableRecipientsPO.setTableAppId(dto.getTableAppId());
                tableRecipientsPO.setNoticeServerId(dto.getNoticeServerId());
                tableRecipientsPO.setAlarmConditions(dto.getAlarmConditions());
                tableRecipientsPO.setWechatUserId(t.getWechatUserId());
                tableRecipientsPO.setWechatUserName(t.getWechatUserName());
                tableRecipientsPO.setType(dto.getNoticeServerType());
                tableRecipientsPOS.add(tableRecipientsPO);
            });
        }
        if (CollectionUtils.isNotEmpty(tableRecipientsPOS)) {
            // 先删再插
            QueryWrapper<TableRecipientsPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TableRecipientsPO::getDelFlag, 1)
                    .eq(TableRecipientsPO::getTableAppId, dto.getTableAppId());
            List<TableRecipientsPO> tableRecipientsPOList = tableRecipientsMapper.selectList(queryWrapper);
            if (CollectionUtils.isNotEmpty(tableRecipientsPOList)) {
                List<Long> idList = tableRecipientsPOList.stream().map(TableRecipientsPO::getId).collect(Collectors.toList());
                // 修改的是del_flag状态
                tableRecipientsManage.removeByIds(idList);
            }
            return tableRecipientsManage.saveBatch(tableRecipientsPOS) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteTableServiceEmail(TableServiceEmailDTO tableServiceEmail) {
        LambdaQueryWrapper<TableRecipientsPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableRecipientsPO::getTableAppId,tableServiceEmail.appId);
        boolean remove = tableRecipientsManage.remove(queryWrapper);
        if(remove){
            return ResultEnum.SUCCESS;
        }else {
            return ResultEnum.DELETE_ERROR;
        }
    }

    @Override
    public ResultEnum tableServiceSendEmails(TableServiceEmailDTO tableServiceEmail) {
        //获取单个管道配置
        TableAppPO tableAppPO = tableAppManage.getById(tableServiceEmail.appId);
        if (tableAppPO != null) {
            tableServiceEmail.body.put("表服务名称", tableAppPO.getAppName());
        }
        // 发邮件
        List<TableRecipientsPO>  email = tableRecipientsManage.query().eq("table_app_id", tableServiceEmail.appId).list();
        //第一步：查询邮件服务器设置
        if (!CollectionUtils.isNotEmpty(email)){
            return ResultEnum.ERROR;
        }
        ResultEntity<EmailServerVO> emailServerById = userClient.getEmailServerById(email.get(0).noticeServerId);
        if (emailServerById == null || emailServerById.getCode() != ResultEnum.SUCCESS.getCode() ||
                emailServerById.getData() == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //true是失败
        boolean contains = tableServiceEmail.msg.contains(NifiStageTypeEnum.RUN_FAILED.getName());
        Integer sendMode = email.get(0).alarmConditions;
        if (Objects.equals(SendModeEnum.failure.getValue(), sendMode)) {
            if (contains) {
                log.info("满足模式,并且msg报错");
            } else {
                log.info("满足模式,但是msg没报错");
                return ResultEnum.SUCCESS;
            }
        } else if (Objects.equals(SendModeEnum.finish.getValue(), sendMode)) {
            log.info("所有模式都发通知");
        } else if (Objects.equals(SendModeEnum.success.getValue(), sendMode)) {
            if (!contains) {
                log.info("满足模式,并且msg没报错");
            } else {
                log.info("满足模式,但是msg报错");
                return ResultEnum.SUCCESS;
            }
        }

        if (email.get(0).type == 1) {
            EmailServerVO emailServerVO = emailServerById.getData();
            MailServeiceDTO mailServeiceDTO = new MailServeiceDTO();
            mailServeiceDTO.setOpenAuth(true);
            mailServeiceDTO.setOpenDebug(true);
            mailServeiceDTO.setHost(emailServerVO.getEmailServer());
            mailServeiceDTO.setProtocol(emailServerVO.getEmailServerType().getName());
            mailServeiceDTO.setUser(emailServerVO.getEmailServerAccount());
            mailServeiceDTO.setPassword(emailServerVO.getEmailServerPwd());
            mailServeiceDTO.setPort(emailServerVO.getEmailServerPort());
            MailSenderDTO mailSenderDTO = new MailSenderDTO();
            mailSenderDTO.setUser(emailServerVO.getEmailServerAccount());
            //邮件标题
            mailSenderDTO.setSubject("FiData数据管道运行结果通知");
            //邮件正文
            String body = "";

            mailSenderDTO.setBody(JSON.toJSONString(tableServiceEmail.body));
            //邮件收件人
            mailSenderDTO.setToAddress(email.get(0).userEmails);
            //mailSenderDTO.setToCc("邮件抄送人");
            //mailSenderDTO.setSendAttachment("是否发送附件");
            //mailSenderDTO.setAttachmentName("附件名称");
            //mailSenderDTO.setAttachmentPath("附件地址");
            //mailSenderDTO.setAttachmentActualName("附件实际名称");
            //mailSenderDTO.setCompanyLogoPath("公司logo地址");
            try {
                //第二步：调用邮件发送方法
                log.info("pipelineSendEmails-mailServeiceDTO：" + JSON.toJSONString(mailServeiceDTO));
                log.info("pipelineSendEmails-mailSenderDTO：" + JSON.toJSONString(mailSenderDTO));
                MailSenderUtils.send(mailServeiceDTO, mailSenderDTO);
            } catch (Exception ex) {
                throw new FkException(ResultEnum.ERROR, ex.getMessage());
            }
        } else if (email.get(0).type == 2) //发送企业微信
        {
            //获取企业微信token
            String accessTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + emailServerById.data.wechatCorpId + "&corpsecret=" + emailServerById.data.wechatAppSecret + "";
            String stringAccessToken = HttpGet(accessTokenUrl);
            JSONObject json = JSONObject.parseObject(stringAccessToken);
            String accessToken = json.getString("access_token");
            //取出dispatchEmail.body的key和value值并将key和value拼接成一段 HTML 格式的字符串
            StringBuffer sb = new StringBuffer();
            int i = 0;
            for (Map.Entry<String, String> entry : tableServiceEmail.body.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key).append(": ").append(value);
                //如果是最后一条数据不加br
                if (i != tableServiceEmail.body.size() - 1) {
                    sb.append("<br>");
                }
                i++;
            }
            String content = sb.toString();

            for (TableRecipientsPO user : email) {
                //构造卡片消息内容
                Map<String, Object> params = new HashMap<>();
                params.put("touser", user.wechatUserId);
                params.put("msgtype", "textcard");
                params.put("agentid", emailServerById.data.wechatAgentId.trim());
                Map<String, Object> textcard = new HashMap<>();
                textcard.put("title", "管道告警通知");
                textcard.put("description", content);
                textcard.put("url", tableServiceEmail.url);
                textcard.put("btntxt", "更多");
                params.put("textcard", textcard);
                params.put("enable_id_trans", 0);
                params.put("enable_duplicate_check", 0);
                params.put("duplicate_check_interval", 1800);

                try {
                    //发送企业微信
                    String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;
                    String send = HttpPost(url, JSON.toJSONString(params));
                    JSONObject jsonSend = JSONObject.parseObject(send);
                } catch (Exception e) {
                    log.debug("【pipelineSendEmails】 e：" + e);
                    throw new FkException(ResultEnum.ERROR, e.getMessage());
                }
            }
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 用于远程调用方法的参数，↑
     *
     * @return
     */
    public static KafkaReceiveDTO getKafkaReceive(Long tableId) {
        //拼接所需的topic
        String topic = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.DATASERVICES.getValue() + ".0." + tableId;
        //获取当前时间并格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = formatter.format(LocalDateTime.now());
        //剔除uuid生成字符串里面的"-"符号
        String pipeTaskTraceId = UUID.randomUUID().toString().replace("-", "");
        String fidata_batch_code = UUID.randomUUID().toString().replace("-", "");
        String pipelStageTraceId = UUID.randomUUID().toString().replace("-", "");
        return KafkaReceiveDTO.builder()
                .topic(topic)
                .start_time(dateTime)
                .pipelTaskTraceId(pipeTaskTraceId)
                .fidata_batch_code(fidata_batch_code)
                .pipelStageTraceId(pipelStageTraceId)
                .ifTaskStart(true)
                .topicType(1)
                .build();
    }

    /**
     * 更新表服务数据
     *
     * @param dto
     * @return
     */
    public ResultEnum updateTableService(TableServiceDTO dto) {
        TableServicePO po = this.query().eq("id", dto.id).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        po = TableServiceMap.INSTANCES.dtoToPo(dto);
        po.id = dto.id;
        if (mapper.updateById(po) == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;

    }

    /**
     * 构建参数
     *
     * @param dto
     */
    public BuildTableServiceDTO buildParameter(TableServiceSaveDTO dto) {

        BuildTableServiceDTO data = new BuildTableServiceDTO();
        //表信息
        data.id = dto.tableService.id;
        data.addType = dto.tableService.addType;
        data.dataSourceId = dto.tableService.sourceDbId;
        data.targetDbId = dto.tableService.targetDbId;
        data.tableName = dto.tableService.tableName;
        data.sqlScript = dto.tableService.sqlScript;
        data.targetTable = dto.tableService.targetTable;

        LambdaQueryWrapper<AppServiceConfigPO> configQueryWrapper = new LambdaQueryWrapper<>();
        configQueryWrapper.eq(AppServiceConfigPO::getServiceId, dto.tableService.id);
        AppServiceConfigPO appServiceConfigPO = appServiceConfigMapper.selectOne(configQueryWrapper);

        LambdaQueryWrapper<TableAppPO> appQueryWrapper = new LambdaQueryWrapper<>();
        appQueryWrapper.eq(TableAppPO::getId, appServiceConfigPO.getAppId());
        TableAppPO tableAppPO = tableAppManage.getOne(appQueryWrapper);
        //添加app信息
        data.tableAppId = (int) tableAppPO.getId();
        data.tableAppName = tableAppPO.getAppName();
        data.tableAppDesc = tableAppPO.appDesc;
        if (data.targetTable.indexOf(".") > 1) {
            String[] str = data.targetTable.split("\\.");
            data.schemaName = str[0];
            data.targetTable = str[1];
        }

        //表字段
        data.fieldDtoList = dto.tableFieldList;
        //同步配置
        data.syncModeDTO = dto.tableSyncMode;

        if (StringUtils.isBlank(data.syncModeDTO.customScriptAfter)) {
            data.syncModeDTO.customScriptAfter = "select 'fisk' as fisk";
        }

        if (StringUtils.isBlank(data.syncModeDTO.customScriptBefore)) {
            data.syncModeDTO.customScriptBefore = "select 'fisk' as fisk";
        }

        return data;
    }

}
