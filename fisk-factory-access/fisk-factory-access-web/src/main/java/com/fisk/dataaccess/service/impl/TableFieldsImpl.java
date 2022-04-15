package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.dto.datareview.DataReviewPageDTO;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.map.TableBusinessMap;
import com.fisk.dataaccess.map.TableFieldsMap;
import com.fisk.dataaccess.mapper.TableFieldsMapper;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.service.ITableFields;
import com.fisk.dataaccess.vo.AtlasIdsVO;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Service
@Slf4j
public class TableFieldsImpl extends ServiceImpl<TableFieldsMapper, TableFieldsPO> implements ITableFields {
    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private ITableAccess iTableAccess;
    @Resource
    private TableAccessImpl tableAccessImpl;
    @Resource
    private IAppRegistration iAppRegistration;
    @Resource
    private TableBusinessImpl businessImpl;
    @Resource
    private TableSyncmodeImpl syncmodeImpl;
    @Resource
    private AppDataSourceImpl dataSourceImpl;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    private UserHelper userHelper;
    @Resource
    private DataFactoryClient dataFactoryClient;

    @Override
    public Page<DataReviewVO> listData(DataReviewQueryDTO query) {

        StringBuilder querySql = new StringBuilder();
        querySql.append(generateCondition.getCondition(query.dto));
        DataReviewPageDTO data = new DataReviewPageDTO();
        data.page = query.page;
        data.where = querySql.toString();

        return baseMapper.filter(query.page, data);
    }

