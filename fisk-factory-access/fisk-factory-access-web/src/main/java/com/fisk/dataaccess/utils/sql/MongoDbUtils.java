package com.fisk.dataaccess.utils.sql;

import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class MongoDbUtils {

    /**
     * 强生--获取库下指定集合里面的表结构信息
     */
    private static final String COLLECTION_NAME = "_schema";

    public List<TablePyhNameDTO> getTrueTableNameList(MongoClient mongoClient) {
        List<TablePyhNameDTO> list = new ArrayList<>();
        try {
            //获取所有数据库名称
            for (String dbName : mongoClient.listDatabaseNames()) {
                //获取库
                MongoDatabase database = mongoClient.getDatabase(dbName);
                //根据collection名获取collection
                MongoCollection<Document> collection;

                try {
                    collection = database.getCollection(COLLECTION_NAME);
                } catch (Exception e) {
                    log.error("mongodb获取_schema信息失败，库名称：" + dbName);
                    log.error("mongodb获取_schema信息失败，原因：" + e.getMessage());
                    continue;
                }

                //查找collection中的所有数据
                for (Document document : collection.find()) {
                    String tblName = (String) document.get("table");
                    log.info("mongo表名：" + tblName);
                    List<Document> fields = (List<Document>) document.get("fields");
                    List<TableStructureDTO> tb_columns = new ArrayList<>();
                    for (Document field : fields) {
                        String fieldName = field.getString("name");
                        String fieldType = field.getString("type");
                        TableStructureDTO dto = new TableStructureDTO();
                        dto.fieldName = fieldName;
                        dto.fieldType = "STRING";
                        dto.sourceTblName = tblName;
                        dto.sourceDbName = dbName;
                        if ("_id".equals(fieldName)) {
                            dto.isPk = 1;
                        } else {
                            dto.isPk = 0;
                        }
                        tb_columns.add(dto);
                    }
                    TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                    tablePyhNameDTO.setTableName(dbName + "." + tblName);
                    tablePyhNameDTO.setFields(tb_columns);
                    list.add(tablePyhNameDTO);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("获取数据-入仓配置同步表失败:" + e);
            log.info("mongodb元数据信息" + list);
            return list;
        }
    }

    public List<TablePyhNameDTO> getTrueTableNameListByTarget(MongoClient mongoClient, String dbName) {
        List<TablePyhNameDTO> list = new ArrayList<>();
        try {
            //获取所有数据库名称
            //获取库
            MongoDatabase database = mongoClient.getDatabase(dbName);
            //根据collection名获取collection
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            //查找collection中的所有数据
            for (Document document : collection.find()) {
                String tblName = (String) document.get("table");
                log.info("mongo表名：" + tblName);
                List<Document> fields = (List<Document>) document.get("fields");
                List<TableStructureDTO> tb_columns = new ArrayList<>();
                for (Document field : fields) {
                    String fieldName = field.getString("name");
                    String fieldType = field.getString("type");
                    TableStructureDTO dto = new TableStructureDTO();
                    dto.fieldName = fieldName;
                    dto.fieldType = "STRING";
                    dto.sourceTblName = tblName;
                    dto.sourceDbName = dbName;
                    if ("_id".equals(fieldName)) {
                        dto.isPk = 1;
                    } else {
                        dto.isPk = 0;
                    }
                    tb_columns.add(dto);
                }
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(dbName + "." + tblName);
                tablePyhNameDTO.setFields(tb_columns);
                list.add(tablePyhNameDTO);
            }

            return list;
        } catch (Exception e) {
            log.error("获取数据-入仓配置同步表失败:" + e);
            log.info("mongodb元数据信息" + list);
            return list;
        }
    }

    public List<TableStructureDTO> getTrueTableNameListForOneTbl(MongoClient mongoClient, String conDbname, String tblName) {
        //库名
        MongoDatabase database = mongoClient.getDatabase(conDbname);
        //根据集合名（表名) 获取collection
        MongoCollection<Document> collection = database.getCollection(tblName);
        //查找collection中的所有数据
        Set<String> keys = new HashSet<>();
        List<TableStructureDTO> tb_columns = new ArrayList<>();
        //查找collection中的所有数据
        for (Document document : collection.find()) {
            for (String k : document.keySet()) {
                if (!keys.contains(k)) {
                    TableStructureDTO dto = new TableStructureDTO();
                    // 获取字段名称
                    dto.fieldName = k;
                    // 获取字段类型
                    dto.fieldType = "STRING";
                    dto.sourceTblName = tblName;
                    dto.sourceDbName = conDbname;
                    tb_columns.add(dto);
                    keys.add(k);
                }
            }
        }
        return tb_columns;
    }

    /**
     * hudi入仓配置 重新同步指定单个来源数据库对应库下的指定表信息到fidata平台配置库
     */
    public List<TableStructureDTO> getTrueTableNameListForReSync(MongoClient mongoClient, String tableName, String sourceDbName) {
        List<TableStructureDTO> tb_columns = new ArrayList<>();
        try {
            //获取库
            MongoDatabase database = mongoClient.getDatabase(sourceDbName);
            //根据collection名获取collection
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            //查找collection中的所有数据
            FindIterable<Document> documents = collection.find();
            for (Document document : documents) {
                String tblName = (String) document.get("table");
                log.info("mongo表名：" + tblName);
                //只获取要重新同步的表的结构
                if (!tableName.equals(tblName)) {
                    continue;
                }

                List<Document> fields = (List<Document>) document.get("fields");

                for (Document field : fields) {
                    String fieldName = field.getString("name");
                    TableStructureDTO dto = new TableStructureDTO();
                    dto.fieldName = fieldName;
                    dto.fieldType = "STRING";
                    dto.sourceTblName = tblName;
                    dto.sourceDbName = sourceDbName;
                    if ("_id".equals(fieldName)) {
                        dto.isPk = 1;
                    } else {
                        dto.isPk = 0;
                    }
                    tb_columns.add(dto);
                }
                break;
            }

            return tb_columns;
        } catch (Exception e) {
            log.error("获取数据-入仓配置同步表失败:" + e);
            return tb_columns;
        }
    }
}
