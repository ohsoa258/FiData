package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.office.easyExcel.AccessCDCExcelDTO;
import com.fisk.common.core.utils.office.easyExcel.EasyExcelUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataReqDTO;
import com.fisk.dataaccess.dto.access.ExportCdcConfigDTO;
import com.fisk.dataaccess.dto.app.AppRegistrationDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.map.DataAccessMap;
import com.fisk.dataaccess.map.TableAccessMap;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.mapper.TableFieldsMapper;
import com.fisk.dataaccess.service.IDataAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 * @version 2.0
 * @description
 * @date 2022/1/6 15:16
 */
@Service
@Slf4j
public class DataAccessImpl extends ServiceImpl<TableAccessMapper, TableAccessPO> implements IDataAccess {

    @Resource
    TableAccessMapper tableAccessMapper;
    @Resource
    TableFieldsMapper tableFieldsMapper;
    @Resource
    TableFieldsImpl tableFieldsImpl;

    @Resource
    private AppRegistrationImpl appRegistration;

    @Override
    public ResultEntity<List<DataAccessSourceTableDTO>> getDataAccessMetaData() {

        List<DataAccessSourceTableDTO> tableDtoList = tableAccessMapper.listTableMetaData();
        List<TableFieldsPO> allTableFieldPOlist = tableFieldsImpl.query().list();
        // 获取物理表下的字段信息
        tableDtoList.forEach(e -> {
            List<TableFieldsPO> fieldsList = allTableFieldPOlist.stream().filter(a -> a.getTableAccessId().equals(e.getId())).collect(Collectors.toList());
            e.list = DataAccessMap.INSTANCES.fieldListPoToDto(fieldsList);
        });
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableDtoList);
    }

    @Override
    public ResultEntity<DataAccessSourceTableDTO> getDataAccessMetaDataByTableName(String tableName) {
        DataAccessSourceTableDTO dataAccessSourceTableDTO = tableAccessMapper.oneTableMetaDataByTableName(tableName).stream().findFirst().orElse(null);
        if (dataAccessSourceTableDTO != null) {
            List<TableFieldsPO> fieldsList = tableFieldsImpl.query().eq("table_access_id", dataAccessSourceTableDTO.getId()).list();
            dataAccessSourceTableDTO.list = DataAccessMap.INSTANCES.fieldListPoToDto(fieldsList);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataAccessSourceTableDTO);
    }

    @Override
    public List<FiDataTableMetaDataDTO> buildFiDataTableMetaData(FiDataTableMetaDataReqDTO dto) {

        List<FiDataTableMetaDataDTO> fiDataTableMetaDataDtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dto.tableUniques)) {
            List<TableAccessPO> tableAccessPoList = new ArrayList<>();
            tableAccessPoList.forEach(po -> {
                FiDataTableMetaDataDTO tableMeta = DataAccessMap.INSTANCES.tablePoToFiDataTableMetaData(po);
                if (tableMeta != null) {
                    List<TableFieldsPO> tableFieldsPoList = tableFieldsImpl.query().eq("table_access_id", po.id).select("field_name", "id").list();
                    tableMeta.setFieldList(DataAccessMap.INSTANCES.fieldListPoToFiDataTableMetaData(tableFieldsPoList));
                    fiDataTableMetaDataDtoList.add(tableMeta);
                }
            });
        }
        return fiDataTableMetaDataDtoList;
    }

    /**
     * 基于构造器注入
     */
    private final HttpServletResponse response;

    public DataAccessImpl(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * 数据湖管理-导出配置数据
     *
     * @param dto
     * @return
     */
    @Override
    public Object exportCdcConfig(ExportCdcConfigDTO dto) {
        //要导出的列
        List<String> includeColumnFieldNames = dto.getIncludeColumnFieldNames();
        //要导出的数据
        List<AccessCDCExcelDTO> data = new ArrayList<>();

        /*
        获取数据
         */
        //1 查询应用信息
        AppRegistrationDTO app = appRegistration.getAppById(Long.valueOf(dto.getAppId()));
        String sheetName = app.getAppName() + System.currentTimeMillis();
        //2 查询表信息
        List<TableAccessPO> tableAccessPOS = listByIds(dto.getTblIds());
        //3 查询字段信息并组装数据
        for (TableAccessPO tableAccessPO : tableAccessPOS) {
            List<TableFieldsPO> fieldsPOList = tableFieldsImpl.list(
                    new LambdaQueryWrapper<TableFieldsPO>()
                            .eq(TableFieldsPO::getTableAccessId, tableAccessPO.getId())
            );
            for (TableFieldsPO po : fieldsPOList) {
                AccessCDCExcelDTO excelDTO = new AccessCDCExcelDTO();
                excelDTO.setAppName(app.getAppName());
                excelDTO.setDbName(po.getSourceDbName());
                //架构名需要从source_tbl_name里面截取 校验源表名是否包含 .
                int dotIndex = po.getSourceTblName().indexOf('.');
                excelDTO.setSchemaName(dotIndex != -1 ? po.getSourceTblName().substring(0, dotIndex) : "");
                excelDTO.setTableName(po.getSourceTblName());
                excelDTO.setColumnName(po.getFieldName());
                excelDTO.setColumnNameCn(po.getDisplayName());
                excelDTO.setColumnNameEn(po.getFieldName());
                excelDTO.setColumnDesc(po.getFieldDes());
                excelDTO.setColumnType(po.getFieldType());
                excelDTO.setIsPrimaryKey(po.getIsPrimarykey());
                data.add(excelDTO);
            }
        }

        response.reset();
        response.setHeader("Content-disposition", "attachment; filename=ACCESS_CDC_APPID" + app.getId() + "_" + System.currentTimeMillis() + ".xlsx");
        response.setContentType("application/x-xls");
        //导出excel
        try {
            EasyExcelUtils.AccessCDCIncludeWrite(response, sheetName, includeColumnFieldNames, data);
        } catch (Exception e) {
            log.error("数据湖管理-Excel导出失败", e);
            throw new FkException(ResultEnum.EXCEL_EXPORT_ERROR);
        }
        return null;
    }

    /**
     * 根据应用id获取应用下的表名称和表id
     *
     * @param appId
     * @return
     */
    @Override
    public List<TableAccessDTO> getTblsByAppId(Integer appId) {
        List<TableAccessPO> list = this.list(
                new LambdaQueryWrapper<TableAccessPO>()
                        .eq(TableAccessPO::getAppId, appId)
                        .select(TableAccessPO::getId, TableAccessPO::getTableName)
        );

        return TableAccessMap.INSTANCES.listPoToDto(list);
    }
}
