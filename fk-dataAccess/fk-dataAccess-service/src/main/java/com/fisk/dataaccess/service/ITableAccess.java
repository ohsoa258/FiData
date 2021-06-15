package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableAccessNDTO;
import com.fisk.dataaccess.dto.TablePhyHomeDTO;
import com.fisk.dataaccess.entity.TableAccessPO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author: Lock
 */
public interface ITableAccess extends IService<TableAccessPO> {
    ResultEnum addRTData(TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException;

    ResultEnum deleteData(long id);

    ResultEnum addNRTData(TableAccessNDTO tableAccessNDTO);

    ResultEnum updateRTData(TableAccessDTO dto) throws SQLException, ClassNotFoundException;

    ResultEnum updateNRTData(TableAccessNDTO dto);

    Map<String, List<String>> queryDataBase(String appName) throws SQLException, ClassNotFoundException;

    PageDTO<TablePhyHomeDTO> queryByPage(String key, Integer page, Integer rows);

    TableAccessDTO getData(long id);
}
