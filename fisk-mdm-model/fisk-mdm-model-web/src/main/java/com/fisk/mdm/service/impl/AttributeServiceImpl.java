package com.fisk.mdm.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.dataaccess.dto.tablefield.CAndLDTO;
import com.fisk.dataaccess.dto.tablefield.ClassificationsAndLevelsDTO;
import com.fisk.dataaccess.dto.tablefield.TableFieldDTO;
import com.fisk.dataaccess.enums.tablefield.DataClassificationEnum;
import com.fisk.dataaccess.enums.tablefield.DataLevelEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamanagement.dto.standards.StandardsBeCitedDTO;
import com.fisk.mdm.dto.attribute.*;
import com.fisk.mdm.dto.attributelog.AttributeLogSaveDTO;
import com.fisk.mdm.entity.AttributeGroupDetailsPO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.enums.*;
import com.fisk.mdm.map.AttributeMap;
import com.fisk.mdm.map.EntityMap;
import com.fisk.mdm.mapper.AttributeGroupDetailsMapper;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.service.AttributeLogService;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.EventLogService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityMsgVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import com.fisk.task.client.PublishTaskClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author WangYan
 * @date 2022/4/5 14:49
 */
@Slf4j
@Service
public class AttributeServiceImpl extends ServiceImpl<AttributeMapper, AttributePO> implements AttributeService {

    @Resource
    EventLogService logService;

    @Resource
    private UserClient userClient;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private EntityMapper entityMapper;

    @Resource
    private EntityService entityService;

    @Resource
    private UserHelper userHelper;

    @Resource
    AttributeGroupDetailsMapper groupDetailsMapper;

    @Resource
    DataManageClient dataManageClient;

    @Resource
    AttributeLogService attributeLogService;
    @Value("${poi.appkey}")
    private String appKey;
    @Value("${poi.secret}")
    private String secret;
    @Value("${poi.tokenUrl}")
    private String getTokenUrl;
    @Value("${poi.listUrl}")
    private String getPoiUrl;

    @Value("${poi.authUrl}")
    private String authUrl;

    @Value("${open-metadata}")
    private Boolean openMetadata;

    @Value("${fiData-data-mdm-source}")
    private Integer mdmSource;

    @Override
    public ResultEntity<AttributeVO> getById(Integer id) {
        AttributeVO attributeVO = AttributeMap.INSTANCES.poToVo(baseMapper.selectById(id));
        if (Objects.isNull(attributeVO)) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询出模型id
        QueryWrapper<EntityPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(EntityPO::getId, attributeVO.getEntityId());
        EntityPO entityPO = entityMapper.selectOne(queryWrapper);
        if (entityPO != null){
            attributeVO.setModelId(entityPO.getModelId());
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, attributeVO);
    }

