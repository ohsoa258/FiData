package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class MongoDbUtils {

    public List<TablePyhNameDTO> getTrueTableNameList(MongoClient mongoClient, String conDbname) throws JSONException {
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
                log.info("mongo表名：" + tblName); // 移到循环外部的日志输出
                List<Document> fields = (List<Document>) document.get("fields");
                List<TableStructureDTO> tb_columns = new ArrayList<>();
                for (Document field : fields) { // 使用流式处理来减少对象创建
                    String fieldName = field.getString("name");
                    String fieldType = field.getString("type");
                    TableStructureDTO dto = new TableStructureDTO();
                    dto.fieldName = fieldName;
                    dto.fieldType = "STRING";
                    dto.sourceTblName = tblName;
                    dto.sourceDbName = conDbname;
                    if ("_id".equals(fieldName)) {
                        dto.isPk = 1;
                    }
                    tb_columns.add(dto);
                }
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(conDbname + "." + tblName);
                tablePyhNameDTO.setFields(tb_columns);
                list.add(tablePyhNameDTO);
            }
            //        //查找collection中的所有数据
//        for (Document document : collection.find()) {
//            for (String k : document.keySet()) {
//                if (!keys.contains(k)) {
//                    TableStructureDTO dto = new TableStructureDTO();
//                    // 获取字段名称
//                    dto.fieldName = k;
//                    // 获取字段类型
//                    dto.fieldType = "STRING";
//                    dto.sourceTblName = tableName;
//                    dto.sourceDbName = conDbname;
//                    tb_columns.add(dto);
//                    keys.add(k);
//                }
//            }
//            TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
//            tablePyhNameDTO.setTableName(conDbname + "." + tableName);
//            tablePyhNameDTO.setFields(tb_columns);
//            list.add(tablePyhNameDTO);
//        }

        } catch (Exception e) {
            log.error("获取数据-入仓配置同步表失败:" + e);
            throw new FkException(ResultEnum.ACCESS_HUDI_SYNC_ERROR, e.getMessage());
        }
        return list;
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
