package com.fisk.dataaccess.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.server.datasource.ExternalDataSourceDTO;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelAppDTO;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.dataaccess.dto.SyncOneTblForHudiDTO;
import com.fisk.dataaccess.dto.access.OdsFieldQueryDTO;
import com.fisk.dataaccess.dto.app.*;
import com.fisk.dataaccess.dto.datafactory.AccessRedirectDTO;
import com.fisk.dataaccess.dto.datasource.DataSourceFullInfoDTO;
import com.fisk.dataaccess.dto.datasource.DataSourceInfoDTO;
import com.fisk.dataaccess.dto.hudi.HudiReSyncDTO;
import com.fisk.dataaccess.dto.hudi.HudiSyncDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobParameterDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobScriptDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.dataaccess.vo.AtlasEntityQueryVO;
import com.fisk.dataaccess.vo.CDCAppDbNameVO;
import com.fisk.dataaccess.vo.CDCAppNameAndTableVO;
import com.fisk.dataaccess.vo.datafactory.SyncTableCountVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.dataaccess.vo.table.CDCAppNameVO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapTblDTO;
import com.fisk.datamanagement.dto.standards.SearchColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lock
 */
public interface IAppRegistration extends IService<AppRegistrationPO> {

    /**
     * 添加应用
     *
     * @param dto 请求参数
     * @return 返回值
     */
    ResultEntity<AtlasEntityQueryVO> addData(AppRegistrationDTO dto);

    /**
     * 分页查询
     *
     * @param key  搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return 返回值
     */
    PageDTO<AppRegistrationDTO> listAppRegistration(String key, Integer page, Integer rows);

    /**
     * 应用注册-修改
     *
     * @param dto 请求参数
     * @return 返回值
     */
    ResultEnum updateAppRegistration(AppRegistrationEditDTO dto);

    /**
     * 修改应用基本信息
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editAppBasicInfo(AppRegistrationEditDTO dto);

    /**
     * 删除
     *
     * @param id 请求参数
     * @return 返回值
     */
    ResultEntity<NifiVO> deleteAppRegistration(long id);

    /**
     * 根据是否为实时,查询应用名称集合
     *
     * @return 返回值
     */
    List<AppNameDTO> queryAppName();

    /**
     * 根据id查询数据,用于数据回显
     *
     * @param id 请求参数
     * @return 返回值
     */
    AppRegistrationDTO getData(long id);

    /**
     * 查询应用数据，按照创建时间倒序排序，查出top 10的数据
     *
     * @return 返回值
     */
    List<AppRegistrationDTO> getDescDate();

    /**
     * 获取非实时应用名称
     *
     * @return 返回值
     */
    List<AppNameDTO> queryNoneRealTimeAppName();

    /**
     * 查询数据源驱动类型
     *
     * @return 驱动类型
     */
    List<AppDriveTypeDTO> getDriveType();

    /**
     * atlas数据
     *
     * @param id id
     * @return 查询结果
     */
    AtlasEntityDTO getAtlasEntity(long id);

    /**
     * atlas通过appid,将atlasInstanceId和atlasDbId保存下来
     *
     * @param appid           appid
     * @param atlasInstanceId atlasInstanceId
     * @param atlasDbId       atlasDbId
     * @return 执行结果
     */
    ResultEnum addAtlasInstanceIdAndDbId(long appid, String atlasInstanceId, String atlasDbId);

    /**
     * 应用筛选器 不包含doris外部目录应用
     *
     * @param query 查询条件
     * @return 过滤结果
     */
    Page<AppRegistrationVO> listData(AppRegistrationQueryDTO query);

    /**
     * 应用筛选器 包含doris外部目录应用
     *
     * @param query
     * @return
     */
    Page<AppRegistrationVO> getDorisCatalogs(AppRegistrationQueryDTO query);

    /**
     * 获取过滤器表字段
     *
     * @return 表字段
     */
    List<FilterFieldDTO> getColumn();

    /**
     * 获取应用注册名称和id
     *
     * @return 应用名称
     */
    List<AppNameDTO> getDataList();

    /**
     * 测试连接
     *
     * @param dto dto
     * @return 连接结果
     */
    List<DbNameDTO> connectDb(DbConnectionDTO dto);

    /**
     * 判断应用名称是否重复
     *
     * @param appName appName
     * @return 执行结果
     */
    ResultEntity<Object> getRepeatAppName(String appName);

    /**
     * 判断应用简称是否重复
     *
     * @param appAbbreviation appAbbreviation
     * @param whetherSchema
     * @return 执行结果
     */
    ResultEntity<Object> getRepeatAppAbbreviation(String appAbbreviation, boolean whetherSchema);

