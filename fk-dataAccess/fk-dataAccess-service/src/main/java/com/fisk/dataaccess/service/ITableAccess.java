package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableAccessNDTO;
import com.fisk.dataaccess.dto.TablePyhNameDTO;
import com.fisk.dataaccess.entity.TableAccessPO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
public interface ITableAccess extends IService<TableAccessPO> {
    ResultEnum addRTData(TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException;

    ResultEnum deleteData(long id);

    ResultEnum addNRTData(TableAccessNDTO tableAccessNDTO) throws SQLException, ClassNotFoundException;

    ResultEnum updateRTData(TableAccessDTO dto) throws SQLException, ClassNotFoundException;

    ResultEnum updateNRTData(TableAccessNDTO dto) throws SQLException, ClassNotFoundException;

    Map<String, List<String>> queryDataBase(String appName) throws SQLException, ClassNotFoundException;

    Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows);

    TableAccessNDTO getData(long id);

    /**
     * @param appName 请求参数
     * @return 返回值
     */
    List<TablePyhNameDTO> getTableFields(String appName);
}