    /**
     * 添加数据
     *
     * @param attributeDTO 属性dto
     * @return {@link ResultEnum}
     */
    @Override
    public ResultEnum addData(AttributeDTO attributeDTO) {
        if (attributeDTO.getEntityId() == null){
            return ResultEnum.PARAMTER_ERROR;
        }
        //判断同实体下是否存在重复名称
        QueryWrapper<AttributePO> attributeWrapper = new QueryWrapper<>();
        attributeWrapper.eq("name", attributeDTO.getName())
                .eq("entity_id", attributeDTO.getEntityId())
                .last("limit 1");
        if (baseMapper.selectOne(attributeWrapper) != null) {
            return ResultEnum.NAME_EXISTS;
        }

        //转换数据
        AttributePO attributePo = AttributeMap.INSTANCES.dtoToPo(attributeDTO);

        //若数据不为浮点型，设置精度为null
        if(attributePo.getDataType() != DataTypeEnum.FLOAT){
            attributePo.setDataTypeDecimalLength(null);
        }

        //若数据类型不为浮点型或文本，数据长度设置为null
        if (attributePo.getDataType() != DataTypeEnum.TEXT &&
                attributePo.getDataType() != DataTypeEnum.FLOAT ){
            attributePo.setDataTypeLength(null);
        }
        if (attributePo.getDataType() == DataTypeEnum.POI){
            attributePo.setDataTypeLength(null);
        }
        //若数据类型为“域字段”类型，维护“域字段id”字段
        if(attributePo.getDataType() == DataTypeEnum.DOMAIN){
            //判断用户是否填入域字段关联id
            if(attributePo.getDomainId() == null){
                return ResultEnum.PARAMTER_ERROR;
            }
            EntityPO entity = entityMapper.selectById(attributePo.getDomainId());
            //判断该id是否存在实体
            if(entity == null){
                return ResultEnum.DATA_NOTEXISTS;
            }
            //查询关联实体下名称为code的属性
            QueryWrapper<AttributePO> codeWrapper = new QueryWrapper<>();
            codeWrapper.lambda().eq(AttributePO::getName,"code")
                    .eq(AttributePO::getEntityId,entity.getId())
                    .last("limit 1");
            AttributePO codeAttribute = baseMapper.selectOne(codeWrapper);
            if(codeAttribute == null){
                return ResultEnum.DATA_NOTEXISTS;
            }
            attributePo.setDomainId((int)codeAttribute.getId());
        }else{
            //若数据类型不为“域字段”，将域字段id置空
            attributePo.setDomainId(null);
        }

        // 如果不是经纬度坐标类型,地图类型不会有值
        if(!attributePo.getDataType().equals(DataTypeEnum.LATITUDE_COORDINATE)){
            attributePo.setMapType(null);
        }

        //添加数据
        attributePo.setStatus(AttributeStatusEnum.INSERT);
        attributePo.setSyncStatus(AttributeSyncStatusEnum.NOT_PUBLISH);

        if (baseMapper.insert(attributePo) <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 添加到属性组
        AttributeGroupDetailsPO detailsPo = new AttributeGroupDetailsPO();
        detailsPo.setEntityId(attributeDTO.getEntityId());
        detailsPo.setAttributeId((int)attributePo.getId());
        attributeDTO.getAttributeGroupId().stream()
                        .forEach(e -> {
                            detailsPo.setGroupId(e);
                            groupDetailsMapper.insert(detailsPo);
                        });

        // 添加到属性日志表
        AttributeLogSaveDTO attributeLogSaveDto = AttributeMap.INSTANCES.poToLogDto(attributePo);
        attributeLogSaveDto.setAttributeId((int)attributePo.getId());
        attributeLogService.saveAttributeLog(attributeLogSaveDto);

        // 记录日志
        String desc = "新增一个属性,id:" + attributePo.getId();
        logService.saveEventLog((int) attributePo.getId(), ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.SAVE, desc);

        //创建成功
        return ResultEnum.SUCCESS;
    }

    /**
     * 编辑数据
     *
     * @param attributeUpdateDTO 属性更新dto
     * @return {@link ResultEnum}
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(AttributeUpdateDTO attributeUpdateDTO) {
        AttributePO attributePo = baseMapper.selectById(attributeUpdateDTO.getId());

        // 添加到属性日志表
        AttributeLogSaveDTO attributeLogSaveDto = AttributeMap.INSTANCES.dtoToLogDto(attributeUpdateDTO);
        attributeLogSaveDto.setAttributeId((int)attributePo.getId());
        attributeLogService.saveAttributeLog(attributeLogSaveDto);

        //判断数据是否存在
        if (attributePo == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //判断修改后的名称是否存在
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", attributeUpdateDTO.getName())
                .ne("id", attributeUpdateDTO.getId())
                .eq("entity_id",attributeUpdateDTO.getEntityId())
                .last("limit 1");
        if ( baseMapper.selectOne(wrapper) != null) {
            return ResultEnum.NAME_EXISTS;
        }

        //维护历史的状态字段，防止保持状态为新增失效
        if(!Objects.isNull(attributePo.getStatus())) {
            attributeUpdateDTO.setStatus(attributePo.getStatus().getValue());
        }

        //把DTO转化到查询出来的PO上
        attributePo = AttributeMap.INSTANCES.updateDtoToPo(attributeUpdateDTO);

        LambdaUpdateWrapper<AttributePO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AttributePO::getId,attributePo.getId());
        //若修改后数据类型不为浮点型，将数据小数点长度修改为null
        if(!Objects.isNull(attributePo.getDataType()) && attributePo.getDataType() != DataTypeEnum.FLOAT){
            updateWrapper.set(AttributePO::getDataTypeDecimalLength,null);
        }

        //若数据类型不为浮点型或文本，数据长度设置为null
        if (attributePo.getDataType() != DataTypeEnum.TEXT &&
                attributePo.getDataType() != DataTypeEnum.FLOAT){
            updateWrapper.set(AttributePO::getDataTypeLength,null);
        }

        //若数据类型为“域字段”类型，维护“域字段id”字段
        if(attributePo.getDataType() == DataTypeEnum.DOMAIN){
            //判断用户是否填入域字段关联id
            if(attributePo.getDomainId() == null){
                return ResultEnum.PARAMTER_ERROR;
            }
            EntityPO entity = entityMapper.selectById(attributePo.getDomainId());
            //判断该id是否存在实体
            if(entity == null){
                return ResultEnum.DATA_NOTEXISTS;
            }
            //查询关联实体下名称为code的属性
            QueryWrapper<AttributePO> codeWrapper = new QueryWrapper<>();
            codeWrapper.lambda().eq(AttributePO::getName,"code")
                    .eq(AttributePO::getEntityId,entity.getId())
                    .last("limit 1");
            AttributePO codeAttribute = baseMapper.selectOne(codeWrapper);
            if(codeAttribute == null){
                return ResultEnum.DATA_NOTEXISTS;
            }
            updateWrapper.set(AttributePO::getDomainId,codeAttribute.getId());
        }else{
            //若数据类型不为“域字段”，将域字段id置空
            updateWrapper.set(AttributePO::getDomainId,null);
        }

        // 判断特殊字符是否有变动
        boolean fieldChanges = this.isSpecialFieldChanges(attributeUpdateDTO);
        if (fieldChanges == false){
            //如果历史的状态是新增,保持状态为新增
            attributePo.setStatus(attributePo.getStatus() == AttributeStatusEnum.INSERT ?
                    AttributeStatusEnum.INSERT : AttributeStatusEnum.UPDATE);
            //修改数据
            attributePo.setSyncStatus(AttributeSyncStatusEnum.NOT_PUBLISH);
        }
        if (baseMapper.update(attributePo,updateWrapper) <= 0) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 删除属性组中的属性
        QueryWrapper<AttributeGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributeGroupDetailsPO::getAttributeId,attributeUpdateDTO.getId());
        groupDetailsMapper.delete(queryWrapper);

        // 添加到属性组
        AttributeGroupDetailsPO detailsPo = new AttributeGroupDetailsPO();
        detailsPo.setEntityId(attributeUpdateDTO.getEntityId());
        detailsPo.setAttributeId((int)attributePo.getId());
        attributeUpdateDTO.getAttributeGroupId().stream()
                .forEach(e -> {
                    detailsPo.setGroupId(e);
                    groupDetailsMapper.insert(detailsPo);
                });


        // 记录日志
        String desc = "修改一个属性,id:" + attributeUpdateDTO.getId();
        logService.saveEventLog((int) attributePo.getId(), ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.UPDATE, desc);

        //添加成功
        return ResultEnum.SUCCESS;
    }

    /**
     * 判断特殊字符
     * @param attributeUpdateDTO
     * @return
     */
    public boolean isSpecialFieldChanges(AttributeUpdateDTO attributeUpdateDTO){
        AttributePO attributePo = baseMapper.selectById(attributeUpdateDTO.getId());
        AttributeSpecialDTO specialDto = AttributeMap.INSTANCES.specialPoToDto(attributePo);
        AttributeSpecialDTO attributeSpecialDto = AttributeMap.INSTANCES.specialDtoToDto(attributeUpdateDTO);
        if (attributeSpecialDto.equals(specialDto)){
            return true;
        }

        return false;
    }

    @Override
    public Page<AttributeVO> getAll(AttributeQueryDTO query) {

        Page<AttributeVO> voPage = baseMapper.getAll(query.page, query);

        //部分字段枚举类型转换
        Page<AttributePageDTO> dtoPage = AttributeMap.INSTANCES.voToPageDtoPage(voPage);
        Page<AttributePO> poPage = AttributeMap.INSTANCES.pageDtoToPoPage(dtoPage);
        Page<AttributeVO> all = AttributeMap.INSTANCES.poToVoPage(poPage);

        //获取创建人名称
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            ReplenishUserInfo.replenishUserName(all.getRecords(), userClient, UserFieldEnum.USER_ACCOUNT);
        }

        return all;
    }

