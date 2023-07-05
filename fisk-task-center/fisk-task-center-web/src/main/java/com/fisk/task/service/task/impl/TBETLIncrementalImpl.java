package com.fisk.task.service.task.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.task.entity.TBETLIncrementalPO;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.service.task.ITBETLIncremental;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
public class TBETLIncrementalImpl extends ServiceImpl<TBETLIncrementalMapper, TBETLIncrementalPO> implements ITBETLIncremental {
    @Resource
    TBETLIncrementalMapper tbetlIncrementalMapper;

    @Override
    public Map<String, String> converSql(String tableName, String sql, String driveType, String deltaTime) {
        List<DeltaTimeDTO> deltaTimes = JSON.parseArray(deltaTime, DeltaTimeDTO.class);
        Map<String, String> paramMap = new HashMap<>();
        List<TBETLIncrementalPO> list = new ArrayList<>();
        TBETLIncrementalPO tbetlIncremental = new TBETLIncrementalPO();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String defaultName = "未命名";
        Date d = new Date();
        Map<String, Date> etlIncremental = tbetlIncrementalMapper.getEtlIncrementalByTableName(tableName);
        List<TBETLIncrementalPO> etlIncrementalList = tbetlIncrementalMapper.getEtlIncrementalList(tableName);
        try {
            //---------------------------------------------------------
            // 如果开始时间是表达式,就用表达式,如果开始时间是常量,需要判断是否是第一次同步,如果是第一次同步就用常量,不是第一次同步用查到的
            log.info("时间增量值:{}", JSON.toJSONString(deltaTimes));
            if (!CollectionUtils.isEmpty(deltaTimes)) {
                DeltaTimeDTO startDeltaTime = deltaTimes.get(0);
                DeltaTimeDTO endDeltaTime = deltaTimes.get(1);
                //如果startDeltaTime不为空并且startDeltaTime的值为变量，并且变量值不为空
                if (Objects.nonNull(startDeltaTime) && Objects.equals(startDeltaTime.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.VARIABLE) &&
                        StringUtils.isNotEmpty(startDeltaTime.variableValue)) {
                    //替换sql内所有的@start_time为 'startDeltaTime.variableValue'  变量值
                    sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "'" + startDeltaTime.variableValue + "'");
                }
                //如果endDeltaTime不为空并且endDeltaTime的值为变量，并且变量值不为空
                if (Objects.nonNull(endDeltaTime) && Objects.equals(endDeltaTime.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.VARIABLE) &&
                        StringUtils.isNotEmpty(endDeltaTime.variableValue)) {
                    //替换sql内所有的@end_time为 'endDeltaTime.variableValue'  变量值
                    sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "'" + endDeltaTime.variableValue + "'");
                }

                //如果etlIncrementalList为空
                if (CollectionUtils.isEmpty(etlIncrementalList)) {
                    //如果endDeltaTime不为空并且endDeltaTime的值类型为常量，并且常量值不为空
                    if (Objects.nonNull(endDeltaTime) && Objects.equals(endDeltaTime.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.CONSTANT) &&
                            StringUtils.isNotEmpty(endDeltaTime.variableValue)) {
                        //将@end_time替换为常量值
                        sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "'" + endDeltaTime.variableValue + "'");
                    }
                    if (Objects.nonNull(startDeltaTime) && Objects.equals(startDeltaTime.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.CONSTANT) &&
                            StringUtils.isNotEmpty(startDeltaTime.variableValue)) {
                        //将@start_time替换为常量值
                        sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "'" + startDeltaTime.variableValue + "'");
                    }
                }

            }

            //---------------------------------------------------------
            //如果sql仍然包含@start_time或@end_time
            if (sql.contains(SystemVariableTypeEnum.START_TIME.getValue()) || sql.contains(SystemVariableTypeEnum.END_TIME.getValue())) {
                //task提供方法
                //如果etlIncremental不为空，即当前发布的表在task库中的tb_etl_Incremental表内有数据
                if (etlIncremental != null) {
                    //从tb_etl_Incremental表中获取当前表名的所有enable_flag为2的记录，enable_flag=2意味着：同步流程是否启动,1启动  2停止
                    list = this.query().eq("object_name", tableName)
                            .eq("enable_flag", 2).list();
                    Date startTime = etlIncremental.get(SystemVariableTypeEnum.START_TIME.getName());
                    Date endTime = etlIncremental.get(SystemVariableTypeEnum.END_TIME.getName());
                    //如果startTime不为空
                    if (startTime != null) {
                        //格式化时间字符串 yyyy-MM-dd HH:mm:ss
                        String startDate = getStringDate(startTime);
                        sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "'" + startDate + "'");
                        paramMap.put(SystemVariableTypeEnum.START_TIME.getValue(), startDate);
                    } else {
                        //如果startTime为空，将@start_time设置为默认值：1753-01-01 00:00:00
                        sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "'" + NifiConstants.AttrConstants.INITIAL_TIME + "'");
                        paramMap.put(SystemVariableTypeEnum.START_TIME.getValue(), NifiConstants.AttrConstants.INITIAL_TIME);
                    }

                   /* if (endTime != null) {
                        String endDate = getStringDate(endTime);
                        sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "'" + endDate + "'");
                        paramMap.put(SystemVariableTypeEnum.END_TIME.getValue(), endDate);
                    } else {*/

                    sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "'" + formatter.format(d) + "'");
                    paramMap.put(SystemVariableTypeEnum.END_TIME.getValue(), formatter.format(d));
                    //}
                } else {
                    //如果etlIncremental为空，即当前发布的表在task库中的tb_etl_Incremental表内没有数据
                    sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "'" + NifiConstants.AttrConstants.INITIAL_TIME + "'");
                    sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "'" + formatter.format(d) + "'");
                    paramMap.put(SystemVariableTypeEnum.END_TIME.getValue(), formatter.format(d));
                    paramMap.put(SystemVariableTypeEnum.START_TIME.getValue(), NifiConstants.AttrConstants.INITIAL_TIME);
                    Date parse = formatter.parse(NifiConstants.AttrConstants.INITIAL_TIME);
                    tbetlIncremental.incrementalObjectivescoreEnd = parse;
                    tbetlIncremental.incrementalObjectivescoreStart = parse;
                    list.add(tbetlIncremental);
                }
            } else {
                ////如果sql不包含@start_time或@end_time
                Date parse = formatter.parse(NifiConstants.AttrConstants.INITIAL_TIME);
                tbetlIncremental.incrementalObjectivescoreEnd = parse;
                tbetlIncremental.incrementalObjectivescoreStart = parse;
                list.add(tbetlIncremental);
            }

            paramMap.put(SystemVariableTypeEnum.HISTORICAL_TIME.getValue(), JSON.toJSONString(list));
            paramMap.put(SystemVariableTypeEnum.QUERY_SQL.getValue(), sql);
            log.info("map返回:" + JSON.toJSONString(paramMap));
            return paramMap;
        } catch (Exception e) {
            log.error("拼装语句报错:" + StackTraceHelper.getStackTraceInfo(e));
            throw new FkException(ResultEnum.ERROR);
        }
    }

    public String getStringDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    @Override
    public void addEtlIncremental(String tableName) {
        TBETLIncrementalPO ETLIncremental = new TBETLIncrementalPO();
        ETLIncremental.objectName = tableName;
        ETLIncremental.enableFlag = "1";
        ETLIncremental.incrementalObjectivescoreBatchno = UUID.randomUUID().toString();
        Map<String, Object> conditionHashMap = new HashMap<>();
        conditionHashMap.put("object_name", ETLIncremental.objectName);
        List<TBETLIncrementalPO> tbetlIncrementalPos = tbetlIncrementalMapper.selectByMap(conditionHashMap);
        if (tbetlIncrementalPos != null && tbetlIncrementalPos.size() > 0) {
            log.info("此表已有同步记录,无需重复添加");
        } else {
            tbetlIncrementalMapper.insert(ETLIncremental);
        }
    }

}
