package com.fisk.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.entity.SsoAccessRecordsPO;
import com.fisk.auth.mapper.SsoAccessRecordsMapper;
import com.fisk.auth.service.SsoAccessRecordsService;
import org.springframework.stereotype.Service;

/**
 * @author 56263
 * @description 针对表【tb_sso_access_records】的数据库操作Service实现
 * @createDate 2023-08-31 11:22:03
 */
@Service
public class SsoAccessRecordsServiceImpl extends ServiceImpl<SsoAccessRecordsMapper, SsoAccessRecordsPO>
        implements SsoAccessRecordsService {

    /**
     * 保存单点登录访问日志
     *
     * @return
     */
    @Override
    public boolean saveRecord(SsoAccessRecordsPO po) {
        return save(po);
    }
}