    /**
     * 发布未发布的属性
     *
     * @return {@link List}<{@link AttributePO}>
     */
    @Override
    public ResultEntity<ResultEnum> getNotSubmittedData(Integer entityId) {

        //查询实体是否存在
        if (Objects.isNull(entityId) || entityService.getDataById(entityId) == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        //查询实体下是否存在可发布的属性
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_id", entityId)
                .ne("status",AttributeStatusEnum.SUBMITTED);
        List<AttributePO> attributePoList = baseMapper.selectList(wrapper);
        if ( CollectionUtils.isEmpty(attributePoList)) {
            return ResultEntityBuild.build(ResultEnum.NO_DATA_TO_SUBMIT);
        }

        //发布
        com.fisk.task.dto.model.EntityDTO entityDTO = new com.fisk.task.dto.model.EntityDTO();
        entityDTO.setEntityId(entityId);
        entityDTO.setUserId(userHelper.getLoginUserInfo().getId());
        if (publishTaskClient.createBackendTable(entityDTO).getCode() != ResultEnum.SUCCESS.getCode()) {
            return ResultEntityBuild.build(ResultEnum.DATA_SUBMIT_ERROR);
        }

        //同步主数据实体属性元数据信息
        if(openMetadata){
            List<MetaDataInstanceAttributeDTO> masterDataMetaData = entityService.getMasterDataMetaData(entityId);
            dataManageClient.consumeMetaData(masterDataMetaData);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS);
    }


    /**
     * 获取实体ER图信息
     *
     * @return {@link List}<{@link EntityMsgVO}>
     */
    @Override
    public List<EntityMsgVO> getEntityMsg() {
        return baseMapper.getER();
    }

    @Override
    public ResultEntity<List<AttributeInfoDTO>> getByIds(List<Integer> ids) {
        List<AttributeInfoDTO> list = AttributeMap.INSTANCES.poToVoList(baseMapper.selectBatchIds(ids));
        if (Objects.isNull(list)) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询出模型id
        List<AttributeInfoDTO> collect = list.stream().filter(e -> e.getEntityId() != null).map(e -> {
            QueryWrapper<EntityPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(EntityPO::getId, e.getEntityId());
            EntityPO entityPO = entityMapper.selectOne(queryWrapper);
            e.setModelId(entityPO.getModelId());
            return e;
        }).collect(Collectors.toList());
        return ResultEntityBuild.build(ResultEnum.SUCCESS, collect);
    }

    @Override
    public AttributeInfoDTO getByDomainId(AttributeDomainDTO dto) {
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributePO::getEntityId,dto.getEntityId())
                .eq(AttributePO::getDomainId,dto.getDomainId())
                .last("limit 1");

        AttributePO attributePO = baseMapper.selectOne(queryWrapper);
        return attributePO == null ? null : AttributeMap.INSTANCES.poToInfoDto(attributePO);
    }

