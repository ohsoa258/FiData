package com.fisk.task.service.doris.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fisk.common.entity.BusinessResult;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;
import com.fisk.task.entity.TBETLlogPO;
import com.fisk.task.mapper.TBETLLogMapper;
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
    @Resource
    private TBETLLogMapper logMapper;

    @Override
    @DS("datainputdb")
    public BusinessResult updateNifiLogsAndImportOdsData(UpdateLogAndImportDataDTO dto) {
        boolean re = true;
        String msg = null;
        try {
            //HashMap<String,Object> map = new HashMap<>();
            //map.put("code",dto.code);
            //查询一个数据，为多个用 selectList 或者 selectByMap
            //List<TBETLlogPO> users = logMapper.selectByMap(map);
            //获取导入数据的条数
            Integer rows = logMapper.getThisTimeStgRows(dto.tablename, dto.code);
            //Integer imput_rows=getThisTimeStgRows(log.tablename,log.code);
            //更新条件
            UpdateWrapper<TBETLlogPO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("code", dto.code);
            TBETLlogPO tp = new TBETLlogPO();
            //更新日志状态
            tp.setStatus(2);
            tp.setDatarows(rows);
            Integer upres = logMapper.update(tp, updateWrapper);
            log.info("log日志更新完成，开始同步stg to ods数据");
            //stg到ods
            Integer importRes = logMapper.stgToOds(dto.tablename, dto.tablename.replace("ods", "stg"), dto.code);
            log.info("ods数据同步完成");
        } catch (Exception ex) {
            log.error(ex.getMessage());
            msg=ex.getMessage();
            re=false;
        }
        BusinessResult res = new BusinessResult(re, msg);
        return res;
    }

    public Integer getThisTimeStgRows(String tablename, String code) {
        Integer rows = logMapper.getThisTimeStgRows(tablename, code);
        return rows;
    }


}
