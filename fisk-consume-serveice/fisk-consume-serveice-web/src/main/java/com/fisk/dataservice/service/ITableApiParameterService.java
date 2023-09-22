package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tableapi.TableApiParameterDTO;
import com.fisk.dataservice.entity.TableApiParameterPO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-09-08 15:44:39
 */
public interface ITableApiParameterService extends IService<TableApiParameterPO> {

    ResultEnum savetTableApiParameter(List<TableApiParameterDTO> dto,long apiId);
}