    @Override
    public ResultEnum updateStatus(AttributeStatusDTO statusDto) {
        AttributePO attributePO = baseMapper.selectById(statusDto.getId());
        if (attributePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        AttributePO statusPo = EntityMap.INSTANCES.dtoToStatusPo(statusDto);
        int res = baseMapper.updateById(statusPo);
        return res == 0 ? ResultEnum.SAVE_DATA_ERROR : ResultEnum.SUCCESS;
    }

    /**
     * 删除属性(后台表已生成该字段，删除需等待发布)
     *
     * @param id 属性id
     * @return {@link ResultEnum}
     */

    public ResultEnum deleteAttribute(Integer id) {
        if(id == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        if(baseMapper.selectById(id) == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        if( baseMapper.deleteAttribute(id) == 0){
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 记录日志
        String desc = "删除一个属性,id:" + id +"（待发布）";
        logService.saveEventLog(id, ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.DELETE, desc);

        return ResultEnum.SUCCESS;
    }

    /**
     * 删除数据（逻辑删除，仅用于删除  未发布的属性）
     *
     * @param id id
     * @return {@link ResultEnum}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteDataById(Integer id) {
        if(id == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        //判断数据是否存在
        if (baseMapper.selectById(id) == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //删除数据
        if (baseMapper.deleteById(id) <= 0) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 删除属性日志表记录
        attributeLogService.deleteDataByAttributeId(id);

        // 记录日志
        String desc = "删除一个属性,id:" + id;
        logService.saveEventLog(id, ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.DELETE, desc);

        //删除成功
        return ResultEnum.SUCCESS;
    }

    /**
     * 删除数据
     *
     * @param id id
     * @return {@link ResultEnum}
     */
    @Override
    public ResultEnum deleteData(Integer id){
        if(id == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        //判断数据是否存在
        AttributePO attributePo = baseMapper.selectById(id);
        if(attributePo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        if("name".equals(attributePo.getName()) && "code".equals(attributePo.getName())){
            return ResultEnum.CAN_NOT_DELETE_NAME_OR_CODE;
        }
        //若状态为新增待发布，则直接逻辑删除
        //若为修改待发布、发布、删除待发布，说明后台表已生成该字段，删除需等待发布
        if (attributePo.getStatus() == AttributeStatusEnum.INSERT) {
            return this.deleteDataById(id);
        } else {
            return this.deleteAttribute(id);
        }
    }

    /**
     * 获取实体下已发布状态的所有属性
     *
     * @param entityId
     * @return
     */
    @Override
    public List<AttributeInfoDTO> listPublishedAttribute(int entityId) {
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_wieght").lambda().eq(AttributePO::getEntityId, entityId)
                .eq(AttributePO::getStatus, AttributeStatusEnum.SUBMITTED)
                .eq(AttributePO::getSyncStatus, AttributeSyncStatusEnum.SUCCESS);
        List<AttributePO> list = baseMapper.selectList(queryWrapper);
        List<AttributeInfoDTO> attributeInfoDTOS = AttributeMap.INSTANCES.poToDtoList(list);
        attributeInfoDTOS = attributeInfoDTOS.stream().map(i->{
            i.setDomainEntityId(i.getDomainId());
            return i;
        }).collect(Collectors.toList());
        return attributeInfoDTOS;
    }

    @Override
    public List<PoiDetailDTO> getPoiDetails(PoiQueryDTO dto) {
        String token = getToken(appKey, secret);
        List<PoiDetailDTO> poiList = getPoiList(dto, token);
        return poiList;
    }

    @Override
    public Map<String, Object> getPoiAuthorization() {
        // 设置请求参数
        // 创建HTTP客户端
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .build();
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(10000).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultSocketConfig(socketConfig)
                .build();
        // 创建POST请求
        HttpGet httpGet = new HttpGet(authUrl);
        httpGet.setHeader("Authorization", getToken(appKey, secret));
        try {

            // 发送请求并获取响应
            HttpResponse response = httpClient.execute(httpGet);

            // 读取响应体中的内容
            String responseBody = EntityUtils.toString(response.getEntity());

            // 输出响应内容
            log.info("-------------------------" + responseBody);

            // 关闭HTTP客户端
            httpClient.close();
            JSONObject jsonObject = JSON.parseObject(responseBody);
            JSONObject data = JSON.parseObject(jsonObject.getString("data"));
            Map<String,Object> result = JSONObject.parseObject(data.toJSONString(), HashMap.class);
            log.info("------------------------poi获取权限:{}", result);
            return result;
        } catch (Exception e) {
            log.error("------------------------获取权限失败:{}", e.getMessage());
            throw new FkException(ResultEnum.ERROR, e.getMessage());
        }
    }

    @Override
    public List<AttributePO> getAttributeByEntityId(Integer entityId) {
        QueryWrapper<AttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(AttributePO::getEntityId,entityId);
        return baseMapper.selectList(queryWrapper);
    }


    public String getToken(String appkey, String secret) {
        // 设置请求参数

        // 创建HTTP客户端
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .build();
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(10000).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultSocketConfig(socketConfig)
                .build();
        // 创建POST请求
        HttpPost post = new HttpPost(getTokenUrl);

        // 创建参数列表
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("appkey", appkey));
        params.add(new BasicNameValuePair("secret", secret));
        try {
            // 创建UrlEncodedFormEntity
            HttpEntity entity = new UrlEncodedFormEntity(params);

            // 将请求体设置到POST请求中
            post.setEntity(entity);

            // 发送请求并获取响应
            HttpResponse response = httpClient.execute(post);

            // 读取响应体中的内容
            String responseBody = EntityUtils.toString(response.getEntity());

            // 输出响应内容
            log.info("-------------------------" + responseBody);

            // 关闭HTTP客户端
            httpClient.close();
            JSONObject jsonObject = JSON.parseObject(responseBody);
            JSONObject data = JSON.parseObject(jsonObject.getString("data"));
            String token = data.getString("token");
            log.info("------------------------poi获取token:{}", token);
            return token;
        } catch (Exception e) {
            log.error("------------------------获取token失败:{}", e.getMessage());
            throw new FkException(ResultEnum.ERROR, e.getMessage());
        }
    }

    public List<PoiDetailDTO> getPoiList(PoiQueryDTO dto, String token) {

        // 创建HTTP客户端
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .build();
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(10000).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultSocketConfig(socketConfig)
                .build();
        // 创建POST请求
        HttpPost post = new HttpPost(getPoiUrl);
        post.addHeader("authorization",token);
        Map<String,Object> map = new HashMap<>();
        map.put("search_area", dto.getSearchArea());
        map.put("category_type", dto.getCategoryType());
        map.put("keyword", dto.getKeyword());
        String parame = JSONObject.toJSONString(map);
        post.setHeader("Content-Type", "application/json");
        StringEntity se = new StringEntity(parame, "UTF-8");
        se.setContentType("application/json");
        // 创建参数列表

        try {
            // 将请求体设置到POST请求中
            post.setEntity(se);

            // 发送请求并获取响应
            HttpResponse response = httpClient.execute(post);

            // 读取响应体中的内容
            String responseBody = EntityUtils.toString(response.getEntity());

            // 输出响应内容
            log.info("-------------------------" + responseBody);

            // 关闭HTTP客户端
            httpClient.close();
            JSONObject jsonObject = JSON.parseObject(responseBody);
            String data = jsonObject.getString("data");
            List<PoiDetailDTO> poiDetailDTO = JSONObject.parseArray(data, PoiDetailDTO.class);
            log.info("------------------------获取poi数据:{}", token);
            return poiDetailDTO;
        } catch (Exception e) {
            log.error("------------------------获取数据失败:{}", e.getMessage());
            throw new FkException(ResultEnum.ERROR, e.getMessage());
        }
    }

    /**
     * 获取主数据字段数据分类和数据级别
     *
     * @return
     */
    @Override
    public CAndLDTO getDataClassificationsAndLevels() {
        CAndLDTO cAndLDTO = new CAndLDTO();
        List<ClassificationsAndLevelsDTO> classifications = new ArrayList<>();
        List<ClassificationsAndLevelsDTO> levels = new ArrayList<>();

        for (DataClassificationEnum value : DataClassificationEnum.values()) {
            ClassificationsAndLevelsDTO dto = new ClassificationsAndLevelsDTO();
            dto.setEnumName(value.getName());
            dto.setEnumValue(value.getValue());
            dto.setEnumLevel(value.getLevel());
            classifications.add(dto);
        }

        for (DataLevelEnum value : DataLevelEnum.values()) {
            ClassificationsAndLevelsDTO dto = new ClassificationsAndLevelsDTO();
            dto.setEnumName(value.getName());
            dto.setEnumValue(value.getValue());
            dto.setEnumLevel(value.getLevel());
            levels.add(dto);
        }
        cAndLDTO.setClassifications(classifications);
        cAndLDTO.setLevels(levels);

        return cAndLDTO;
    }

    @Override
    public Object mapMDMFieldsWithStandards(List<StandardsBeCitedDTO> dtos) {
        //        if (CollectionUtils.isEmpty(dtos)) {
//            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
//        }

        //获取 dmp_dw配置信息
        ResultEntity<DataSourceDTO> resultEntity = userClient.getFiDataDataSourceById(mdmSource);
        if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        DataSourceDTO data = resultEntity.getData();

        //重新装载前端没有的信息
        for (StandardsBeCitedDTO dto : dtos) {
            //设置数据库名称
            dto.setDatabaseName(data.getConDbname());
            //设置数据源id
            dto.setDbId(mdmSource);
            //设置数据源名称
            dto.setDatasourceName(data.getName());
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        ResultEntity<Object> result = dataManageClient.setStandardsByModelField(dtos,userInfo.getToken());

        return result.getData();
    }

    /**
     * 搜索主数据数据元关联字段
     * @param key
     * @return
     */
    @Override
    public List<TableFieldDTO> searchColumn(String key) {
        return this.baseMapper.searchColumn(key);
    }
}
