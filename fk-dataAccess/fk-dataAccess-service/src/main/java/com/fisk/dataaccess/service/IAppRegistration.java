package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.AppNameDTO;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.AppRegistrationEditDTO;
import com.fisk.dataaccess.dto.TableAppNameDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;

import java.util.List;

/**
 * @author: Lock
 * @data: 2021/5/26 14:12
 */
public interface IAppRegistration extends IService<AppRegistrationPO> {
    ResultEnum addData(AppRegistrationDTO appRegistrationDTO);

    PageDTO<AppRegistrationDTO> listAppRegistration(String key, Integer page, Integer rows);

    ResultEnum updateAppRegistration(AppRegistrationEditDTO dto);

    ResultEnum deleteAppRegistration(long id);

    //    List<String> queryAppName(byte appType);
//List<TableAppNameDTO> queryAppName(byte appType);
    List<AppNameDTO> queryAppName();

    AppRegistrationDTO getData(long id);

    List<AppRegistrationDTO> getDescDate();

    List<AppNameDTO> queryNRTAppName();
}
