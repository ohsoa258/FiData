package com.fisk.task.service.task.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.task.entity.TBETLIncrementalPO;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.service.task.ITBETLIncremental;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/4 20:57
 * Description:
 */
@Service
@Slf4j
public class TBETLIncrementalImpl extends ServiceImpl<TBETLIncrementalMapper, TBETLIncrementalPO>  implements ITBETLIncremental {
    @Resource
    TBETLIncrementalMapper tbetlIncrementalMapper;

    @Override
    public Map<String, String> converSql(String tableName, String sql, String driveType) {
        Map<String, String> paramMap = new HashMap<>();
        if(sql.contains(SystemVariableTypeEnum.START_TIME.getValue())||sql.contains(SystemVariableTypeEnum.END_TIME.getValue())){
            //task提供方法
            Map<String, Date> etlIncremental = tbetlIncrementalMapper.getEtlIncrementalByTableName(tableName);
            if (etlIncremental != null) {
                Date startTime = etlIncremental.get(SystemVariableTypeEnum.START_TIME.getName());
                Date endTime = etlIncremental.get(SystemVariableTypeEnum.END_TIME.getName());
                if (startTime != null) {
                    String startDate = getStringDate(startTime);
                    sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), startDate);
                    paramMap.put(SystemVariableTypeEnum.START_TIME.getValue(), startDate);
                } else {
                    sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "0000-00-00");
                    paramMap.put(SystemVariableTypeEnum.START_TIME.getValue(), "0000-00-00");
                }
                if (endTime != null) {
                    String endDate = getStringDate(endTime);
                    sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), endDate);
                    paramMap.put(SystemVariableTypeEnum.END_TIME.getValue(), endDate);
                } else {
                    sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "0000-00-00");
                    paramMap.put(SystemVariableTypeEnum.END_TIME.getValue(), "0000-00-00");
                }
            } else {
                sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "0000-00-00");
                sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "0000-00-00");
                paramMap.put(SystemVariableTypeEnum.END_TIME.getValue(), "0000-00-00");
                paramMap.put(SystemVariableTypeEnum.START_TIME.getValue(), "0000-00-00");
            }
        }
        paramMap.put(SystemVariableTypeEnum.QUERY_SQL.getValue(),sql);
        return  paramMap;
    }
      public  String getStringDate(Date date) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(date);
            return dateString;
         }

    @Override
    public void addEtlIncremental(String tableName) {
        TBETLIncrementalPO ETLIncremental=new TBETLIncrementalPO();
        ETLIncremental.object_name=tableName;
        ETLIncremental.enable_flag="1";
        ETLIncremental.incremental_objectivescore_batchno= UUID.randomUUID().toString();
        Map<String, Object> conditionHashMap = new HashMap<>();
        conditionHashMap.put("object_name",ETLIncremental.object_name);
        List<TBETLIncrementalPO> tbetlIncrementalPos = tbetlIncrementalMapper.selectByMap(conditionHashMap);
        if(tbetlIncrementalPos!=null&&tbetlIncrementalPos.size()>0){
            log.info("此表已有同步记录,无需重复添加");
        }else{
            tbetlIncrementalMapper.insert(ETLIncremental);
        }
    }


}
