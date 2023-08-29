package com.fisk.dataaccess.service;

import com.fisk.dataaccess.dto.apistate.ApiStateDTO;
import com.fisk.dataaccess.entity.ApiStatePO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tb_api_state】的数据库操作Service
* @createDate 2023-08-29 10:44:39
*/
public interface IApiStateService extends IService<ApiStatePO> {

    /**
     * 编辑api开启状态    save or update
     *
     * @param dto
     * @return
     */
    Boolean editApiState(ApiStateDTO dto);

    /**
     * 回显api是否开启状态 get
     *
     * @return
     */
    ApiStateDTO getApiState();
}
