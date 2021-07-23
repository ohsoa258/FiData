package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.AppDriveTypeDTO;
import com.fisk.dataaccess.dto.AppNameDTO;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.AppRegistrationEditDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
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
    ResultEnum addData(AppRegistrationDTO dto);

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
    ResultEnum deleteAppRegistration(long id);

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
}
