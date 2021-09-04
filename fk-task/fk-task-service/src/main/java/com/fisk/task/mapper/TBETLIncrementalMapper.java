package com.fisk.task.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.task.entity.TBETLIncrementalPO;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/4 16:39
 * Description:
 */
@DS("datainputdb")
public interface TBETLIncrementalMapper extends BaseMapper<TBETLIncrementalPO> {

}
