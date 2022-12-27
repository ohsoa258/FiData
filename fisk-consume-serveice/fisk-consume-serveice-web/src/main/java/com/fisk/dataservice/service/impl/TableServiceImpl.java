package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.datasource.DataSourceConfigInfoDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageDataDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceSaveDTO;
import com.fisk.dataservice.entity.TableServicePO;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.map.DataSourceConMap;
import com.fisk.dataservice.map.TableServiceMap;
import com.fisk.dataservice.mapper.TableServiceMapper;
import com.fisk.dataservice.service.ITableService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class TableServiceImpl
        extends ServiceImpl<TableServiceMapper, TableServicePO>
        implements ITableService {

    @Resource
    DataSourceConManageImpl dataSourceConManage;
    @Resource
    TableFieldImpl tableField;
    @Resource
    TableSyncModeImpl tableSyncMode;

    @Resource
    UserClient client;


    @Resource
    TableServiceMapper mapper;

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
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data.id);
    }

    @Override
    public List<DataSourceConfigInfoDTO> getDataSourceConfig() {

        ResultEntity<List<DataSourceDTO>> allExternalDataSource = client.getAllExternalDataSource();
        if (allExternalDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        return DataSourceConMap.INSTANCES.voListToDtoInfo(allExternalDataSource.data);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum TableServiceSave(TableServiceSaveDTO dto) {

        //修改表服务数据
        updateTableService(dto.tableService);

        //表字段
        tableField.tableServiceSaveConfig((int) dto.tableService.id, dto.tableFieldList);

        //覆盖方式
        dto.tableSyncMode.typeTableId = (int) dto.tableService.id;
        dto.tableSyncMode.type = AppServiceTypeEnum.TABLE.getValue();
        tableSyncMode.tableServiceTableSyncMode(dto.tableSyncMode);

        //推送task
        buildParameter(dto);

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
        data.tableFieldList = tableField.getTableServiceField(id);
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

        tableField.delTableServiceField((int) id);

        tableSyncMode.delTableServiceSyncMode(id, AppServiceTypeEnum.TABLE.getValue());

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
        //表字段
        data.fieldDtoList = dto.tableFieldList;
        //同步配置
        data.syncModeDTO = dto.tableSyncMode;

        return data;
    }


}
