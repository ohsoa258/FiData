package com.fisk.dataaccess.utils.sql;

import com.fisk.dataaccess.dto.pbi.PBItemDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

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
                FindIterable<Document> documents;
                try {
                    collection = database.getCollection(COLLECTION_NAME);
                    documents = collection.find();
                } catch (Exception e) {
                    log.error("mongodb获取_schema信息失败，库名称：" + dbName);
                    log.error("mongodb获取_schema信息失败，原因：" + e.getMessage());
                    continue;
                }

                //查找collection中的所有数据
                for (Document document : documents) {
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

    /**
     * 获取mongodb指定数据库下的所有集合名
     *
     * @param mongoClient
     * @return
     */
    public List<TablePyhNameDTO> getCollectionsForAccess(com.mongodb.client.MongoClient mongoClient, String dbName) {
        List<TablePyhNameDTO> list = new ArrayList<>();
        try {
            //获取所有数据库名称
            //获取库
            MongoDatabase database = mongoClient.getDatabase(dbName);
            //根据collection名获取collection
            MongoIterable<String> collectionNames = database.listCollectionNames();

            for (String collectionName : collectionNames) {
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(collectionName);
                list.add(tablePyhNameDTO);
            }
            return list;
        } catch (Exception e) {
            log.error("获取mongodb指定数据库下的所有集合名失败:" + e);
            log.info("mongodb元数据信息" + list);
            return list;
        }
    }

    /**
     * 获取mongodb指定数据库下的所有集合名  不包含字段类型
     *
     * @param mongoClient
     * @return
     */
    public List<PBItemDTO> getDocumentsByCollection(com.mongodb.client.MongoClient mongoClient, String dbName, String collectionName) {
        List<PBItemDTO> list = new ArrayList<>();

        //获取所有数据库名称
        //获取库
        MongoDatabase database = mongoClient.getDatabase(dbName);
        //根据collection名获取collection
        MongoCollection<Document> collection = database.getCollection(collectionName);
        FindIterable<Document> documents = collection.find();
        HashSet<String> fieldNames = new HashSet<>();

        for (Document document : documents) {
            Set<String> keys = document.keySet();
            fieldNames.addAll(keys);
        }

        for (String fieldName : fieldNames) {
            PBItemDTO itemDTO = new PBItemDTO();
            itemDTO.setName(fieldName);
            list.add(itemDTO);
        }

        return list;

    }

    /**
     * 获取mongodb指定数据库下的所有集合名  不包含字段类型
     *
     * @param mongoClient
     * @return
     */
    public List<TableStructureDTO> getDocumentsByCollectionV2(com.mongodb.client.MongoClient mongoClient, String dbName, String collectionName) {
        List<TableStructureDTO> list = new ArrayList<>();

        //获取所有数据库名称
        //获取库
        MongoDatabase database = mongoClient.getDatabase(dbName);
        //根据collection名获取collection
        MongoCollection<Document> collection = database.getCollection(collectionName);
        FindIterable<Document> documents = collection.find();
        HashSet<String> fieldNames = new HashSet<>();

        for (Document document : documents) {
            Set<String> keys = document.keySet();
            fieldNames.addAll(keys);
        }

        for (String fieldName : fieldNames) {
            TableStructureDTO itemDTO = new TableStructureDTO();
            itemDTO.setFieldName(fieldName);
            list.add(itemDTO);
        }

        return list;

    }

    /**
     * 获取mongodb指定数据库下的所有集合名  包含字段类型
     *
     * @param mongoClient
     * @return
     */
    public List<PBItemDTO> getDocumentsByCollectionWithType(com.mongodb.client.MongoClient mongoClient, String dbName, String collectionName) {
        List<PBItemDTO> list = new ArrayList<>();

        //获取所有数据库名称
        //获取库
        MongoDatabase database = mongoClient.getDatabase(dbName);
        //根据collection名获取collection
        MongoCollection<Document> collection = database.getCollection(collectionName);
        FindIterable<Document> documents = collection.find();
        //用于存储字段名
        HashSet<String> fieldNames = new HashSet<>();
        // 用于存储字段类型
        Map<String, Set<String>> fieldTypeMap = new HashMap<>();

        //遍历文档，获取字段名和类型
        for (Document document : documents) {
            Set<String> keys = document.keySet();
            fieldNames.addAll(keys);
            extractFieldTypes(document, fieldTypeMap);
        }

        //装填对象
        for (String fieldName : fieldNames) {
            PBItemDTO itemDTO = new PBItemDTO();
            itemDTO.setType(fieldTypeMap.get(fieldName).toString());
            itemDTO.setName(fieldName);
            list.add(itemDTO);
        }
        return list;
    }

    /**
     * 获取mongodb指定集合里的文档的字段元数据map
     *
     * @param documents
     * @return
     */
    public Map<String, Set<String>> getMongoCollectionMetadata(FindIterable<Document> documents) {
        // 用于存储字段类型
        Map<String, Set<String>> fieldTypeMap = new HashMap<>();
        //遍历文档，获取字段名和类型
        for (Document document : documents) {
            extractFieldTypes(document, fieldTypeMap);
        }
        return fieldTypeMap;
    }

    /**
     * 获取mongodb指定数据库下的所有集合名  包含字段类型
     *
     * @param documents
     * @return
     */
    public List<PBItemDTO> extractDocuments(FindIterable<Document> documents) {
        List<PBItemDTO> list = new ArrayList<>();

        //用于存储字段名
        HashSet<String> fieldNames = new HashSet<>();
        // 用于存储字段类型
        Map<String, Set<String>> fieldTypeMap = new HashMap<>();

        //遍历文档，获取字段名和类型
        for (Document document : documents) {
            Set<String> keys = document.keySet();
            fieldNames.addAll(keys);
            extractFieldTypes(document, fieldTypeMap);
        }

        //装填对象
        for (String fieldName : fieldNames) {
            PBItemDTO itemDTO = new PBItemDTO();
            itemDTO.setType(fieldTypeMap.get(fieldName).toString());
            itemDTO.setName(fieldName);
            list.add(itemDTO);
        }
        return list;
    }

    /**
     * 测试获取mongodb某集合下的字段类型
     *
     * @param args
     */
    public static void main(String[] args) {
        // 创建 MongoDB 客户端
        try (com.mongodb.client.MongoClient mongoClient =
                     DbConnectionHelper.myMongoClient("192.168.21.21", 27017,
                             "admin", "fisk", "password01!")) {
            // 获取数据库
            MongoDatabase database = mongoClient.getDatabase("Fisk_Test_Mongodb");
            // 获取集合
            MongoCollection<Document> collection = database.getCollection("orders");

            // 用于存储字段类型
            Map<String, Set<String>> fieldTypeMap = new HashMap<>();

            // 遍历集合中的每个文档
            for (Document doc : collection.find()) {
                extractFieldTypes(doc, fieldTypeMap);
            }

            // 输出结果
            for (Map.Entry<String, Set<String>> entry : fieldTypeMap.entrySet()) {
                System.out.println("Field: " + entry.getKey() + ", Types: " + entry.getValue());
            }
        }
    }

    /**
     * 解析mongodb集合下的document类型
     *
     * @param doc
     * @param fieldTypeMap
     */
    private static void extractFieldTypes(Document doc, Map<String, Set<String>> fieldTypeMap) {
        for (String key : doc.keySet()) {
            Object value = doc.get(key);
            if (value instanceof Document) {
                addFieldType(fieldTypeMap, key, "Document");
                // 如果值是嵌套文档，递归处理
                extractFieldTypesForDoc((Document) value, fieldTypeMap, key);
            } else if (value instanceof List) {
                // 如果值是列表，遍历列表中的每个元素
                addFieldType(fieldTypeMap, key, "List");
                for (Object item : (List<?>) value) {
                    if (item instanceof Document) {
                        extractFieldTypes((Document) item, fieldTypeMap);
                    } else {
                        addFieldType(fieldTypeMap, key, item.getClass().getSimpleName());
                    }
                }
            } else {
                addFieldType(fieldTypeMap, key, value.getClass().getSimpleName());
            }
        }
    }

    /**
     * 解析嵌套document
     *
     * @param doc
     * @param fieldTypeMap
     * @param parentKey
     */
    private static void extractFieldTypesForDoc(Document doc, Map<String, Set<String>> fieldTypeMap, String parentKey) {
        for (String key : doc.keySet()) {
            Object value = doc.get(key);
            if (value instanceof Document) {
                addFieldType(fieldTypeMap, parentKey + "." + key, "Document");
                // 如果值是嵌套文档，递归处理
                extractFieldTypesForDoc((Document) value, fieldTypeMap, key);
            } else if (value instanceof List) {
                // 如果值是列表，遍历列表中的每个元素
                addFieldType(fieldTypeMap, parentKey + "." + key, "List");
                for (Object item : (List<?>) value) {
                    if (item instanceof Document) {
                        extractFieldTypes((Document) item, fieldTypeMap);
                    } else {
                        addFieldType(fieldTypeMap, key, item.getClass().getSimpleName());
                    }
                }
            } else {
                addFieldType(fieldTypeMap, parentKey + "." + key, value.getClass().getSimpleName());
            }
        }
    }

    /**
     * 向map内添加字段:字段类型
     *
     * @param fieldTypeMap
     * @param key
     * @param type
     */
    private static void addFieldType(Map<String, Set<String>> fieldTypeMap, String key, String type) {
        fieldTypeMap.computeIfAbsent(key, k -> new HashSet<>()).add(type);
    }

    /**
     * mongodb查询
     *
     * @param mongoClient
     * @param dbName
     * @param collectionName
     * @param queryCondition
     * @param neededFields
     * @return
     */
    public FindIterable<Document> mongodbQuery(com.mongodb.client.MongoClient mongoClient,
                                       String dbName,
                                       String collectionName,
                                       String queryCondition,
                                       String neededFields) {

        Bson queryFilter = null;
        Bson fields = null;
        FindIterable<Document> projection = null;
        List<Document> result = new ArrayList<>();

        // 获取数据库
        MongoDatabase database = mongoClient.getDatabase(dbName);
        // 获取集合
        MongoCollection<Document> collection = database.getCollection(collectionName);

        // 定义查询条件
        if (queryCondition != null && !queryCondition.trim().isEmpty()) {
            queryFilter = Document.parse(queryCondition);
        }

        // 定义投影
        if (neededFields != null && !neededFields.trim().isEmpty()) {
            fields = Document.parse(neededFields);
        }

        // 根据情况执行查询
        if (queryFilter == null && fields == null) {
            projection = collection.find();
        } else {
            if (queryFilter != null && fields != null) {
                projection = collection.find(queryFilter).projection(fields);
            } else if (queryFilter != null) {
                projection = collection.find(queryFilter);
            } else {
                projection = collection.find().projection(fields);
            }
        }

        // 处理结果
        for (Document document : projection) {
            result.add(document); // 将结果添加到列表中
        }

        return projection;
    }


}
