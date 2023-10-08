package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.jcoutils.MyDestinationDataProvider;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.dataaccess.dto.sapbw.ProviderAndDestination;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.util.Properties;

/**
 * @author JianWenYang
 */
@Slf4j
public class DbConnectionHelper {

    public static Connection connection(String connectionStr, String acc, String pwd, DataSourceTypeEnum type) {
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        return commonDbHelper.connection(connectionStr, acc, pwd, type);
    }

    /**
     * 获取连接sapbw的对象
     *
     * @return
     */
    public static ProviderAndDestination myDestination(String host, String sysNr, String port, String connectAccount, String connectPwd, String lang) {
        ProviderAndDestination providerAndDestination = new ProviderAndDestination();
        Properties connProps = new Properties();
        connProps.setProperty(DestinationDataProvider.JCO_ASHOST, host); //服务器
        connProps.setProperty(DestinationDataProvider.JCO_SYSNR, sysNr); //系统编号 一般是00
        connProps.setProperty(DestinationDataProvider.JCO_CLIENT, port); //SAP客户端
        connProps.setProperty(DestinationDataProvider.JCO_USER, connectAccount); //用户名
        connProps.setProperty(DestinationDataProvider.JCO_PASSWD, connectPwd); //密码
        connProps.setProperty(DestinationDataProvider.JCO_LANG, lang); //登录语言
        // 配置jco连接池
        connProps.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "10"); //最大连接数
        connProps.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "20"); //最大连接线程

        MyDestinationDataProvider myProvider = new MyDestinationDataProvider();
        myProvider.addDestination("SAPBW", connProps);
        log.info("注册SAPBW驱动程序前...");
        // 创建JCo连接
        JCoDestination destination = null;
        try {
            Environment.registerDestinationDataProvider(myProvider);
            destination = JCoDestinationManager.getDestination("SAPBW");
            // 测试连接
            destination.ping();
            log.info("注册SAPBW驱动程序后...");
          //如果报这个错，
          // java.lang.IllegalStateException: DestinationDataProvider already registered [com.fisk.common.core.utils.jcoutils.MyDestinationDataProvider
          // 意味着该连接信息已经注册过 则直接使用即可
        } catch (IllegalStateException e) {
            log.info("该连接信息已经注册过 则直接使用即可");
            providerAndDestination.setMyProvider(myProvider);
            try {
                destination = JCoDestinationManager.getDestination("SAPBW");
                // 测试连接
                log.info("【该连接信息已经注册过】--测试连接");
                destination.ping();
                log.info("【该连接信息已经注册过】-- 测试连接成功");
            } catch (JCoException ex) {
                Environment.unregisterDestinationDataProvider(myProvider);
                log.error("【该连接信息已经注册过】--获取sapbw连接对象失败..");
                throw new FkException(ResultEnum.SAPBW_CONNECT_ERROR, e);
            }
            providerAndDestination.setDestination(destination);
            return providerAndDestination;
        } catch (JCoException e) {
            Environment.unregisterDestinationDataProvider(myProvider);
            log.error("获取sapbw连接对象失败..");
            throw new FkException(ResultEnum.SAPBW_CONNECT_ERROR, e);
        }
        providerAndDestination.setMyProvider(myProvider);
        providerAndDestination.setDestination(destination);
        return providerAndDestination;
    }


}
