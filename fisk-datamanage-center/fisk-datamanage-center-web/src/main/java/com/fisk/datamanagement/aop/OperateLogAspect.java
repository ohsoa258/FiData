package com.fisk.datamanagement.aop;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.service.metadata.dto.metadata.MetaDataColumnAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDbAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataTableAttributeDTO;
import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.datamanagement.entity.MetaDataEntityOperationLogPO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import com.fisk.datamanagement.map.MetaDataEntityOperationLogMap;
import com.fisk.datamanagement.mapper.MetadataEntityMapper;
import com.fisk.datamanagement.service.IMetaDataEntityOperationLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-08 13:58
 * @description AOP 切面类
 */
@Aspect
@Component
public class OperateLogAspect {
    @Resource
    private IMetaDataEntityOperationLog entityOperationLog;

    @Resource
    private MetadataEntityMapper entityMapper;

    @Resource
    private UserHelper userHelper;
    /**
     * 对所有使用@OperateLog注解的方法进行拦截
     */
    @Pointcut("@annotation(com.fisk.datamanagement.aop.OperateLog)")
    private void pointcut(){
    }

    // 定义环绕在切点前后的操作
    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint point) {
        Object result = null;

        try {
            // 调用方法，完成具体逻辑
            result = point.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }finally {
            // 保存操作日志
            saveLog(point);
        }


        return result;
    }

    private void saveLog(ProceedingJoinPoint point) {
        MetaDataEntityOperationLogDTO sysLog=null;

        UserInfo userInfo = userHelper.getLoginUserInfo();

        // 从切点获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        // 获取方法
        Method method = signature.getMethod();
        // 获取日志注解
        OperateLog annotation = method.getAnnotation(OperateLog.class);

        // 获取日志注解上的描述，并且设置日志行为描述
        String opertionType=null;
        if (annotation != null) {
            opertionType=annotation.value();
        }
        //sysLog.setBeforeChange(methodName);

        // 获取参数
        Object[] args = point.getArgs();

        //通过限定名拿到对应元数据的实体类 可能会有多个表
        List<String> tableQNS= new ArrayList<>();

        //那到对应表中的一个或多个列
        List<String> columnS = new ArrayList<>();

        List<Object> objectList = Arrays.asList(args);
        for (Object o : objectList) {
            //参数转换拿出对应的参数
            List<MetaDataInstanceAttributeDTO> list= (List<MetaDataInstanceAttributeDTO>) o;
            for (MetaDataInstanceAttributeDTO meInDTO : list) {
                for (MetaDataDbAttributeDTO dbAttributeDTO : meInDTO.getDbList()) {
                    //拿出所有表
                    for (MetaDataTableAttributeDTO tableAttributeDTO : dbAttributeDTO.getTableList()) {
                        tableQNS.add(tableAttributeDTO.getQualifiedName());
                        //拿出每个表中的列
                        for (MetaDataColumnAttributeDTO columnAttributeDTO : tableAttributeDTO.getColumnList()) {
                            columnS.add(columnAttributeDTO.name);
                        }
                    }
                }
            }
        }
        //通过tableQNS 查出所有表的id
        //过滤掉stg 数据
        List<Long> ids= new ArrayList<>();
        MetadataEntityPO entityPO =null;
        for (String tableQN : tableQNS) {
            entityPO = new MetadataEntityPO();
            QueryWrapper<MetadataEntityPO> wrapper = new QueryWrapper<>();
            wrapper.eq("qualified_name", tableQN.replace("_stg", ""));
            entityPO = entityMapper.selectOne(wrapper);
            ids.add(entityPO.getId());
        }

        //过滤元数据库中已经有的字段
        List<String> column2 = new ArrayList<>();//保存过滤后的列
        for (Long id : ids) {
            QueryWrapper<MetadataEntityPO> wrapper = new QueryWrapper<>();
            wrapper.eq("parent_id",id);
            List<MetadataEntityPO> entityPOS = entityMapper.selectList(wrapper);
            entityPOS.forEach(System.out::println);
            for (MetadataEntityPO po : entityPOS) {
                for (String column : columnS) {
                    String columnNotStg =column.replace("stg_","");
                    if(!(columnNotStg).equals(po.getName())){
                        column2.add(columnNotStg);
                    }
                    break;
                }
            }


        }


        //入库参数 封装
        List<MetaDataEntityOperationLogDTO> listLog = new ArrayList<>();
        for (Long id : ids) {
            for (String column : column2) {
                sysLog = new MetaDataEntityOperationLogDTO();
                sysLog.setCreateUser(userInfo.getUsername());
                sysLog.setMetadataEntityId(id.toString());
                sysLog.setCreateTime(LocalDateTime.now());
                if(("新增").equals(opertionType)) {
                    sysLog.setBeforeChange("");
                    sysLog.setAfterChange(column);
                } else if (("删除").equals(opertionType)){
                    sysLog.setBeforeChange(column);
                    sysLog.setAfterChange("");
                }
                listLog.add(sysLog);
            }
        }
        listLog.forEach(System.out::println);
        //entityOperationLog.addOperationLog(new MetaDataEntityOperationLogDTO());
    }


}
