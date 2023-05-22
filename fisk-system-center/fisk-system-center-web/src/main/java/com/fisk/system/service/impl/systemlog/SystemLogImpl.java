package com.fisk.system.service.impl.systemlog;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.dto.systemlog.SystemLogDTO;
import com.fisk.system.enums.systemlog.SystemLogEnum;
import com.fisk.system.service.systemlog.SystemLog;
import com.fisk.system.vo.systemlog.SystemLogVO;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author lishiji
 */
@Service
public class SystemLogImpl implements SystemLog {

    /**
     * 获取某个服务当天的所有日志列表
     *
     * @param dto data transfer object
     * @return 日志信息集合
     */
    @Override
    public List<String> getSystemLogNames(SystemLogDTO dto) {
        //获取服务类型
        int serviceType = dto.serviceType;
//        //获取排序方式 0倒序 1正序
//        int orderBy = dto.orderBy;
        //获取要查询哪天的日志
        String date = dto.date;

        //调用封装的方法，获取当前服务类型的日志文件包路径
        String logHomePath = getLogHomePath(serviceType);
        //调用封装的方法，获取当前服务类型的当天的日志文件名
        String logName = getLogName(serviceType);

        //获取到该路径下的所有文件
        File file = new File(logHomePath);
        File[] files = file.listFiles();

        //获取当前时间
        Date dateNow = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String now = simpleDateFormat.format(dateNow);

        //新建集合预装载日志文件名
        List<String> logNames = new ArrayList<>();

        if (files != null) {
            //遍历数组，进行io读入操作
            for (File log : files) {
                //获取文件名
                String name = log.getName();
                //对比传入的时间参数，判断是否需要的是当天的日志
                if (now.equals(date)) {
                    //如果是获取当天日志，获取当前所有日志名称
                    String finalLogName = logName.substring(logName.lastIndexOf("/") + 1);
                    //获取名字不带日期的日志
                    if (name.equals(finalLogName)) {
                        logNames.add(log.getName());
                    }else if (name.contains(date)){
                        //获取名字带日期的日志
                        logNames.add(log.getName());
                    }
                } else {
                    //若不是查询当天日志，直接获取包含要查询日期的日志名即可
                    if (name.contains(date)) logNames.add(log.getName());
                }
            }
        } else {
            throw new FkException(ResultEnum.LOG_NOT_EXISTS);
        }
        return logNames;
    }

    /**
     * 获取某个服务某天的所有日志
     *
     * @param logName
     * @return 日志信息集合
     */
    @Override
    public SystemLogVO getSystemLogBylogName(Integer serviceType, String logName) {
        //获取日志包路径
        String logHomePath = getLogHomePath(serviceType);
        //拼接完整路径
        String fullPath = logHomePath + logName;
        return IOInput(fullPath);
    }

    /**
     * 提取io读入操作方法
     *
     * @param logName
     * @return
     */
    public SystemLogVO IOInput(String logName) {
        File file = new File(logName);
        if (file.length() == 0) {
            throw new FkException(ResultEnum.LOG_NOT_EXISTS);
        }
        //提取BufferedReader对象，方便关流
        BufferedReader reader = null;
        //新建集合，预装载日志
        List<String> oneLog = new ArrayList<>();
        //用来表示每一个日志文件
        SystemLogVO systemLogVO = new SystemLogVO();
        //获取日志名称
        systemLogVO.logName = file.getName();
        //获取文件最后更新时间
        long l = file.lastModified();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        systemLogVO.lastUpdateTime = format.format(new Date(l));

        try {
            reader = new BufferedReader(new FileReader(file));
            //用来判断日志是否读完毕
            String tempString = null;
            //可以记录行数
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                String logLine = reader.readLine();
                oneLog.add(logLine);
                line++;
            }
            systemLogVO.logList = oneLog;
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return systemLogVO;
    }

