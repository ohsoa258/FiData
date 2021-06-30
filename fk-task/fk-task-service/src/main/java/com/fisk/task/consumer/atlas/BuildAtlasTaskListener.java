package com.fisk.task.consumer.atlas;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MQConstants;
import com.fisk.task.dto.atlas.ReceiveDataConfigDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.utils.DorisHelper;
import com.fisk.task.utils.WsSessionManager;
import com.fisk.task.utils.YamlReader;
import com.rabbitmq.client.Channel;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;

/**
 * @DennyHui
 */
@Component
@RabbitListener(queues = MQConstants.QueueConstants.BUILD_ATLAS_FLOW)
@Slf4j

public class BuildAtlasTaskListener {
    @RabbitHandler
    @MQConsumerLog
    public void msg(String settingid, Channel channel, Message message) {
        ReceiveDataConfigDTO dto = JSON.parseObject(settingid, ReceiveDataConfigDTO.class);
        String dorisconstr = YamlReader.instance.getValueByKey("dorisconstr.url").toString();
        String username = YamlReader.instance.getValueByKey("dorisconstr.username").toString();
        String pwd = YamlReader.instance.getValueByKey("dorisconstr.password").toString();
        String Driver = YamlReader.instance.getValueByKey("dorisconstr.driver_class_name").toString();
        System.out.println("dorisconstr:" + dorisconstr);
        System.out.println("username:" + username);
        System.out.println("pwd:" + pwd);
        System.out.println("Driver:" + Driver);
        Connection conn = null;
        Statement stmt = null;
        String sql=null;
        int result = 0;
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
            conn = DorisHelper.getConnection();
            // 2执行对象
            stmt = conn.createStatement();
            // 3执行
            result = stmt.executeUpdate(sql);

        } catch (Exception e) {
            //捕捉错误
            e.printStackTrace();
        } finally {
            //关闭操作对象
            DorisHelper.closeStatement(stmt);
            //关闭连接
            DorisHelper.closeConn(conn);
        }
    }
}