    @Override
    public TableFieldsDTO getTableField(int id) {
        //name 类别  简称
        TableFieldsPO po = baseMapper.selectById(id);
        TableAccessNonDTO data = iTableAccess.getData(po.tableAccessId);
        AppRegistrationDTO data1 = iAppRegistration.getData(data.appId);
        TableFieldsDTO tableFieldsDTO = new TableFieldsDTO();
        tableFieldsDTO.appbAbreviation = data1.appAbbreviation;
        tableFieldsDTO.fieldName = po.fieldName;
        tableFieldsDTO.fieldType = po.fieldType;
        tableFieldsDTO.originalTableName = data.tableName;
        return tableFieldsDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addData(TableAccessNonDTO dto) {

        List<TableFieldsDTO> listDto = dto.list;
        TableSyncmodeDTO syncmodeDto = dto.tableSyncmodeDTO;
        TableBusinessDTO businessDto = dto.businessDTO;
        if (CollectionUtils.isEmpty(listDto) || syncmodeDto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // list: dto -> po
        List<TableFieldsPO> listPo = TableFieldsMap.INSTANCES.listDtoToPo(listDto);

        boolean success;
        // 批量添加tb_table_fields
        success = this.saveBatch(listPo);
        if (!success) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        int businessMode = 4;
        if (syncmodeDto.syncMode == businessMode && businessDto != null) {
            success = businessImpl.save(TableBusinessMap.INSTANCES.dtoToPo(businessDto));
            if (!success) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        // 添加tb_table_syncmode
        Long tableAccessId = listPo.get(0).tableAccessId;
        TableSyncmodePO syncmodePo = syncmodeDto.toEntity(TableSyncmodePO.class);
        syncmodePo.id = tableAccessId;
        success = syncmodeImpl.saveOrUpdate(syncmodePo);

        TableAccessPO accessPo = tableAccessImpl.query().eq("id", tableAccessId).one();
        if (accessPo == null) {
            return ResultEnum.TABLE_NOT_EXIST;
        }

        // 修改发布状态
        if (dto.flag == 1) {
            accessPo.publish = 3;
            tableAccessImpl.updateById(accessPo);
        }

        // 发布
        publish(success, accessPo.appId, accessPo.id, accessPo.tableName, dto.flag, dto.openTransmission);

        return success ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum updateData(TableAccessNonDTO dto) {

        List<TableFieldsDTO> list = dto.list;

        List<TableFieldsPO> originalDataList = list(Wrappers.<TableFieldsPO>lambdaQuery()
                .eq(TableFieldsPO::getTableAccessId, list.get(0).tableAccessId)
                .select(TableFieldsPO::getId));

        TableAccessPO model = tableAccessImpl.getById(dto.id);

        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        TableSyncmodeDTO tableSyncmodeDTO = dto.getTableSyncmodeDTO();
        if (CollectionUtils.isEmpty(dto.list) || tableSyncmodeDTO == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 保存tb_table_fields
        boolean success;
////        for (TableFieldsDTO tableFieldsDTO : list) {
////            // 0: 未操作的数据  1: 新增  2: 编辑
////            int funcType = tableFieldsDTO.getFuncType();
////            if (funcType == 2) {
////                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
////                success = this.updateById(modelField);
////            } else if (funcType == 1) {
////                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
////                modelField.delFlag = 1;
////                success = this.save(modelField);
////            }
////        }
        success = this.saveOrUpdateBatch(TableFieldsMap.INSTANCES.listDtoToPo(list));
        if (!success) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 删除字段
        List<TableFieldsPO> webData = TableFieldsMap.INSTANCES.listDtoToPo(list);
        List<TableFieldsPO> webData1 = webData.stream().filter(e -> StringUtils.isNotEmpty(String.valueOf(e.id))).collect(Collectors.toList());
        List<TableFieldsPO> webDataList = webData1.stream().map(e -> e.id).collect(Collectors.toList()).stream()
                .map(e -> {
                    TableFieldsPO fieldsPo = new TableFieldsPO();
                    fieldsPo.setId(e);
                    return fieldsPo;
                }).collect(Collectors.toList());
        List<TableFieldsPO> collect = originalDataList.stream().filter(item -> !webDataList.contains(item)).collect(Collectors.toList());
        System.out.println("collect = " + collect);
        try {
            collect.stream().map(e -> baseMapper.deleteByIdWithFill(e)).collect(Collectors.toList());
        } catch (Exception e) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }
////        // 全删全插
////        try {
////            List<TableFieldsPO> list = this.query().eq("table_access_id", dto.id).list();
////            if (!CollectionUtils.isEmpty(list)) {
////                list.forEach(e -> baseMapper.deleteByIdWithFill(e));
////            }
////            // list: dto -> po
////            List<TableFieldsPO> listPo = TableFieldsMap.INSTANCES.listDtoToPo(dto.list);
////            this.saveBatch(listPo);
////        } catch (Exception e) {
////            return ResultEnum.UPDATE_DATA_ERROR;
////        }

////        boolean success = true;
        TableBusinessDTO businessDto = dto.businessDTO;
        // 保存tb_table_business
        int businessMode = 4;
        if (tableSyncmodeDTO.syncMode == businessMode && businessDto != null) {

            TableBusinessPO businessPo = TableBusinessMap.INSTANCES.dtoToPo(businessDto);
            success = businessImpl.saveOrUpdate(businessPo);
            if (!success) {
                return ResultEnum.UPDATE_DATA_ERROR;
            }
        }
        // 保存tb_table_syncmode
        TableSyncmodePO modelSync = tableSyncmodeDTO.toEntity(TableSyncmodePO.class);
        success = syncmodeImpl.updateById(modelSync);

        // 修改发布状态
        if (dto.flag == 1) {
            model.publish = 3;
            tableAccessImpl.updateById(model);
        }

        // 发布
        publish(success, model.appId, model.id, model.tableName, dto.flag, dto.openTransmission);

        return success ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public OperateMsgDTO loadDepend(OperateTableDTO dto) {
        OperateMsgDTO operateMsgDTO = new OperateMsgDTO();
        try {
            LoadDependDTO loadDependDTO = new LoadDependDTO();

            loadDependDTO.channelDataEnum = ChannelDataEnum.DATALAKE_TASK;
            loadDependDTO.tableId = dto.tableId;
            loadDependDTO.appId = dto.appId;
            // 判断数据工厂是否有依赖
            boolean loadDepend = dataFactoryClient.loadDepend(loadDependDTO);
            if (loadDepend) {
                switch (dto.operateBehaveTypeEnum) {
                    case UPDATE_APP:
                    case DELETE_APP:
                        operateMsgDTO.operateBehaveMsg = "管道正在使用该应用中的表,请及时更新管道";
                        break;
                    case UPDATE_TABLE:
                    case DELETE_TABLE:
                        operateMsgDTO.operateBehaveMsg = "管道正在使用该表,请及时更新管道";
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            throw new FkException(ResultEnum.DATA_FACTORY_FEIGN_EXCEPTION);
        }
        return operateMsgDTO;
    }

    /**
     * 组装参数
     *
     * @param userId    当前登录人id
     * @param appId     应用注册id
     * @param accessId  物理表id
     * @param tableName 表名
     * @return atlasIdsVO
     */
    public AtlasIdsVO getAtlasIdsVO(Long userId, Long appId, Long accessId, String tableName) {
        AtlasIdsVO atlasIdsVO = new AtlasIdsVO();
        atlasIdsVO.userId = userId;
        // 应用注册id
        atlasIdsVO.appId = String.valueOf(appId);
        // 物理表id
        atlasIdsVO.dbId = String.valueOf(accessId);
        atlasIdsVO.tableName = tableName;
        return atlasIdsVO;
    }


    /**
     * 调用发布和存储过程
     *
     * @param success   true: 保存成功,执行发布
     * @param appId     应用id
     * @param accessId  物理表id
     * @param tableName 物理表tableName
     * @param flag      0: 保存;  1: 发布
     */
    private void publish(boolean success, long appId, long accessId, String tableName, int flag, boolean openTransmission) {
        if (success && flag == 1) {
            UserInfo userInfo = userHelper.getLoginUserInfo();
            ResultEntity<BuildPhysicalTableDTO> result = tableAccessImpl.getBuildPhysicalTableDTO(accessId, appId);
            BuildPhysicalTableDTO data = result.data;
            data.appId = String.valueOf(appId);
            data.dbId = String.valueOf(accessId);
            data.userId = userInfo.id;
            data.openTransmission = openTransmission;

            // 版本号入库、调用存储存储过程  
            List<TableFieldsPO> list = this.query().eq("table_access_id", accessId).list();
            AppRegistrationPO registration = iAppRegistration.getById(appId);
            AppDataSourcePO dataSourcePo = dataSourceImpl.query().eq("app_id", appId).one();
            String odsTableName = "ods_" + registration.appAbbreviation + "_" + tableName;
            data.modelPublishTableDTO = getModelPublishTableDTO(accessId, odsTableName, 3, list);

            // 执行发布
            try {
                // 实时--RestfulAPI类型
                if (registration.appType == 0 && DataSourceTypeEnum.RestfulAPI.getName().equals(dataSourcePo.driveType)) {
                    // 传入apiId和api下所有表
                    TableAccessPO accessPo = tableAccessImpl.query().eq("id", accessId).one();
                    List<TableAccessPO> tablePoList = tableAccessImpl.query().eq("api_id", accessPo.apiId).list();
                    // api下所有表
                    data.apiTableNames = tablePoList.stream().map(e -> registration.appAbbreviation + "_" + e.tableName).collect(Collectors.toList());
                    data.appType = registration.appType;
                    data.apiId = accessPo.apiId;
                    // 创建表流程
                    publishTaskClient.publishBuildPhysicsTableTask(data);
                } else if (registration.appType == 1) {
                    // 非实时物理表发布
                    // 创建表流程
                    publishTaskClient.publishBuildPhysicsTableTask(data);
                    if (DataSourceTypeEnum.FTP.getName().equals(dataSourcePo.driveType)) {
                        data.excelFlow = true;
                    }
                    // 生成nifi流程
                    log.info(JSON.toJSONString(data));
                    publishTaskClient.publishBuildAtlasTableTask(data);
                }
            } catch (Exception e) {
                log.info("发布失败", e);
                log.info("发布失败,{}", ResultEnum.TASK_EXEC_FAILURE.getMsg());
                throw new FkException(ResultEnum.TASK_EXEC_FAILURE);
            }
        }
    }

    /**
     * 封装版本号和修改表结构的参数
     *
     * @param accessId     物理表名
     * @param odsTableName ods表名
     * @param createType   3: 代表数据接入
     * @param list         表字段
     * @return dto
     */
    private ModelPublishTableDTO getModelPublishTableDTO(long accessId, String odsTableName, int createType, List<TableFieldsPO> list) {
        ModelPublishTableDTO dto = new ModelPublishTableDTO();
        List<ModelPublishFieldDTO> fieldList = new ArrayList<>();

        list.forEach(po -> {
            ModelPublishFieldDTO fieldDTO = new ModelPublishFieldDTO();
            fieldDTO.fieldId = po.id;
            fieldDTO.fieldEnName = po.fieldName;
            fieldDTO.fieldType = po.fieldType;
            fieldDTO.fieldLength = Math.toIntExact(po.fieldLength);
            fieldList.add(fieldDTO);
        });

        dto.createType = createType;
        dto.tableId = accessId;
        dto.tableName = odsTableName;
        dto.fieldList = fieldList;
        return dto;
    }
}