    /**
     * 获取日志根包路径
     *
     * @param serviceType
     * @return
     */
    public String getLogHomePath(int serviceType) {
        if (serviceType == SystemLogEnum.FISK_API_SERVEICE.getValue()) {
            return SystemLogEnum.FISK_API_SERVEICE.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_CONSUME_SERVEICE.getValue()) {
            return SystemLogEnum.FISK_CONSUME_SERVEICE.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_CONSUME_VISUAL.getValue()) {
            return SystemLogEnum.FISK_CONSUME_VISUAL.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_DATAMANAGE_CENTER.getValue()) {
            return SystemLogEnum.FISK_DATAMANAGE_CENTER.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_FACTORY_ACCESS.getValue()) {
            return SystemLogEnum.FISK_FACTORY_ACCESS.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_FACTORY_DISPATCH.getValue()) {
            return SystemLogEnum.FISK_FACTORY_DISPATCH.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_FACTORY_MODEL.getValue()) {
            return SystemLogEnum.FISK_FACTORY_MODEL.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_FRAMEWORK_AUTHORIZATION.getValue()) {
            return SystemLogEnum.FISK_FRAMEWORK_AUTHORIZATION.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_FRAMEWORK_GATEWAY.getValue()) {
            return SystemLogEnum.FISK_FRAMEWORK_GATEWAY.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_FRAMEWORK_REGISTRY.getValue()) {
            return SystemLogEnum.FISK_FRAMEWORK_REGISTRY.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_GOVERNANCE_CENTER.getValue()) {
            return SystemLogEnum.FISK_GOVERNANCE_CENTER.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_LICENSE_REGISTRY.getValue()) {
            return SystemLogEnum.FISK_LICENSE_REGISTRY.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_MDM_MODEL.getValue()) {
            return SystemLogEnum.FISK_MDM_MODEL.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_SYSTEM_CENTER.getValue()) {
            return SystemLogEnum.FISK_SYSTEM_CENTER.getLogHome();
        } else if (serviceType == SystemLogEnum.FISK_TASK_CENTER.getValue()) {
            return SystemLogEnum.FISK_TASK_CENTER.getLogHome();
        } else {
            throw new FkException(ResultEnum.SERVICE_NOT_EXISTS);
        }
    }

    /**
     * 获取当天日志名
     *
     * @param serviceType
     * @return
     */
    public String getLogName(int serviceType) {
        if (serviceType == SystemLogEnum.FISK_API_SERVEICE.getValue()) {
            return SystemLogEnum.FISK_API_SERVEICE.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_CONSUME_SERVEICE.getValue()) {
            return SystemLogEnum.FISK_CONSUME_SERVEICE.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_CONSUME_VISUAL.getValue()) {
            return SystemLogEnum.FISK_CONSUME_VISUAL.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_DATAMANAGE_CENTER.getValue()) {
            return SystemLogEnum.FISK_DATAMANAGE_CENTER.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_FACTORY_ACCESS.getValue()) {
            return SystemLogEnum.FISK_FACTORY_ACCESS.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_FACTORY_DISPATCH.getValue()) {
            return SystemLogEnum.FISK_FACTORY_DISPATCH.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_FACTORY_MODEL.getValue()) {
            return SystemLogEnum.FISK_FACTORY_MODEL.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_FRAMEWORK_AUTHORIZATION.getValue()) {
            return SystemLogEnum.FISK_FRAMEWORK_AUTHORIZATION.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_FRAMEWORK_GATEWAY.getValue()) {
            return SystemLogEnum.FISK_FRAMEWORK_GATEWAY.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_FRAMEWORK_REGISTRY.getValue()) {
            return SystemLogEnum.FISK_FRAMEWORK_REGISTRY.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_GOVERNANCE_CENTER.getValue()) {
            return SystemLogEnum.FISK_GOVERNANCE_CENTER.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_LICENSE_REGISTRY.getValue()) {
            return SystemLogEnum.FISK_LICENSE_REGISTRY.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_MDM_MODEL.getValue()) {
            return SystemLogEnum.FISK_MDM_MODEL.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_SYSTEM_CENTER.getValue()) {
            return SystemLogEnum.FISK_SYSTEM_CENTER.getLogName();
        } else if (serviceType == SystemLogEnum.FISK_TASK_CENTER.getValue()) {
            return SystemLogEnum.FISK_TASK_CENTER.getLogName();
        } else {
            throw new FkException(ResultEnum.SERVICE_NOT_EXISTS);
        }
    }

}
