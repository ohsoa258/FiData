package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tableapi.TableApiResultDTO;
import com.fisk.dataservice.entity.TableApiResultPO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-09-08 13:38:07
 */
public interface ITableApiResultService extends IService<TableApiResultPO> {

    ResultEnum saveApiResult(List<TableApiResultDTO> dto, long appId);
}

