package com.fisk.task.service.doris.impl;

import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.utils.DorisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * @author:yhxu
 * CreateTime: 2021/7/1 11:55
 * Description:
 */
@Service
@Slf4j
public class DorisBuildImpl implements IDorisBuild {
    @Resource
    IDorisBuild doris;
    @Value("${datamodeldorisconstr.url}")
    private String dorisUrl;
    @Value("${datamodeldorisconstr.username}")
    private String dorisUser;
    @Value("${datamodeldorisconstr.password}")
    private String dorisPwd;
    @Value("${datamodeldorisconstr.driver_class_name}")
    private String dorisDriverName;


    @Override
    public BusinessResult dorisBuildTable(String executsql) {
        boolean re = false;
        String msg = null;
        String dorisconstr = dorisUrl;
        String username = dorisUser;
        String pwd = dorisPwd;
        String Driver = dorisDriverName;
        Connection conn = null;
        Statement stmt = null;
        //PreparedStatement和Statement的区别在于
        //PreparedStatement接口继承Statement，
        //PreparedStatement 实例包含已编译的 SQL 语句，所以其执行速度要快于 Statement 对象。
        //作为 Statement 的子类，PreparedStatement 继承了 Statement 的所有功能。
        //三种方法 execute、 executeQuery 和 executeUpdate 已被更改以使之不再需要参数
        //PreparedStatement性能更优，建议使用，但是比较复杂一点
        //Statement 是 Java 执行数据库操作的一个重要方法，用于在已经建立数据库连接的基础上，向数据库发送要执行的SQL语句
        //使用 Statement 对象执行语句
        // 访问数据库
        try {
            // 1获得连接
            conn = DorisHelper.getConnection(dorisconstr, Driver, username, pwd);
            // 2执行对象
            stmt = conn.createStatement();
            // 3执行
            stmt.executeUpdate(executsql);
            re = true;

        } catch (Exception e) {
            //捕捉错误
            log.error("系统异常" + e);
            msg = e.getMessage();
            throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
        } finally {
            //关闭操作对象
            DorisHelper.closeStatement(stmt);
            //关闭连接
            DorisHelper.closeConn(conn);
        }
        BusinessResult res = new BusinessResult(re, msg);
        return res;
    }



}
