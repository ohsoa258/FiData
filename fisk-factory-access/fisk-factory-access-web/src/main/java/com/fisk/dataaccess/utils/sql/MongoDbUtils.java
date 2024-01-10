package com.fisk.dataaccess.utils.sql;

import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.mongodb.MongoClient;
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

    public List<TablePyhNameDTO> getTrueTableNameList(MongoClient mongoClient, String conDbname) {
        List<TablePyhNameDTO> list = new ArrayList<>();
        try {
            //库名
            MongoDatabase database = mongoClient.getDatabase(conDbname);
            //获取集合名（表名）
            String tableName = "_schema";
            //根据collection名获取collection
            MongoCollection<Document> collection = database.getCollection(tableName);

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
                    dto.sourceDbName = conDbname;
                    if ("_id".equals(fieldName)) {
                        dto.isPk = 1;
                    } else {
                        dto.isPk = 0;
                    }
                    tb_columns.add(dto);
                }
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(conDbname + "." + tblName);
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
}
