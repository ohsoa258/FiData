package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableAccessNonDTO;
import com.fisk.dataaccess.dto.TablePyhNameDTO;
import com.fisk.dataaccess.entity.TableAccessPO;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
public interface ITableAccess extends IService<TableAccessPO> {

    /**
     * 添加物理表(实时)
     * @param tableAccessDTO 请求参数
     * @return 返回值
     */
    ResultEnum addRealTimeData(TableAccessDTO tableAccessDTO);

    /**
     * 删除数据
     * @param id 请求参数
     * @return 返回值
     */
    ResultEnum deleteData(long id);

    /**
     * 添加物理表(非实时)
     * @param dto 请求参数
     * @return 返回值
     */
    ResultEnum addNonRealTimeData(TableAccessNonDTO dto);

    /**
     * 修改物理表(实时)
     * @param dto 请求参数
     * @return 返回值
     */
    ResultEnum updateRealTimeData(TableAccessDTO dto);

    /**
     * 修改物理表(非实时)
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
}
