package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;

import java.util.List;

public interface BusinessExtendedfieldsService extends IService<BusinessExtendedfieldsPO> {
    /**
     * 展示维度数据
     * @param
     * @return
     */
    List<BusinessExtendedfieldsPO> addBusinessExtendedfields(String indexid);

}
