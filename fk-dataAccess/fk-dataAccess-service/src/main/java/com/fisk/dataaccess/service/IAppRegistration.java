package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.vo.AppRegistrationVO;

import java.util.List;

/**
 * @author: Lock
 * @data: 2021/5/26 14:12
 */
public interface IAppRegistration extends IService<AppRegistrationPO> {
    ResultEnum addData(AppRegistrationDTO appRegistrationDTO);

    List<AppRegistrationVO> listAppRegistration();

}
