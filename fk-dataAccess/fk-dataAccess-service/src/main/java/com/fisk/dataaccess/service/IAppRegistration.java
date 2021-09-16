package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.dataaccess.vo.AtlasEntityQueryVO;
import com.fisk.dataaccess.vo.NifiVO;
import com.fisk.task.dto.atlas.AtlasEntityDTO;

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
     * 删除
     * @param id 请求参数
     * @return 返回值
     */
    ResultEntity<NifiVO> deleteAppRegistration(long id);

    /**
     * 根据是否为实时,查询应用名称集合
     * @return 返回值
     */
    List<AppNameDTO> queryAppName();

    /**
     * 根据id查询数据,用于数据回显
     * @param id 请求参数
     * @return 返回值
     */
    AppRegistrationDTO getData(long id);

    /**
     * 查询应用数据，按照创建时间倒序排序，查出top 10的数据
     * @return 返回值
     */
    List<AppRegistrationDTO> getDescDate();

    /**
     * 获取非实时应用名称
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
     * @param appid appid
     * @param atlasInstanceId atlasInstanceId
     * @param atlasDbId atlasDbId
     * @return 执行结果
     */
    ResultEnum addAtlasInstanceIdAndDbId(long appid, String atlasInstanceId, String atlasDbId);

    /**
     * 过滤器
     *
     * @param query 查询条件
     * @return 过滤结果
     */
    Page<AppRegistrationVO> listData(AppRegistrationQueryDTO query);

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
     * @param dto dto
     * @return 连接结果
     */
    ResultEntity<Object> connectDb(DbConnectionDTO dto);

    /**
     * 判断应用名称是否重复
     * @param appName appName
     * @return 执行结果
     */
    ResultEntity<Object> getRepeatAppName(String appName);

    /**
     * 判断应用简称是否重复
     * @param appAbbreviation appAbbreviation
     * @return 执行结果
     */
    ResultEntity<Object> getRepeatAppAbbreviation(String appAbbreviation);

    /**
     * insertAppRegistrationPO
     * @param appRegistrationPO dto
     * @return
     */
    AppRegistrationPO insertAppRegistrationPO(AppRegistrationPO appRegistrationPO);
}
