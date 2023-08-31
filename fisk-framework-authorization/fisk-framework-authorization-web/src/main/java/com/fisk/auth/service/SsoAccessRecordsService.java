package com.fisk.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.auth.entity.SsoAccessRecordsPO;

/**
 * @author 56263
 * @description 针对表【tb_sso_access_records】的数据库操作Service
 * @createDate 2023-08-31 11:22:03
 */
public interface SsoAccessRecordsService extends IService<SsoAccessRecordsPO> {


    /**
     * 保存单点登录访问日志
     *
     * @return
     */
    boolean saveRecord(SsoAccessRecordsPO po);

}
