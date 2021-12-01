package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.dto.datareview.DataReviewPageDTO;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.entity.TableBusinessPO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.entity.TableSyncmodePO;
import com.fisk.dataaccess.map.TableBusinessMap;
import com.fisk.dataaccess.map.TableFieldsMap;
import com.fisk.dataaccess.mapper.TableFieldsMapper;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.service.ITableFields;
import com.fisk.dataaccess.vo.AtlasIdsVO;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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
    private PublishTaskClient publishTaskClient;
    @Resource
    private UserHelper userHelper;

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
        TableFieldsPO tableFieldsPO = baseMapper.selectById(id);
        TableAccessNonDTO data = iTableAccess.getData(tableFieldsPO.tableAccessId);
        AppRegistrationDTO data1 = iAppRegistration.getData(data.appId);
        TableFieldsDTO tableFieldsDTO = new TableFieldsDTO();
        tableFieldsDTO.appbAbreviation = data1.appAbbreviation;
        tableFieldsDTO.fieldName = tableFieldsPO.fieldName;
        tableFieldsDTO.fieldType = tableFieldsPO.fieldType;
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

        int businessMode = 3;
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
        success = syncmodeImpl.save(syncmodePo);

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
        publish(success, accessPo.appId, accessPo.id, accessPo.tableName, dto.flag);

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
        boolean success = true;
        for (TableFieldsDTO tableFieldsDTO : list) {
            // 0: 未操作的数据  1: 新增  2: 编辑
            int funcType = tableFieldsDTO.getFuncType();
            if (funcType == 2) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                success = this.updateById(modelField);
            } else if (funcType == 1) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                modelField.delFlag = 1;
                success = this.save(modelField);
            }
        }
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
/*
        // 全删全插
        try {
            List<TableFieldsPO> list = this.query().eq("table_access_id", dto.id).list();
            if (!CollectionUtils.isEmpty(list)) {
                list.forEach(e -> baseMapper.deleteByIdWithFill(e));
            }
            // list: dto -> po
            List<TableFieldsPO> listPo = TableFieldsMap.INSTANCES.listDtoToPo(dto.list);
            this.saveBatch(listPo);
        } catch (Exception e) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }
*/

//        boolean success = true;
        TableBusinessDTO businessDto = dto.businessDTO;
        // 保存tb_table_business
        int businessMode = 3;
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
        publish(success,model.appId,model.id,model.tableName,dto.flag);

        return success ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
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
     * @param tableName 物理表-表名
     * @param flag      0: 保存;  1: 发布
     */
    private void publish(boolean success, long appId, long accessId, String tableName, int flag) {
        if (success && flag == 1) {
/*
            UserInfo userInfo = userHelper.getLoginUserInfo();
            AtlasIdsVO atlasIdsVO = getAtlasIdsVO(userInfo.id, appId, accessId, tableName);
            AtlasEntityQueryDTO atlasEntityQueryDTO = new AtlasEntityQueryDTO();
            atlasEntityQueryDTO.userId = atlasIdsVO.userId;
            // 应用注册id
            atlasEntityQueryDTO.appId = atlasIdsVO.appId;
            // 物理表id
            atlasEntityQueryDTO.dbId = atlasIdsVO.dbId;
            //表名称
            atlasEntityQueryDTO.tableName = tableName;
            log.info("给nifi组装参数" + atlasEntityQueryDTO);
            System.out.println("atlasEntityQueryDTO = " + atlasEntityQueryDTO);*/

            ResultEntity<BuildPhysicalTableDTO> buildPhysicalTableDTO = tableAccessImpl.getBuildPhysicalTableDTO(accessId, appId);
            BuildPhysicalTableDTO data = buildPhysicalTableDTO.data;
            data.appId = String.valueOf(appId);
            data.dbId = String.valueOf(accessId);

            // 保存成功,执行发布,调用存储过程
            publishTaskClient.publishBuildAtlasTableTask(data);
        }
    }
}
