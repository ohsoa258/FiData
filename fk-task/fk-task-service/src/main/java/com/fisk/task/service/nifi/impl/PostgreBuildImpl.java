package com.fisk.task.service.nifi.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;
import com.fisk.task.entity.TBETLlogPO;
import com.fisk.task.mapper.TBETLLogMapper;
import com.fisk.task.service.nifi.IPostgreBuild;
import com.fisk.task.utils.PostgreHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.CallableStatement;
import java.sql.Connection;

@Service
@Slf4j
public class PostgreBuildImpl implements IPostgreBuild {
    @Resource
    private TBETLLogMapper logMapper;

    private static String DatainputUrl;
    @Value("${pgsql-datainput.url}")
    public void setPgsqlDatainputUrl(String pgsqlDatainputUrl) {
        DatainputUrl = pgsqlDatainputUrl;
    }

    @Override
    public BusinessResult postgreBuildTable(String executsql, BusinessTypeEnum businessTypeEnum) {
        boolean re = false;
        String msg = null;
        try {
            PostgreHelper.postgreExecuteSql(executsql,businessTypeEnum);
            re = true;
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
            msg = e.getMessage();
        }
        BusinessResult res = new BusinessResult(re, msg);
        return res;
    }

    @Override
    public <T> BusinessResult postgreQuery(String executsql, BusinessTypeEnum businessTypeEnum,T data) {
        boolean re = false;
        String msg = null;
        BusinessResult res = null;
        try {
            data = PostgreHelper.postgreQuery(executsql, businessTypeEnum, data);
            re = true;
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
            msg = e.getMessage();
        }
        res = BusinessResult.of(re, msg, data);
        return res;
    }

    @Override
    public BusinessResult postgreQuery(String executsql, BusinessTypeEnum businessTypeEnum) {
        boolean re = false;
        String msg = null;
        BusinessResult res = null;
        JSONArray resultSet=null;
        try {
             resultSet = PostgreHelper.postgreQuery(executsql, businessTypeEnum);
            re = true;
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
            msg = e.getMessage();
        }
        res = BusinessResult.of(re, msg, resultSet);
        return res;
    }
    @Override
    public BusinessResult postgreDataStgToOds(String stgTable, String odsTable, UpdateLogAndImportDataDTO dto)
    {
        boolean re = false;
        String msg = null;
        int proc_res=0;
        BusinessResult res = new BusinessResult(re, msg);
        String procedure = "{ call data_stg_to_ods(?, ?)}";
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
            Connection conn=PostgreHelper.getConnection(DatainputUrl);
            CallableStatement statement = conn.prepareCall(procedure);
            //通过 setXXX 方法将值传给IN参数
            statement.setString(1, stgTable);
            statement.setString(2, odsTable);
            statement.execute();
            proc_res=statement.getInt(3);
            log.info(proc_res+"");
            log.info("ods数据同步完成");
        }
        catch (Exception e){
            //捕捉错误
            log.error(e.getMessage());
            msg = e.getMessage();
        }
        res = BusinessResult.of(re, msg, proc_res);
        return res;
    }

}
