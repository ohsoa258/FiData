package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.mdm.entity.ProcessApplyNotesPO;
import com.fisk.mdm.mapper.ProcessApplyNotesMapper;
import com.fisk.mdm.service.IProcessApplyNotesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: wangjian
 * @Date: 2023-04-04
 */
@Slf4j
@Service
public class ProcessApplyNotesServiceImpl extends ServiceImpl<ProcessApplyNotesMapper, ProcessApplyNotesPO> implements IProcessApplyNotesService {
}
