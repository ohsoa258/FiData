package com.fisk.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.entity.TBETLIncrementalPO;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.service.ITBETLIncremental;
import org.springframework.stereotype.Service;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/4 20:57
 * Description:
 */
@Service
public class TBETLIncrementalImpl extends ServiceImpl<TBETLIncrementalMapper, TBETLIncrementalPO>  implements ITBETLIncremental {
}
