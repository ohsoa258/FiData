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
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataEntityDTO;
import com.fisk.datafactory.enums.SendModeEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.dataservice.dto.datasource.DataSourceConfigInfoDTO;
import com.fisk.dataservice.dto.tableapi.TableApiServiceSaveDTO;
import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.dto.tableservice.*;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.ApiStateTypeEnum;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.enums.AppTypeEnum;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.map.DataSourceConMap;
import com.fisk.dataservice.map.TableServiceMap;
import com.fisk.dataservice.mapper.AppServiceConfigMapper;
import com.fisk.dataservice.mapper.TableRecipientsMapper;
import com.fisk.dataservice.mapper.TableServiceMapper;
import com.fisk.dataservice.service.ITableApiParameterService;
import com.fisk.dataservice.service.ITableApiService;
import com.fisk.dataservice.service.ITableService;
import com.fisk.dataservice.vo.tableservice.TableRecipientsVO;
import com.fisk.dataservice.vo.tableservice.WechatUserVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.BuildDeleteTableServiceDTO;
import com.fisk.task.dto.task.BuildTableApiServiceDTO;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Resource
    private ITableApiService tableApiService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    ITableApiParameterService tableApiParameterService;


    @Resource
    DataManageClient dataManageClient;

    @Value("${open-metadata}")
    private Boolean openMetadata;

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

        //同步元数据
        if (openMetadata){
            List<MetaDataEntityDTO> tableSyncMetaDataById = tableAppManage.getTableSyncMetaDataById(dto.getTableService().getId());
            dataManageClient.syncDataConsumptionMetaData(tableSyncMetaDataById);
        }
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
        data.tableSyncMode = tableSyncMode.getTableServiceSyncMode(id, AppServiceTypeEnum.TABLE.getValue());
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

        //删除元数据
        if (openMetadata){
            List<MetaDataEntityDTO> tableSyncMetaDataById = tableAppManage.getTableSyncMetaDataById(id);
            dataManageClient.deleteConsumptionMetaData(tableSyncMetaDataById);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<BuildTableServiceDTO> getTableListByPipelineId(Integer pipelineId) {
        List<Integer> tableListByPipelineId = tableSyncMode.getTableListByPipelineId(pipelineId, 2);
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
    public List<BuildTableServiceDTO> getTableListByInputId(Integer inputId) {
        List<Integer> tableListByPipelineId = tableSyncMode.getTableListByInputId(inputId,2);
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
    public ResultEnum editTableServiceSync(TableServiceSyncDTO tableServiceSyncDTO) {
        TableServicePO tableServicePO = mapper.selectById(tableServiceSyncDTO.getTableId());
        //判断表状态是否已发布
        if (tableServicePO.getPublish() != 1) {
            log.info("手动同步失败，原因：表未发布");
            return ResultEnum.TABLE_NOT_PUBLISHED;
        }
        //获取远程调用接口中需要的参数KafkaReceiveDTO
        KafkaReceiveDTO kafkaReceiveDTO = getKafkaReceive(tableServiceSyncDTO);
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
            tableRecipientsVO.setEnable(tableRecipientsPOList.get(0).getEnable());
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
            tableRecipientsPO.setEnable(dto.enable);
            tableRecipientsPOS.add(tableRecipientsPO);
        } else if (dto.getNoticeServerType() == 2 && CollectionUtils.isNotEmpty(dto.getWechatUserList())) {
            dto.getWechatUserList().forEach(t -> {
                TableRecipientsPO tableRecipientsPO = new TableRecipientsPO();
                tableRecipientsPO.setTableAppId(dto.getTableAppId());
                tableRecipientsPO.setNoticeServerId(dto.getNoticeServerId());
                tableRecipientsPO.setAlarmConditions(dto.getAlarmConditions());
                tableRecipientsPO.setWechatUserId(t.getWechatUserId());
                tableRecipientsPO.setWechatUserName(t.getWechatUserName());
                tableRecipientsPO.setEnable(dto.enable);
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
        queryWrapper.eq(TableRecipientsPO::getTableAppId, tableServiceEmail.appId);
        boolean remove = tableRecipientsManage.remove(queryWrapper);
        if (remove) {
            return ResultEnum.SUCCESS;
        } else {
            return ResultEnum.DELETE_ERROR;
        }
    }

    @Override
    public ResultEnum tableServiceSendEmails(TableServiceEmailDTO tableServiceEmail) {
        String serviceName = null;
        TableAppPO tableAppPO = null;
        if (tableServiceEmail.appType == 1) {
            serviceName = "数据分发表";
            //获取单个管道配置
            tableAppPO = tableAppManage.getById(tableServiceEmail.appId);
            if (tableAppPO != null) {
                tableServiceEmail.body.put(serviceName + "服务名称", tableAppPO.getAppName());
            }
            String tableId = tableServiceEmail.body.get("表名");
            LambdaQueryWrapper<TableServicePO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TableServicePO::getId, tableId);
            TableServicePO tableServicePO = this.getOne(queryWrapper);
            tableServiceEmail.body.put("表名", tableServicePO.getTableName());
        } else if (tableServiceEmail.appType == 2) {
            serviceName = "数据分发api";
            //获取单个管道配置
            tableAppPO = tableAppManage.getById(tableServiceEmail.appId);
            if (tableAppPO != null) {
                tableServiceEmail.body.put(serviceName + "服务名称", tableAppPO.getAppName());
            }
            String tableId = tableServiceEmail.body.get("表名");
            LambdaQueryWrapper<TableApiServicePO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TableApiServicePO::getId, tableId);
            TableApiServicePO tableApiServicePO = tableApiService.getOne(queryWrapper);
            tableServiceEmail.body.put("api名称", tableApiServicePO.getApiName());
        }

        // 发邮件
        List<TableRecipientsPO> email = tableRecipientsManage.query().eq("table_app_id", tableServiceEmail.appId).list();
        //第一步：查询邮件服务器设置
        if (!CollectionUtils.isNotEmpty(email)) {
            return ResultEnum.ERROR;
        }
        if (email.get(0).enable == 2) {
            return ResultEnum.SUCCESS;
        }
        if (email.get(0).enable != 1) {
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
            mailSenderDTO.setSubject(String.format("FiData" + serviceName + "服务(%s)运行结果通知", tableAppPO.getAppName()));
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
                textcard.put("title", serviceName + "服务告警通知");
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

    @Override
    public List<String> getTableName() {
        LambdaQueryWrapper<TableServicePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(TableServicePO::getTableName);
        List<TableServicePO> tableServicePOS = mapper.selectList(queryWrapper);
        List<String> tableNames = tableServicePOS.stream().map(TableServicePO::getTableName).distinct().collect(Collectors.toList());
        return tableNames;
    }

    /**
     * 启用或禁用
     *
     * @param id
     * @return
     */
    @Override
    public ResultEnum enableOrDisable(Integer id) {
        TableServicePO tableServicePO = this.getById(id);
        TableServiceDTO tableServiceDTO = TableServiceMap.INSTANCES.poToDto(tableServicePO);
        ResultEntity<TableServiceDTO> result = publishTaskClient.enableOrDisable(tableServiceDTO);
        if (result.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        } else {
            TableServiceDTO data = result.getData();
            TableServicePO tableService = TableServiceMap.INSTANCES.dtoToPo(data);
            if (mapper.updateById(tableService) == 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<FiDataMetaDataDTO> getDataServiceStructure(FiDataMetaDataReqDTO dto) {
        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildFiDataStructureKey(dto.dataSourceId));
        if (!flag) {
            // 将数据接入结构存入redis
            setDataServiceStructure(dto);
        }
        List<FiDataMetaDataDTO> list = null;
        String dataAccessStructure = redisUtil.get(RedisKeyBuild.buildFiDataStructureKey(dto.dataSourceId)).toString();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dataAccessStructure)) {
            list = JSONObject.parseArray(dataAccessStructure, FiDataMetaDataDTO.class);
        }
        return list;
    }

    @Override
    public List<FiDataMetaDataTreeDTO> getDataServiceTableStructure(FiDataMetaDataReqDTO dto) {
        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildFiDataTableStructureKey(dto.dataSourceId));
        if (!flag) {
            // 将数据接入结构存入redis
            setDataServiceStructure(dto);
        }
        List<FiDataMetaDataTreeDTO> list = null;
        String dataAccessStructure = redisUtil.get(RedisKeyBuild.buildFiDataTableStructureKey(dto.dataSourceId)).toString();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dataAccessStructure)) {
            list = JSONObject.parseArray(dataAccessStructure, FiDataMetaDataTreeDTO.class);
        }
        return list;
    }

    @Override
    public boolean setDataServiceStructure(FiDataMetaDataReqDTO reqDto) {
        List<FiDataMetaDataDTO> list = new ArrayList<>();
        FiDataMetaDataDTO dto = new FiDataMetaDataDTO();
        // FiData数据源id: 数据资产自定义
        dto.setDataSourceId(Integer.parseInt(reqDto.dataSourceId));

        // 第一层id
        List<FiDataMetaDataTreeDTO> dataTreeList = new ArrayList<>();
        FiDataMetaDataTreeDTO dataTree = new FiDataMetaDataTreeDTO();
        dataTree.setId(reqDto.dataSourceId);
        dataTree.setParentId("-10");
        dataTree.setLabel(reqDto.dataSourceName);
        dataTree.setLabelAlias(reqDto.dataSourceName);
        dataTree.setLevelType(LevelTypeEnum.DATABASE);
        dataTree.setSourceType(1);
        dataTree.setSourceId(Integer.parseInt(reqDto.dataSourceId));

        // 封装data所有结构数据
        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = buildChildren(reqDto.dataSourceId);
        Map.Entry<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> next = hashMap.entrySet().iterator().next();
        dataTree.setChildren(next.getValue());
        dataTreeList.add(dataTree);

        dto.setChildren(dataTreeList);
        list.add(dto);

        if (!org.springframework.util.CollectionUtils.isEmpty(list)) {
            redisUtil.set(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId), JSON.toJSONString(list));
        }
        List<FiDataMetaDataTreeDTO> key = next.getKey();
        if (!org.springframework.util.CollectionUtils.isEmpty(key)) {
            String s = JSON.toJSONString(key);
            redisUtil.set(RedisKeyBuild.buildFiDataTableStructureKey(reqDto.dataSourceId), s);
        }
        return true;
    }

    /**
     * 构建data-access子集树
     *
     * @param id FiData数据源id
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @author Lock
     * @date 2022/6/15 17:46
     */
    private HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> buildChildren(String id) {

        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = new HashMap<>();

        List<FiDataMetaDataTreeDTO> appTypeTreeList = new ArrayList<>();

        FiDataMetaDataTreeDTO appTreeByRealTime = new FiDataMetaDataTreeDTO();
        String appTreeByRealTimeGuid = UUID.randomUUID().toString().replace("-", "");;
        appTreeByRealTime.setId(appTreeByRealTimeGuid);
        appTreeByRealTime.setParentId(id);
        appTreeByRealTime.setLabel("数据表");
        appTreeByRealTime.setLabelAlias("数据表");
        appTreeByRealTime.setLevelType(LevelTypeEnum.FOLDER);
        appTreeByRealTime.setSourceType(1);
        appTreeByRealTime.setSourceId(Integer.parseInt(id));

        FiDataMetaDataTreeDTO appTreeByNonRealTime = new FiDataMetaDataTreeDTO();
        String appTreeByNonRealTimeGuid = UUID.randomUUID().toString().replace("-", "");;
        appTreeByNonRealTime.setId(appTreeByNonRealTimeGuid);
        appTreeByNonRealTime.setParentId(id);
        appTreeByNonRealTime.setLabel("API");
        appTreeByNonRealTime.setLabelAlias("API");
        appTreeByNonRealTime.setLevelType(LevelTypeEnum.FOLDER);
        appTreeByNonRealTime.setSourceType(1);
        appTreeByNonRealTime.setSourceId(Integer.parseInt(id));

        // 所有应用
        List<TableAppPO> tableAppPOS = tableAppManage.query().orderByDesc("create_time").list();
        // 所有应用下表字段信息
        List<FiDataMetaDataTreeDTO> tableFieldList = new ArrayList<>();
        List<TableAppPO> tableApps = tableAppPOS.stream().filter(i -> i.getAppType() == AppTypeEnum.TABLE_TYPE.getValue()).collect(Collectors.toList());
        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> fiDataMetaDataTreeByRealTime = getFiDataMetaDataTreeByTable(appTreeByRealTimeGuid, id, tableApps);
        Map.Entry<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> nextTreeByRealTime = fiDataMetaDataTreeByRealTime.entrySet().iterator().next();
        appTreeByRealTime.setChildren(nextTreeByRealTime.getValue());
        tableFieldList.addAll(nextTreeByRealTime.getKey());

        List<TableAppPO> tableApis = tableAppPOS.stream().filter(i -> i.getAppType() == AppTypeEnum.API_TYPE.getValue()).collect(Collectors.toList());
        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> fiDataMetaDataTreeByNonRealTime = getFiDataMetaDataTreeByApi(appTreeByNonRealTimeGuid, id, tableApis);
        Map.Entry<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> nextTreeByNonRealTime = fiDataMetaDataTreeByNonRealTime.entrySet().iterator().next();
        appTreeByNonRealTime.setChildren(nextTreeByNonRealTime.getValue());
        tableFieldList.addAll(nextTreeByNonRealTime.getKey());

        appTypeTreeList.add(appTreeByRealTime);
        appTypeTreeList.add(appTreeByNonRealTime);

        // key是表字段 value是tree
        hashMap.put(tableFieldList, appTypeTreeList);
        return hashMap;
    }

    /**
     * 获取实时应用结构
     *
     * @param appPoList 所有的应用实体对象
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @author Lock
     * @date 2022/6/16 15:21
     * @params id FiData数据源id
     */
    private HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> getFiDataMetaDataTreeByTable(String appTreeByTableGuid, String id, List<TableAppPO> appPoList) {
        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = new HashMap<>();
        List<FiDataMetaDataTreeDTO> key = new ArrayList<>();
        List<FiDataMetaDataTreeDTO> value = appPoList.stream().filter(Objects::nonNull)
                // 表服务
                .filter(e -> e.appType == 1).map(app -> {

                    // 第一层: app层
                    FiDataMetaDataTreeDTO appDtoTree = new FiDataMetaDataTreeDTO();
                    // 当前层默认生成的uuid
                    String uuid_appId = UUID.randomUUID().toString().replace("-", "");
                    appDtoTree.setId(uuid_appId); //String.valueOf(app.id)
                    // 上一级的id
                    appDtoTree.setSourceType(1);
                    appDtoTree.setSourceId(Integer.parseInt(id));
                    appDtoTree.setParentId(appTreeByTableGuid);
                    appDtoTree.setLabel(app.appName);
                    appDtoTree.setLabelAlias(app.appName);
                    appDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                    appDtoTree.setLabelDesc(app.appDesc);

                    LambdaQueryWrapper<AppServiceConfigPO> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(AppServiceConfigPO::getAppId, app.id);
                    List<AppServiceConfigPO> appServiceConfigPOS = appServiceConfigMapper.selectList(queryWrapper);
                    if (CollectionUtils.isNotEmpty(appServiceConfigPOS)){
                        List<Integer> tableServiceIds = appServiceConfigPOS.stream().map(AppServiceConfigPO::getServiceId).collect(Collectors.toList());
                        // 当前app下的所有table
                        List<FiDataMetaDataTreeDTO> tableTreeList = this.query().in("id", tableServiceIds).orderByDesc("create_time").list().stream().filter(Objects::nonNull).map(table -> {
                            FiDataMetaDataTreeDTO tableDtoTree = new FiDataMetaDataTreeDTO();
                            tableDtoTree.setId(String.valueOf(table.id));// String.valueOf(api.id)
                            tableDtoTree.setParentId(uuid_appId); // String.valueOf(app.id)
                            tableDtoTree.setLabel(table.tableName);
                            tableDtoTree.setLabelAlias(table.displayName);
                            tableDtoTree.setSourceType(1);
                            tableDtoTree.setSourceId(Integer.parseInt(id));
                            tableDtoTree.setLevelType(LevelTypeEnum.TABLE);
                            tableDtoTree.setLabelBusinessType(TableBusinessTypeEnum.DATA_SERVICE_TABLE.getValue());
                            // 不是已发布的都当作未发布处理
                            if (table.publish == null) {
                                tableDtoTree.setPublishState("0");
                            } else {
                                tableDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                            }
                            tableDtoTree.setLabelDesc(table.tableDes);
                            // 第四层: field层
                            List<FiDataMetaDataTreeDTO> fieldTreeList = tableField.query().eq("table_service_id", table.id).list().stream().filter(Objects::nonNull).map(field -> {

                                FiDataMetaDataTreeDTO fieldDtoTree = new FiDataMetaDataTreeDTO();
                                fieldDtoTree.setId(String.valueOf(field.id));
                                fieldDtoTree.setParentId(String.valueOf(table.id));
                                fieldDtoTree.setLabel(field.fieldName);
                                fieldDtoTree.setLabelAlias(field.fieldName);
                                fieldDtoTree.setLevelType(LevelTypeEnum.FIELD);
                                fieldDtoTree.setPublishState(String.valueOf(table.publish != 1 ? 0 : 1));
                                fieldDtoTree.setLabelLength(String.valueOf(field.fieldLength));
                                fieldDtoTree.setLabelType(field.fieldType);
                                fieldDtoTree.setLabelDesc(field.fieldDes);
                                fieldDtoTree.setSourceType(1);
                                fieldDtoTree.setSourceId(Integer.parseInt(id));
                                fieldDtoTree.setParentName(table.tableName);
                                fieldDtoTree.setParentNameAlias(table.tableName);
                                fieldDtoTree.setParentLabelRelName(table.tableName);
                                fieldDtoTree.setParentLabelFramework(null);
                                fieldDtoTree.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                return fieldDtoTree;
                            }).collect(Collectors.toList());

                            // table的子级
                            tableDtoTree.setChildren(fieldTreeList);
                            return tableDtoTree;
                        }).collect(Collectors.toList());
                        // 表字段信息单独再保存一份
                        if (!org.springframework.util.CollectionUtils.isEmpty(tableTreeList)) {
                            key.addAll(tableTreeList);
                        }
                        // app的子级
                        appDtoTree.setChildren(tableTreeList);
                    }
                    return appDtoTree;
                }).collect(Collectors.toList());
        hashMap.put(key, value);
        return hashMap;
    }

    /**
     * 获取非实时应用结构
     *
     * @param id        guid
     * @param appPoList 所有的应用实体对象
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @author Lock
     * @date 2022/6/16 15:21
     */
    private HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> getFiDataMetaDataTreeByApi(String appTreeByApiGuid, String id, List<TableAppPO> appPoList) {
        HashMap<List<FiDataMetaDataTreeDTO>, List<FiDataMetaDataTreeDTO>> hashMap = new HashMap<>();
        List<FiDataMetaDataTreeDTO> key = new ArrayList<>();
        List<FiDataMetaDataTreeDTO> value = appPoList.stream().filter(Objects::nonNull)
                // api服务
                .filter(e -> e.appType == 2).map(app -> {

                    // 第一层: app层
                    FiDataMetaDataTreeDTO appDtoTree = new FiDataMetaDataTreeDTO();
                    // 当前层默认生成的uuid
                    String uuid_appId = UUID.randomUUID().toString().replace("-", "");
                    appDtoTree.setId(uuid_appId); //String.valueOf(app.id)
                    // 上一级的id
                    appDtoTree.setSourceType(1);
                    appDtoTree.setSourceId(Integer.parseInt(id));
                    appDtoTree.setParentId(appTreeByApiGuid);
                    appDtoTree.setLabel(app.appName);
                    appDtoTree.setLabelAlias(app.appName);
                    appDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                    appDtoTree.setLabelDesc(app.appDesc);
                    // 当前app下的所有api
                    List<FiDataMetaDataTreeDTO> apiTreeList = tableApiService.query().eq("app_id", app.id).orderByDesc("create_time").list().stream().filter(Objects::nonNull).map(api -> {
                        FiDataMetaDataTreeDTO apiDtoTree = new FiDataMetaDataTreeDTO();
                        apiDtoTree.setId(String.valueOf(api.id)); // String.valueOf(api.id)
                        apiDtoTree.setParentId(uuid_appId); // String.valueOf(app.id)
                        apiDtoTree.setLabel(api.getApiName());
                        apiDtoTree.setLabelAlias(api.getApiName());
                        apiDtoTree.setSourceType(1);
                        apiDtoTree.setSourceId(Integer.parseInt(id));
                        apiDtoTree.setLevelType(LevelTypeEnum.FOLDER);
                        apiDtoTree.setLabelBusinessType(TableBusinessTypeEnum.DATA_SERVICE_API.getValue());
                        // 不是已发布的都当作未发布处理
                        if (api.getPublish() == null) {
                            apiDtoTree.setPublishState("0");
                        } else {
                            apiDtoTree.setPublishState(String.valueOf(api.getPublish() != 1 ? 0 : 1));
                        }
                        apiDtoTree.setLabelDesc(api.getApiDes());

                        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(TableApiParameterPO::getApiId,api.id);
                        List<TableApiParameterPO> apiParameters = tableApiParameterService.list(queryWrapper);
                        List<TableApiParameterPO> apiParameterPOS = apiParameters.stream().filter(i -> i.getSelected() == 1).collect(Collectors.toList());
                        apiParameters = apiParameters.stream().filter(i->i.getPid() == apiParameterPOS.get(0).getId()).collect(Collectors.toList());
                        ;
                        // 第四层: field层
                        List<FiDataMetaDataTreeDTO> fieldTreeList = apiParameters.stream().filter(Objects::nonNull).map(field -> {

                            FiDataMetaDataTreeDTO fieldDtoTree = new FiDataMetaDataTreeDTO();
                            fieldDtoTree.setId(String.valueOf(field.id));
                            fieldDtoTree.setParentId(String.valueOf(api.id));
                            fieldDtoTree.setLabel(field.getParameterName());
                            fieldDtoTree.setLabelAlias(field.getParameterName());
                            fieldDtoTree.setLevelType(LevelTypeEnum.FIELD);
                            fieldDtoTree.setPublishState(String.valueOf(api.getPublish() != 1 ? 0 : 1));
                            if (field.getParameterType() == JsonTypeEnum.NUMBER.getValue()){
                                fieldDtoTree.setLabelType("INT");
                            }else if(field.getParameterType() == JsonTypeEnum.STRING.getValue()){
                                fieldDtoTree.setLabelType("NVARCHAR");
                            }
                            fieldDtoTree.setLabelDesc(field.getParameterName());
                            fieldDtoTree.setSourceType(1);
                            fieldDtoTree.setSourceId(Integer.parseInt(id));
                            fieldDtoTree.setParentName(api.getApiName());
                            fieldDtoTree.setParentNameAlias(api.getApiName());
                            fieldDtoTree.setParentLabelRelName(api.getApiName());
                            fieldDtoTree.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                            return fieldDtoTree;
                        }).collect(Collectors.toList());
                        // api的子级
                        apiDtoTree.setChildren(fieldTreeList);
                        return apiDtoTree;
                    }).collect(Collectors.toList());
                    // app的子级
                    appDtoTree.setChildren(apiTreeList);
                    // 表字段信息单独再保存一份
                    if (!org.springframework.util.CollectionUtils.isEmpty(apiTreeList)) {
                        key.addAll(apiTreeList);
                    }
                    return appDtoTree;
                }).collect(Collectors.toList());
        hashMap.put(key, value);
        return hashMap;
    }

    /**
     * 用于远程调用方法的参数，↑
     *
     * @return
     */
    public static KafkaReceiveDTO getKafkaReceive(TableServiceSyncDTO dto) {

        //拼接所需的topic
        String topic = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.DATASERVICES.getValue() + "." + dto.appId + "." + dto.tableId;
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
        dto.enable = 1;
        TableServicePO po = this.query().eq("id", dto.id).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        po = TableServiceMap.INSTANCES.dtoToPo(dto);
        po.id = dto.id;
        mapper.updateById(po);
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
        data.enable = dto.tableService.enable;

        LambdaQueryWrapper<AppServiceConfigPO> configQueryWrapper = new LambdaQueryWrapper<>();
        configQueryWrapper.eq(AppServiceConfigPO::getAppId, dto.tableService.tableAppId);
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
