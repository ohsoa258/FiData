package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.core.utils.jcoutils.MyDestinationDataProvider;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.sapbw.ProviderAndDestination;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.daconfig.DataSourceConfig;
import com.fisk.task.dto.daconfig.SapBwConfig;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.nifi.ISapBwListener;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.utils.StackTraceHelper;
import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author lsj
 */
@Service
@Slf4j
public class SapBwListenerImpl implements ISapBwListener {

    @Resource
    DataAccessClient client;
    @Resource
    UserClient userClient;
    @Value("${fiData-data-ods-source}")
    private String dataSourceOdsId;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;

    /**
     * sapbw-Java代码同步
     *
     * @param data
     * @return
     */
    @Override
    public ResultEnum sapBwToStg(String data) {
        log.info("sapbw-Java代码同步参数:{}", data);
        Connection conn = null;
        PreparedStatement pstmt = null;
        MyDestinationDataProvider myProvider = null;

        try {
            KafkaReceiveDTO kafkaReceive = JSON.parseObject(data, KafkaReceiveDTO.class);
            //获取topic
            String topic = kafkaReceive.topic;
            //获取大批次号
            String fidata_batch_code = kafkaReceive.fidata_batch_code;
            log.info("大批次号：{}", fidata_batch_code);

            String[] topicParameter = topic.split("\\.");
            //应用id
            String appId = "";
            //表id
            String tableId = "";
            if (Objects.equals(topicParameter.length, 6)) {
                appId = topicParameter[4];
                tableId = topicParameter[5];
            } else if (Objects.equals(topicParameter.length, 7)) {
                appId = topicParameter[5];
                tableId = topicParameter[6];
            }

            //远程调用数据接入的接口，获取数据接入配置项
            ResultEntity<DataAccessConfigDTO> dataAccessConfig = client.dataAccessConfig(Long.parseLong(tableId), Long.parseLong(appId));
            if (dataAccessConfig.code == ResultEnum.SUCCESS.getCode()) {
                //获取数据接入配置项
                DataAccessConfigDTO config = dataAccessConfig.data;
                //获取目标源jdbc连接
                DataSourceConfig targetDsConfig = config.targetDsConfig;
                //获取sapbw配置信息
                SapBwConfig sapBwConfig = config.sapBwConfig;
                //mdx语句
                List<String> mdxList = sapBwConfig.getMdxList();

                //通过表id,应用id,表类别从 tb_table_nifi_setting 表获取 TableNifiSettingPO 对象
                TableNifiSettingPO one = tableNifiSettingService.query().eq("table_access_id", tableId).eq("app_id", appId).eq("type", OlapTableEnum.PHYSICS.getValue()).one();
                //获取表名
                targetDsConfig.targetTableName = one.tableName;
                //获取物理表字段集合
                List<TableFieldsDTO> tableFieldsList = targetDsConfig.tableFieldsList;

                //获取sapbw连接
                ProviderAndDestination providerAndDestination =
                        myDestination(sapBwConfig.host, sapBwConfig.sysNr, sapBwConfig.port,
                                sapBwConfig.connectAccount, sapBwConfig.connectPwd, sapBwConfig.lang);
                JCoDestination destination = providerAndDestination.getDestination();
                myProvider = providerAndDestination.getMyProvider();

                //调用封装的方法 执行mdx语句并获得结果
                List<List<String>> lists = excuteMdx(destination, myProvider, mdxList);

                //获取列总数
                int columnCount = tableFieldsList.size();

                // 构造 SQL 语句，? 代表需要填充的数据
                //调用方法获取stg表名和目标表名
                List<String> stgAndTableName = TableNameGenerateUtils.getStgAndTableName(targetDsConfig.targetTableName);
                //stgAndTableName.get(0) 是stg表名，即 sqlBuilder 语句是往stg表里面插入数据
                StringBuilder sqlBuilder = new StringBuilder("INSERT INTO " + stgAndTableName.get(0) + " (fidata_batch_code,");
                //fori循环，目的是遍历tableFieldsList1，获取每个字段的字段名
                for (int i = 0; i < columnCount; i++) {
                    if (i > 0) {
                        sqlBuilder.append(", ");
                    }
                    sqlBuilder.append("[").append(tableFieldsList.get(i).fieldName).append("]");
                }
                sqlBuilder.append(") VALUES ('")
                        .append(fidata_batch_code)
                        .append("',");

                //将要插入的数据以占位符 ? 替代
                for (int i = 0; i < columnCount; i++) {
                    if (i > 0) {
                        sqlBuilder.append(", ");
                    }
                    //将要插入的数据以占位符 ? 替代
                    sqlBuilder.append("?");
                }
                sqlBuilder.append(")");
                log.info("预编译的sql语句为：{}", sqlBuilder);
                log.info("预编译sql的占位符个数为：{}", columnCount);

                //远程调用，调用系统管理的接口从dmp_system_db库，tb_datasource_config表中获取ods数据源的信息  即dataSourceOdsId = 2
                ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
                if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                    DataSourceDTO dataSource = fiDataDataSource.data;
                    // 创建连接
                    conn = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
                    // 预编译 SQL 语句
                    pstmt = conn.prepareStatement(sqlBuilder.toString());
                    // 循环插入数据
                    int batchSize = 1000; // 每批次插入的数据条数
                    int count = 0;
//                    int excelRowCount = 1;
                    assert lists != null;
                    for (List list : lists) {

//                        log.info("excel第" + excelRowCount + "行数据个数：{}", list.size());
                        for (int i = 0; i < list.size(); i++) {
                            //列数和占位符必须匹配
                            if (i >= columnCount) break;
                            Object object = list.get(i);

                            if (object != null) {
                                pstmt.setString(i + 1, object.toString());
                            } else {
                                pstmt.setString(i + 1, null);
                            }
                        }
                        pstmt.addBatch();
//                        excelRowCount++;
                        count++;
                        if (count % batchSize == 0) {
                            pstmt.executeBatch();
                        }
                    }
                    pstmt.executeBatch(); // 执行剩余的数据

                } else {
                    log.error("userclient无法查询到ods库的连接信息");
                    throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL, "userclient无法查询到ods库的连接信息");
                }
            }
        } catch (Exception e) {
            log.error("sapbw-Java代码同步报错" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.SAPBW_NIFI_SYNC_ERROR;
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
                if (myProvider != null) {
                    Environment.unregisterDestinationDataProvider(myProvider);
                }
            } catch (Exception e) {
                log.error(StackTraceHelper.getStackTraceInfo(e));
            }
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * sapbw:执行mdx语句，获取数据并返回
     *
     * @param destination
     * @param myProvider
     * @return
     */
    private List<List<String>> excuteMdx(JCoDestination destination, MyDestinationDataProvider myProvider, List<String> mdxList) {
        List<List<String>> allData = new ArrayList<>();
        List<FieldNameDTO> fieldNameDTOS = new ArrayList<>();

        log.info("待执行的mdx语句:[{}]", mdxList);

//        //将前端传递的mdx语句截取为每段50长度的字符串
//        int segmentLength = 50; // 每段字符串的长度
//        //存储每段mdx语句
//        List<String> segments = new ArrayList<>();
//        for (int i = 0; i < mdx.length(); i += segmentLength) {
//            int endIndex = Math.min(i + segmentLength, mdx.length());
//            String segment = mdx.substring(i, endIndex);
//            segments.add(segment);
//        }

        String datasetid = null;

        try {
            // 为了执行多个RFC函数，我们需要开启上下文，这行代码至关重要！！！
            log.info("sap jco3:开启连接上下文");
            JCoContext.begin(destination);

            // 获取创建数据集的函数
            JCoFunction function_create = destination.getRepository().getFunction("BAPI_MDDATASET_CREATE_OBJECT");

            //获取入参列表
            JCoParameterList tableParams = function_create.getTableParameterList();
            try {
                JCoTable table = tableParams.getTable("COMMAND_TEXT");
                // 将每段要执行的mdx语句拼接起来
                for (String mdxByPart : mdxList) {
                    table.appendRow();
                    table.setValue("LINE", mdxByPart);
                }
                // 设置要执行的已处理的mdx语句作为参数COMMAND_TEXT
                tableParams.setValue("COMMAND_TEXT", table);
                function_create.getTableParameterList().setValue("COMMAND_TEXT", table);
                // 执行创建数据集的函数
                log.info("执行创建数据集的函数: BAPI_MDDATASET_CREATE_OBJECT");
                function_create.execute(destination);
                // 获取刚刚创建的数据集的id
                datasetid = String.valueOf(function_create.getExportParameterList().getValue("DATASETID"));

                // 定义第二个函数：查询刚刚创建的数据集的数据
                JCoFunction function_select = destination.getRepository().getFunction("BAPI_MDDATASET_SELECT_DATA");
                // 将刚创建的数据集的id作为我们查询的参数
                function_select.getImportParameterList().setValue("DATASETID", datasetid);
                // 执行查询函数
                log.info("执行查询函数: BAPI_MDDATASET_SELECT_DATA");
                function_select.execute(destination);

                // 定义第三个函数：查询刚刚创建的数据集的列明细（字段详情）
                JCoFunction function_getAxisInfo = destination.getRepository().getFunction("BAPI_MDDATASET_GET_AXIS_INFO");
                // 获取参数列表
                JCoParameterList axisInfoParam = function_getAxisInfo.getImportParameterList();
                // 设置参数
                axisInfoParam.setValue("DATASETID", datasetid);
                // 执行函数
                log.info("查询刚刚创建的数据集的列明细（字段详情）: BAPI_MDDATASET_GET_AXIS_INFO");
                function_getAxisInfo.execute(destination);

                JCoParameterList tableParameterList = function_getAxisInfo.getTableParameterList();
                // 获取字段详情
                JCoTable axisInfo = tableParameterList.getTable("AXIS_INFO");
                // 获取字段id
                List<String> axisList = new ArrayList<>();
                for (int i = 0; i < axisInfo.getNumRows(); i++) {
                    axisInfo.setRow(i);
                    String axis = axisInfo.getString("AXIS");
                    // 排除 000 和255 这两个无效列
                    if (!"000".equals(axis) && !"255".equals(axis)) {
                        axisList.add(axis);
                    }
                }

                // 定义第四个函数：通过数据集id和要查询的列id,查询刚刚创建的数据集的数据
                JCoFunction function_getData = destination.getRepository().getFunction("BAPI_MDDATASET_GET_AXIS_DATA");
                JCoParameterList param = function_getData.getImportParameterList();
                param.setValue("DATASETID", datasetid);
                // 设置查询数据的行数
//                param.setValue("START_TUPLE", "0");
//                param.setValue("END_TUPLE", "9");

                // unFormattedData集合装载每列数据
                List<String> unFormattedData = new ArrayList<>();
                // BAPI_MDDATASET_GET_AXIS_DATA在设置AXIS参数时，无法设置多个，因此采用这种方法去查询mdx语句里面的多列数据
                for (String s : axisList) {
                    // 设置axis参数，axis相当于每个字段
                    param.setValue("AXIS", s);
                    // 执行函数
                    log.info("通过数据集id和要查询的列id,查询刚刚创建的数据集的数据: BAPI_MDDATASET_GET_AXIS_DATA");
                    function_getData.execute(destination);

                    // 获取查询到的数据
                    JCoParameterList parameterList = function_getData.getTableParameterList();
                    JCoTable mndtryPrptys = parameterList.getTable("MNDTRY_PRPTYS");

                    String firstFieldName = null;
                    if (!mndtryPrptys.isEmpty()) {

                        for (int i = 0; i < mndtryPrptys.getNumRows(); i++) {
                            mndtryPrptys.setRow(i);
                            // 这一步是为了获取字段名称
                            if (i == 0) {
                                //格式形如：[YYLABOR].[LEVEL01]
                                firstFieldName = mndtryPrptys.getString("LVL_UNAM");
                                FieldNameDTO fieldNameDTO = new FieldNameDTO();
                                fieldNameDTO.setSourceFieldName(firstFieldName);
                                //todo:sapbw数据的源字段类型暂时全设置为NVARCHAR
                                fieldNameDTO.setSourceFieldType("NVARCHAR");
                                //todo:sapbw数据的目标字段长度暂时全设置为2000
                                fieldNameDTO.setFieldLength("2000");
                                //格式化目标字段名称 去掉[ ]  替换 . 为 _
                                String modifiedString = firstFieldName.replaceAll("[\\[\\]]", "")
                                        .replaceAll("\\.", "_");
                                fieldNameDTO.setFieldName(modifiedString);
                                fieldNameDTOS.add(fieldNameDTO);
                            }
                            if (!firstFieldName.equals(mndtryPrptys.getString("LVL_UNAM"))) {
                                FieldNameDTO fieldNameDTO = new FieldNameDTO();
                                String lvlUname = mndtryPrptys.getString("LVL_UNAM");
                                //格式化目标字段名称 去掉[ ]  替换 . 为 _
                                String modifiedString = lvlUname.replaceAll("[\\[\\]]", "")
                                        .replaceAll("\\.", "_");
                                fieldNameDTO.setSourceFieldName(lvlUname);
                                //todo:sapbw数据的源字段类型暂时全设置为NVARCHAR
                                fieldNameDTO.setSourceFieldType("NVARCHAR");
                                //todo:sapbw数据的目标字段长度暂时全设置为2000
                                fieldNameDTO.setFieldLength("2000");
                                fieldNameDTO.setFieldName(modifiedString);
                                fieldNameDTOS.add(fieldNameDTO);
                            } else {
                                break;
                            }
                        }

                        for (int i = 0; i < mndtryPrptys.getNumRows(); i++) {
                            mndtryPrptys.setRow(i);
                            String value = mndtryPrptys.getString("MEM_CAP");
                            unFormattedData.add(value);
                        }
                    }
                }

                // 执行查询后不管mdx语句查询多少列数据，返回的都在一列里面，因此我们需要根据列的个数对数据做处理
                int size = fieldNameDTOS.size();
                // 这一步是将unFormattedData集合里面装载的未处理的数据，转换为allData集合装载的处理过的数据
                allData = convertToRowData(unFormattedData, size);

                // 删除前面创建的数据集
                JCoFunction deleteObjectFunction = destination.getRepository().getFunction("BAPI_MDDATASET_DELETE_OBJECT");
                deleteObjectFunction.getImportParameterList().setValue("DATASETID", datasetid);
                log.info("删除前面创建的数据集: BAPI_MDDATASET_DELETE_OBJECT");
                deleteObjectFunction.execute(destination);

                // 结束连接上下文
                log.info("sap jco3:结束连接上下文");
                JCoContext.end(destination);
            } catch (Exception e) {
                if (datasetid != null) {
                    // 删除前面创建的数据集
                    JCoFunction deleteObjectFunction = destination.getRepository().getFunction("BAPI_MDDATASET_DELETE_OBJECT");
                    deleteObjectFunction.getImportParameterList().setValue("DATASETID", datasetid);
                    deleteObjectFunction.execute(destination);
                }
                log.error("sapbw执行mdx获取结果报错..");
                throw new FkException(ResultEnum.SAPBW_EXECUATE_MDX_ERROR, e);
            }
        } catch (JCoException e) {
            log.error("sapbw执行mdx获取结果报错..");
            throw new FkException(ResultEnum.SAPBW_EXECUATE_MDX_ERROR, e);
        } finally {
            Environment.unregisterDestinationDataProvider(myProvider);
        }
        return allData;
    }

    /**
     * 将集合里面装载的每列数据转换为装载每行数据的集合
     *
     * @param colList
     * @return
     */
    private static List<List<String>> convertToRowData(List<String> colList, int size) {
        List<List<String>> formedData = new ArrayList<>();
        List<String> sublist = new ArrayList<>();

        for (int i = 0; i < colList.size(); i++) {
            sublist.add(colList.get(i));

            if ((i + 1) % size == 0) {
                formedData.add(sublist);
                sublist = new ArrayList<>();
            }
        }

        // 处理剩余的元素，如果有的话
        if (!sublist.isEmpty()) {
            formedData.add(sublist);
        }
        return formedData;
    }

    /**
     * 获取连接sapbw的对象
     *
     * @return
     */
    private ProviderAndDestination myDestination(String host, String sysNr, String port, String connectAccount, String connectPwd, String lang) {
        ProviderAndDestination providerAndDestination = new ProviderAndDestination();
        Properties connProps = new Properties();
        connProps.setProperty(DestinationDataProvider.JCO_ASHOST, host);
        connProps.setProperty(DestinationDataProvider.JCO_SYSNR, sysNr);
        connProps.setProperty(DestinationDataProvider.JCO_CLIENT, port);
        connProps.setProperty(DestinationDataProvider.JCO_USER, connectAccount);
        connProps.setProperty(DestinationDataProvider.JCO_PASSWD, connectPwd);
        connProps.setProperty(DestinationDataProvider.JCO_LANG, lang);
        // 配置jco连接池
        connProps.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "10");
        connProps.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "20");

        MyDestinationDataProvider myProvider = new MyDestinationDataProvider();
        myProvider.addDestination("SAPBW", connProps);
        log.info("注册SAPBW驱动程序前...");
        Environment.registerDestinationDataProvider(myProvider);
        // 创建JCo连接
        JCoDestination destination = null;
        try {
            destination = JCoDestinationManager.getDestination("SAPBW");
        } catch (JCoException e) {
            log.error("获取sapbw连接对象失败..");
            throw new FkException(ResultEnum.SAPBW_CONNECT_ERROR, e);
        }
        providerAndDestination.setMyProvider(myProvider);
        providerAndDestination.setDestination(destination);
        return providerAndDestination;
    }

}
