package com.fisk.task.service.doris.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fisk.common.entity.BusinessResult;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;
import com.fisk.task.service.doris.IDorisIncrementalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/3 11:26
 * Description:
 */
@Service
@Slf4j
public class DorisIncrementallmpl implements IDorisIncrementalService {


    @Override
    @DS("datainputdb")
    public BusinessResult updateNifiLogsAndImportOdsData(UpdateLogAndImportDataDTO dto) {


        return null;
    }



}
