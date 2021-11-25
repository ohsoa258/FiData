package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Service
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
    public ResultEntity<AtlasIdsVO> addData(TableAccessNonDTO dto) {

        List<TableFieldsDTO> listDto = dto.list;
        TableSyncmodeDTO syncmodeDto = dto.tableSyncmodeDTO;
        TableBusinessDTO businessDto = dto.businessDTO;
        if (CollectionUtils.isEmpty(listDto) || syncmodeDto == null) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL);
        }

        // list: dto -> po
        List<TableFieldsPO> listPo = TableFieldsMap.INSTANCES.listDtoToPo(listDto);

        boolean success;
        // 批量添加tb_table_fields
        success = this.saveBatch(listPo);
        if (!success) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        int businessMode = 3;
        if (syncmodeDto.syncMode == businessMode && businessDto != null) {
            success = businessImpl.save(TableBusinessMap.INSTANCES.dtoToPo(businessDto));
            if (!success) {
                return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        // 添加tb_table_syncmode
        TableSyncmodePO syncmodePo = syncmodeDto.toEntity(TableSyncmodePO.class);
        Long tableAccessId = listPo.get(0).tableAccessId;
        syncmodePo.id = tableAccessId;
        success = syncmodeImpl.save(syncmodePo);
        if (!success) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        UserInfo userInfo = userHelper.getLoginUserInfo();
        TableAccessPO tableAccessPo = tableAccessImpl.query().eq("id", tableAccessId).one();
        AtlasIdsVO atlasIdsVO = getAtlasIdsVO(userInfo.id, tableAccessPo.appId, tableAccessId, tableAccessPo.tableName);
        return ResultEntityBuild.build(ResultEnum.SUCCESS, atlasIdsVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum updateData(TableAccessNonDTO dto) {

//        List<TableFieldsDTO> list = dto.list;

/*        List<TableFieldsPO> originalDataList = list(Wrappers.<TableFieldsPO>lambdaQuery()
                .eq(TableFieldsPO::getTableAccessId, list.get(0).tableAccessId)
                .select(TableFieldsPO::getId));*/

        TableSyncmodeDTO tableSyncmodeDTO = dto.getTableSyncmodeDTO();
        if (CollectionUtils.isEmpty(dto.list) || tableSyncmodeDTO == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 保存tb_table_fields
/*
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
*/
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

        boolean success = true;
        TableBusinessDTO businessDto = dto.businessDTO;
        // 保存tb_table_business
        int businessMode = 3;
        if (tableSyncmodeDTO.syncMode == businessMode && businessDto != null) {

//            TableBusinessPO businessPo = businessDto.toEntity(TableBusinessPO.class);
            TableBusinessPO businessPo = TableBusinessMap.INSTANCES.dtoToPo(businessDto);
            success = businessImpl.saveOrUpdate(businessPo);

//            success = businessImpl.updateById(businessPo);
            if (!success) {
                return ResultEnum.UPDATE_DATA_ERROR;
            }
        }
        // 保存tb_table_syncmode
        TableSyncmodePO modelSync = tableSyncmodeDTO.toEntity(TableSyncmodePO.class);
        success = syncmodeImpl.updateById(modelSync);

        return success ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    /**
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
}