    /**
     * 查询数据接入下所有业务系统个数
     *
     * @return dto
     */
    DataAccessNumDTO getDataAccessNum();

    /**
     * 日志分页筛选器
     *
     * @param dto dto
     * @return 执行结果
     */
    Page<PipelineTableLogVO> logMessageFilter(PipelineTableQueryDTO dto);

    /**
     * 通过appId和apiId查询表名集合
     *
     * @param dto dto
     * @return 执行结果
     */
    List<LogMessageFilterVO> getTableNameListByAppIdAndApiId(PipelineTableQueryDTO dto);

    /**
     * 跳转页面: 查询出当前(表、api、ftp)具体在哪个管道中使用,并给跳转页面提供数据
     *
     * @param dto dto
     * @return list
     */
    List<DispatchRedirectDTO> redirect(AccessRedirectDTO dto);

    /**
     * 获取数据接入结构
     *
     * @param dto dto
     * @return list
     */
    List<FiDataMetaDataDTO> getDataAccessStructure(FiDataMetaDataReqDTO dto);

    /**
     * 获取数据接入表结构
     *
     * @param dto dto
     * @return list
     */
    List<FiDataMetaDataTreeDTO> getDataAccessTableStructure(FiDataMetaDataReqDTO dto);

    /**
     * 刷新数据接入结构
     *
     * @param dto dto
     * @return list
     */
    boolean setDataAccessStructure(FiDataMetaDataReqDTO dto);

    /**
     * 构建业务元数据其他数据信息
     *
     * @param dto dto
     * @return 查询结果
     */
    TableRuleInfoDTO buildTableRuleInfo(TableRuleParameterDTO dto);

    /**
     * 根据表信息/字段ID,获取表/字段基本信息
     *
     * @param dto dto
     * @return 查询结果
     */
    List<FiDataTableMetaDataDTO> getFiDataTableMetaData(FiDataTableMetaDataReqDTO dto);

    /**
     * 获取所有应用信息
     *
     * @return list
     */
    List<AppBusinessInfoDTO> getAppList();

    /**
     * 获取所有不使用简称作为架构名的应用信息
     *
     * @return list
     */
    List<AppRegistrationPO> getAppListWithNoSchema();

    /**
     * 根据jwt身份验证地址,获取token
     *
     * @param dto
     * @return
     */
    String getApiToken(AppDataSourceDTO dto);

    /**
     * 获取cdc任务脚本
     *
     * @param dto
     * @return
     */
    CdcJobScriptDTO buildCdcJobScript(CdcJobParameterDTO dto);

    /**
     * 获取ods数据源集合
     *
     * @return
     */
    List<ExternalDataSourceDTO> getFiDataDataSource();

    /**
     * 数仓建模获取fidata数据源（ods & lake） 不包含HUDI
     * @return
     */
    List<ExternalDataSourceDTO> getFiDataOdsAndLakeSource();

    /**
     * 数据类型集合
     *
     * @param appId
     * @return
     */
    JSONObject dataTypeList(Integer appId);

    /**
     * 元数据同步所有应用
     *
     * @return
     */
    List<MetaDataInstanceAttributeDTO> synchronizationAppRegistration();

    /**
     * 元数据同步所有接入表
     *
     * @return
     */
    List<MetaDataInstanceAttributeDTO> synchronizationAccessTable();

    /**
     * 元数据根据最近同步时间同步接入表
     *
     * @param lastSyncTime
     * @return
     */
    List<MetaDataInstanceAttributeDTO> synchronizationAccessTableByLastSyncTime(String lastSyncTime);

    /**
     * 元数据同步单个接入表
     *
     * @return
     */
    List<MetaDataInstanceAttributeDTO> synchronizationAccessOneTable(Long appId);

    /**
     * 依据应用id集合查询应用对应的目标源id集合
     *
     * @param appIds 应用id集合
     * @return
     */
    List<AppRegistrationInfoDTO> getBatchTargetDbIdByAppIds(List<Integer> appIds);

    /**
     * 获取数据接入不同同步类型下的表个数
     *
     * @param appId
     * @return
     */
    SyncTableCountVO getSyncTableCount(Integer appId);

    /**
     * 通过appid获取特定数据源驱动类型
     * 若当前app存在的数据源已经包含mysql,sqlserver,oracle,pg数据库类型，那么就不能再使用除这四种数据库类型外的其他数据源
     * 意味着只返回mysql,sqlserver,oracle,pg
     * 若当前app存在的数据源已经包含RestfulApi,Api,Sftp,Ftp这四种非数据库类型的其中一种，那么当前app就只能使用这一种数据源
     * 意味着只返回RestfulApi,Api,Sftp,Ftp这四种中已被选取的那一种
     *
     * @param appid
     * @return
     */
    List<AppDriveTypeDTO> getDriveTypeByAppId(Long appid);

