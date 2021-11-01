package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.dto.datamodel.AppRegistrationDataDTO;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.vo.AtlasIdsVO;
import com.fisk.dataaccess.vo.TableAccessVO;
import com.fisk.dataaccess.vo.datafactory.TableIdAndNameVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
public interface ITableAccess extends IService<TableAccessPO> {

    /**
     * 添加物理表(实时)
     *
     * @param tableAccessDTO 请求参数
     * @return 返回值
     */
    ResultEnum addRealTimeData(TableAccessDTO tableAccessDTO);

    /**
     * 删除数据
     *
     * @param id 请求参数
     * @return 返回值
     */
    ResultEntity<NifiVO> deleteData(long id);

    /**
     * 添加物理表(非实时)
     *
     * @param dto 请求参数
     * @return 返回值
     */
    ResultEntity<AtlasIdsVO> addNonRealTimeData(TableAccessNonDTO dto);

    /**
     * 修改物理表(实时)
     *
     * @param dto 请求参数
     * @return 返回值
     */
    ResultEnum updateRealTimeData(TableAccessDTO dto);

    /**
     * 修改物理表(非实时)
     *
     * @param dto 请求参数
     * @return 返回值
     */
    ResultEnum updateNonRealTimeData(TableAccessNonDTO dto);

    /**
     * 根据非实时应用名称,获取远程数据库的表及表对应的字段
     *
     * @param appName 请求参数
     * @return 返回值
     */
    Map<String, List<String>> queryDataBase(String appName);

    /**
     * 物理表接口首页分页查询
     *
     * @param key  搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return 返回值
     */
    Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows);

    /**
     * 根据id查询数据,回显实时表
     *
     * @param id 请求参数
     * @return 返回值
     */
    TableAccessNonDTO getData(long id);

    /**
     * 根据应用名称,获取远程数据库的表及表对应的字段
     *
     * @param appName 请求参数
     * @return 返回值
     */
    List<TablePyhNameDTO> getTableFields(String appName);

    /**
     * 根据应用名称,获取远程数据库的表及表对应的字段
     *
     * @param appId 应用ID
     * @return 返回值
     */
    List<TablePyhNameDTO> getTableFieldsByAppId(long appId);

    /**
     * atlas
     *
     * @param id    id
     * @param appid appid
     * @return atlas
     */
    ResultEntity<AtlasEntityDbTableColumnDTO> getAtlasBuildTableAndColumn(long id, long appid);

    /**
     * 提供给nifi的数据
     *
     * @param id    物理表id
     * @param appid 应用注册id
     * @return DataAccessConfigDTO
     */
    ResultEntity<DataAccessConfigDTO> dataAccessConfig(long id, long appid);

    /**
     * 根据appid和物理表id,查询atlasInstanceId和atlasDbId
     *
     * @param appid 应用注册id
     * @param id    物理表id
     * @return AtlasWriteBackDataDTO
     */
    ResultEntity<AtlasWriteBackDataDTO> getAtlasWriteBackDataDTO(long appid, long id);

    /**
     * atlas物理表回写
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addAtlasTableIdAndDorisSql(AtlasWriteBackDataDTO dto);

    /**
     * 回写componentId
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addComponentId(NifiAccessDTO dto);

    /**
     * 过滤器
     *
     * @param query 查询条件
     * @return 过滤结果
     */
    Page<TableAccessVO> listData(TableAccessQueryDTO query);

    /**
     * 筛选器获取表字段(多表)
     *
     * @return 多表字段
     */
    List<FilterFieldDTO> getColumn();

    /**
     * 获取数据接入表名以及字段
     *
     * @return 表名及表字段
     */
    List<TableNameDTO> getDataAccessMeta();

    /**
     * 应用注册tree
     *
     * @return tree
     */
    List<DataAccessTreeDTO> getTree();

    Object getDimensionMeta();

    /**
     * 添加维度时需要的应用下的物理表
     *
     * @param id 应用id
     * @return TableNameAndFieldDTO
     */
    List<TableNameDTO> getTableName(long id);

    /**
     * 根据表id获取表详情
     *
     * @param id
     * @return
     */
    TableAccessDTO getTableAccess(int id);

    /**
     * nifiSettingPO
     *
     * @return 表名及查询语句
     */
    BuildNifiFlowDTO createPgToDorisConfig(String tableName, String selectSql);

    ResultEntity<ComponentIdDTO> getComponentId(DataAccessIdsDTO dto);

    /**
     * 根据id获取接入表所有字段id
     *
     * @param id
     * @return
     */
    List<FieldNameDTO> getTableFieldId(int id);

    /**
     * 获取所有物理表id
     *
     * @return list
     */
    ResultEntity<List<ChannelDataDTO>> getTableId();

    List<TableIdAndNameVO> getTableIds();

    /**
     * 获取所有应用下表以及字段数据
     * @return
     */
    List<AppRegistrationDataDTO> getDataAppRegistrationMeta();

    List<FieldNameDTO> getTableFieldByQuery(String query);

}
