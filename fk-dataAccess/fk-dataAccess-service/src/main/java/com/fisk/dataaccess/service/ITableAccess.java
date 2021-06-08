package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableAccessNDTO;
import com.fisk.dataaccess.entity.TableAccessPO;

/**
 * @author: Lock
 */
public interface ITableAccess extends IService<TableAccessPO> {
    ResultEnum addRTData(TableAccessDTO tableAccessDTO);

    ResultEnum deleteData(long id);

    ResultEnum addNRTData(TableAccessNDTO tableAccessNDTO);
}