    /**
     * 根据appId获取app应用名称
     *
     * @param id
     * @return
     */
    AppRegistrationDTO getAppNameById(Long id);

    /**
     * 根据appId获取app应用信息
     *
     * @param id
     * @return
     */
    AppRegistrationDTO getAppById(Long id);

    /**
     * 根据应用名称获取单个应用详情
     *
     * @param appName
     * @return
     */
    AppRegistrationDTO getAppByAppName(String appName);

    /**
     * 数据接入--应用级别修改应用下的接口是否允许推送数据
     *
     * @param appId
     * @return
     */
    Object appIfAllowDataTransfer(Long appId);

    /**
     * hudi入仓配置 -- 新增表时 获取表名
     *
     * @param dbId
     * @return
     */
    List<TablePyhNameDTO> getHudiConfigFromDb(Integer dbId);

    /**
     * hudi入仓配置 -- 配置单张表
     *
     * @param dto
     * @return
     */
    Object syncOneTblForHudi(SyncOneTblForHudiDTO dto);

    /**
     * 获取cdc类型所有应用及表名
     */
    List<CDCAppNameAndTableVO> getCDCAppNameAndTables(Integer appId);
    /**
     * 获取cdc类型所有应用的库名
     *
     * @return
     */
    List<CDCAppDbNameVO> getCDCAppDbName();

    /**
     * 获取cdc类型所有应用
     */
    List<CDCAppNameVO> getAllCDCAppName();


    /**
     * 获取数据接入所有应用和应用下的所有物理表
     *
     * @return
     */
    List<AccessAndModelAppDTO> getAllAppAndTables();

    /**
     * 通过物理表id获取应用详情
     *
     * @param tblId
     * @return
     */
    AppRegistrationDTO getAppByTableAccessId(Integer tblId);

    /**
     * hudi入仓配置 同步所有来源数据库对应库下的表信息到fidata平台配置库
     * 同步方式 1全量  2增量  3同步指定库
     *
     * @param syncDto
     */
    Object hudiSyncAllTables(HudiSyncDTO syncDto);


    /**
     * hudi入仓配置 重新同步单个指定表
     * 某张表结构变了  想重新同步一下   这张表的字段  已有的不要动  不存在的删掉  新加的再同步过来
     *
     *
     * @param syncDto
     */
    ResultEnum hudiReSyncOneTable(HudiReSyncDTO syncDto);

    /**
     * 获取所有被应用引用的数据源信息
     */
    List<AppDataSourceDTO> getAppSources();

    /**
     * 获取数据接入表结构(数据标准用)
     * @param dto
     * @return
     */
    List<TableNameDTO> getTableDataStructure(FiDataMetaDataReqDTO dto);
    /**
     * 获取数据接入字段结构(数据标准用)
     * @param dto
     * @return
     */
    List<TableColumnDTO> getFieldsDataStructure(ColumnQueryDTO dto);

    /**
     * 搜索数据元关联字段(数据标准用)
     * @param key
     * @return
     */
    List<SearchColumnDTO> searchStandardBeCitedField(String key);

    /**
     * 获取元数据地图 数据湖（数据接入）
     */
    List<MetaMapDTO> accessGetMetaMap();

    /**
     * 元数据地图 获取应用下的表
     * @return
     */
    List<MetaMapTblDTO> accessGetMetaMapTableDetail(Integer appId);

    /**
     * 根据应用id 获取当前应用引用的系统数据源和目标库的系统数据源   id+名称
     *
     * @return
     */
    List<DataSourceInfoDTO> getAppSourceAndTarget(Integer appId);

    /**
     * 根据应用id 获取当前应用引用的系统数据源和目标库的系统数据源详细信息
     *
     * @return
     */
    List<DataSourceFullInfoDTO> getAppSourceAndTargetFullInfo(Integer appId, Integer tblId);

    /**
     * 数据质量左侧Tree接口-获取ods数据源文件夹表层级
     *
     * @return
     */
    DataQualityDataSourceTreeDTO getOdsFolderTableTree();

    /**
     * 数据质量左侧Tree接口-根据表ID获取表下面的字段
     *
     * @param dto
     * @return
     */
    List<DataQualityDataSourceTreeDTO> getOdsTableFieldByTableId(OdsFieldQueryDTO dto);
}
