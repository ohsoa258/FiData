package com.fisk.dataaccess.utils.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.dto.json.ApiTableDTO;
import com.fisk.dataaccess.dto.json.ApiTableDTO02;
import com.fisk.dataaccess.dto.json.JsonSchema;
import com.fisk.dataaccess.dto.json.JsonTableData;
import com.fisk.dataaccess.enums.FieldTypeEnum;
import com.fisk.dataaccess.utils.sql.PgsqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/18 17:38
 */
@Slf4j
public class JsonUtils {

    private static final String JSONSTR = "{\n" +
            "\t\"data\": [{\n" +
            "\t\t\"id\": 1,\n" +
            "\t\t\"name\": \"张三\",\n" +
            "\t\t\"age\": 16,\n" +
            "\t\t\"createTime\": \"2022-1-22 12:00:00\",\n" +
            "\t\t\"role\": [{\n" +
            "\t\t\t\"userId\": 1,\n" +
            "\t\t\t\"roleId\": 1,\n" +
            "\t\t\t\"roleName\": \"role1\",\n" +
            "\t\t\t\"menus\": [{\n" +
            "\t\t\t\t\"roleId\": 1,\n" +
            "\t\t\t\t\"menuName\": \"menu1\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu1\"\n" +
            "\t\t\t}, {\n" +
            "\t\t\t\t\"roleId\": 1,\n" +
            "\t\t\t\t\"menuName\": \"menu2\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu2\"\n" +
            "\t\t\t}, {\n" +
            "\t\t\t\t\"roleId\": 1,\n" +
            "\t\t\t\t\"menuName\": \"menu3\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu3\"\n" +
            "\t\t\t}]\n" +
            "\t\t}, {\n" +
            "\t\t\t\"userId\": 1,\n" +
            "\t\t\t\"roleId\": 2,\n" +
            "\t\t\t\"roleName\": \"role2\",\n" +
            "\t\t\t\"menus\": [{\n" +
            "\t\t\t\t\"roleId\": 2,\n" +
            "\t\t\t\t\"menuName\": \"menu21\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu21\"\n" +
            "\t\t\t}, {\n" +
            "\t\t\t\t\"roleId\": 2,\n" +
            "\t\t\t\t\"menuName\": \"menu22\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu22\"\n" +
            "\t\t\t}, {\n" +
            "\t\t\t\t\"roleId\": 2,\n" +
            "\t\t\t\t\"menuName\": \"menu23\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu23\"\n" +
            "\t\t\t}]\n" +
            "\t\t}, {\n" +
            "\t\t\t\"userId\": 1,\n" +
            "\t\t\t\"roleId\": 3,\n" +
            "\t\t\t\"roleName\": \"role3\"\n" +
            "\t\t}]\n" +
            "\t}, {\n" +
            "\t\t\"id\": 2,\n" +
            "\t\t\"name\": \"李四\",\n" +
            "\t\t\"age\": 17,\n" +
            "\t\t\"createTime\": \"2022-1-22 12:30:00\",\n" +
            "\t\t\"role\": [{\n" +
            "\t\t\t\"userId\": 2,\n" +
            "\t\t\t\"roleId\": 4,\n" +
            "\t\t\t\"roleName\": \"role4\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"userId\": 2,\n" +
            "\t\t\t\"roleId\": 5,\n" +
            "\t\t\t\"roleName\": \"role5\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"userId\": 2,\n" +
            "\t\t\t\"roleId\": 6,\n" +
            "\t\t\t\"roleName\": \"role6\"\n" +
            "\t\t}]\n" +
            "\t}]\n" +
            "}";


    public static void main(String[] args) {
        // 测试时间
        Instant inst1 = Instant.now();
        JSONObject json = JSON.parseObject(JSONSTR);

        // 封装数据库存储的数据结构
//        List<ApiTableDTO> apiTableDtoList = getApiTableDtoList01();
        List<ApiTableDTO02> apiTableDtoList = getApiTableDtoList02();

        List<String> tableNameList = apiTableDtoList.stream().map(tableDTO -> tableDTO.tableName).collect(Collectors.toList());
        // 获取目标表
        List<JsonTableData> targetTable = getTargetTable(tableNameList);
        // 获取Json的schema信息
//        List<JsonSchema> schemas = getJsonSchema(apiTableDtoList);
        List<JsonSchema> schemas = getJsonSchema02(apiTableDtoList);
        try {
            // json根节点处理
            rootNodeHandler(schemas, json, targetTable);
            targetTable.forEach(System.out::println);

            System.out.println("开始执行sql");
            PgsqlUtils pgsqlUtils = new PgsqlUtils();
            // ods_abbreviationName_tableName
            pgsqlUtils.executeBatchPgsql("", targetTable);

            Instant inst2 = Instant.now();
            System.out.println("Difference in 纳秒 : " + Duration.between(inst1, inst2).getNano());
            System.out.println("Difference in seconds : " + Duration.between(inst1, inst2).getSeconds());
        } catch (Exception e) {
            System.out.println("执行失败");
        }
    }

    /**
     * 手动设置数据库参数
     *
     * @return list
     */
    private static List<ApiTableDTO> getApiTableDtoList01() {

        List<ApiTableDTO> list = new ArrayList<>();

        ApiTableDTO apiTableDTO1 = new ApiTableDTO();
        apiTableDTO1.tableName = "tb_user";
        apiTableDTO1.pid = true;

        List<TableFieldsDTO> fieldsDTOS1 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO1 = new TableFieldsDTO();
        tableFieldsDTO1.fieldName = "id";
        tableFieldsDTO1.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO2 = new TableFieldsDTO();
        tableFieldsDTO2.fieldName = "age";
        tableFieldsDTO2.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO3 = new TableFieldsDTO();
        tableFieldsDTO3.fieldName = "name";
        tableFieldsDTO3.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO4 = new TableFieldsDTO();
        tableFieldsDTO4.fieldName = "createTime";
        tableFieldsDTO4.fieldType = "VARCHAR";
        fieldsDTOS1.add(tableFieldsDTO1);
        fieldsDTOS1.add(tableFieldsDTO2);
        fieldsDTOS1.add(tableFieldsDTO3);
        fieldsDTOS1.add(tableFieldsDTO4);
        apiTableDTO1.list = fieldsDTOS1;
        List<String> ch = new ArrayList<>();
        ch.add("tb_role");
        ch.add("tb_menu");
        apiTableDTO1.childTableName = ch;


        ApiTableDTO apiTableDTO2 = new ApiTableDTO();
        apiTableDTO2.tableName = "tb_role";
        apiTableDTO2.pid = false;

        List<TableFieldsDTO> fieldsDTOS2 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO5 = new TableFieldsDTO();
        tableFieldsDTO5.fieldName = "roleId";
        tableFieldsDTO5.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO6 = new TableFieldsDTO();
        tableFieldsDTO6.fieldName = "roleName";
        tableFieldsDTO6.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO7 = new TableFieldsDTO();
        tableFieldsDTO7.fieldName = "userId";
        tableFieldsDTO7.fieldType = "INT";
        fieldsDTOS2.add(tableFieldsDTO5);
        fieldsDTOS2.add(tableFieldsDTO6);
        fieldsDTOS2.add(tableFieldsDTO7);
        apiTableDTO2.list = fieldsDTOS2;
        List<String> ch2 = new ArrayList<>();
        ch2.add("tb_menu");
        apiTableDTO2.childTableName = ch2;

        ApiTableDTO apiTableDTO3 = new ApiTableDTO();
        apiTableDTO3.tableName = "tb_menu";
        apiTableDTO3.pid = false;
        List<TableFieldsDTO> fieldsDTOS3 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO8 = new TableFieldsDTO();
        tableFieldsDTO8.fieldName = "menuSrc";
        tableFieldsDTO8.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO9 = new TableFieldsDTO();
        tableFieldsDTO9.fieldName = "roleId";
        tableFieldsDTO9.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO10 = new TableFieldsDTO();
        tableFieldsDTO10.fieldName = "menuName";
        tableFieldsDTO10.fieldType = "VARCHAR";
        fieldsDTOS3.add(tableFieldsDTO8);
        fieldsDTOS3.add(tableFieldsDTO9);
        fieldsDTOS3.add(tableFieldsDTO10);
        apiTableDTO3.list = fieldsDTOS3;
        apiTableDTO3.childTableName = null;


        list.add(apiTableDTO1);
        list.add(apiTableDTO2);
        list.add(apiTableDTO3);
        return list;
    }

    private static List<ApiTableDTO02> getApiTableDtoList02() {

        List<ApiTableDTO02> list = new ArrayList<>();
        List<ApiTableDTO02> list2 = new ArrayList<>();
        List<ApiTableDTO02> list3 = new ArrayList<>();

        ApiTableDTO02 apiTableDTO1 = new ApiTableDTO02();
        apiTableDTO1.tableName = "tb_user";
        apiTableDTO1.pid = true;

        List<TableFieldsDTO> fieldsDTOS1 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO1 = new TableFieldsDTO();
        tableFieldsDTO1.fieldName = "id";
        tableFieldsDTO1.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO2 = new TableFieldsDTO();
        tableFieldsDTO2.fieldName = "age";
        tableFieldsDTO2.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO3 = new TableFieldsDTO();
        tableFieldsDTO3.fieldName = "name";
        tableFieldsDTO3.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO4 = new TableFieldsDTO();
        tableFieldsDTO4.fieldName = "createTime";
        tableFieldsDTO4.fieldType = "VARCHAR";
        fieldsDTOS1.add(tableFieldsDTO1);
        fieldsDTOS1.add(tableFieldsDTO2);
        fieldsDTOS1.add(tableFieldsDTO3);
        fieldsDTOS1.add(tableFieldsDTO4);
        apiTableDTO1.list = fieldsDTOS1;
        List<String> ch = new ArrayList<>();
        ch.add("tb_role");
        ch.add("tb_menu");
//        apiTableDTO1.childTableName = ch;


        ApiTableDTO02 apiTableDTO2 = new ApiTableDTO02();
        apiTableDTO2.tableName = "tb_role";
        apiTableDTO2.pid = false;

        List<TableFieldsDTO> fieldsDTOS2 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO5 = new TableFieldsDTO();
        tableFieldsDTO5.fieldName = "roleId";
        tableFieldsDTO5.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO6 = new TableFieldsDTO();
        tableFieldsDTO6.fieldName = "roleName";
        tableFieldsDTO6.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO7 = new TableFieldsDTO();
        tableFieldsDTO7.fieldName = "userId";
        tableFieldsDTO7.fieldType = "INT";
        fieldsDTOS2.add(tableFieldsDTO5);
        fieldsDTOS2.add(tableFieldsDTO6);
        fieldsDTOS2.add(tableFieldsDTO7);
        apiTableDTO2.list = fieldsDTOS2;
        List<String> ch2 = new ArrayList<>();
        ch2.add("tb_menu");
//        apiTableDTO2.childTableName = ch2;

        ApiTableDTO02 apiTableDTO3 = new ApiTableDTO02();
        apiTableDTO3.tableName = "tb_menu";
        apiTableDTO3.pid = false;
        List<TableFieldsDTO> fieldsDTOS3 = new ArrayList<>();
        TableFieldsDTO tableFieldsDTO8 = new TableFieldsDTO();
        tableFieldsDTO8.fieldName = "menuSrc";
        tableFieldsDTO8.fieldType = "VARCHAR";
        TableFieldsDTO tableFieldsDTO9 = new TableFieldsDTO();
        tableFieldsDTO9.fieldName = "roleId";
        tableFieldsDTO9.fieldType = "INT";
        TableFieldsDTO tableFieldsDTO10 = new TableFieldsDTO();
        tableFieldsDTO10.fieldName = "menuName";
        tableFieldsDTO10.fieldType = "VARCHAR";
        fieldsDTOS3.add(tableFieldsDTO8);
        fieldsDTOS3.add(tableFieldsDTO9);
        fieldsDTOS3.add(tableFieldsDTO10);
        apiTableDTO3.list = fieldsDTOS3;
//        apiTableDTO3.childTableName = null;

        list3.add(apiTableDTO3);
        list2.add(apiTableDTO2);
        apiTableDTO2.childTable = list3;
        apiTableDTO1.childTable = list2;
//        list.add(apiTableDTO2);
//        list.add(apiTableDTO3);
        list.add(apiTableDTO1);

        return list;
    }

    /**
     * json根节点处理
     *
     * @return void
     * @description json根节点处理
     * @author gy
     * @date 2022/1/20 15:49
     * @version v1.0
     * @params schemas json架构
     * @params data json数据
     * @params targetTable 最终处理结果
     */
    private static void rootNodeHandler(List<JsonSchema> schemas, JSONObject data, List<JsonTableData> targetTable) throws Exception {
        for (JsonSchema schema : schemas) {
            if (schema.type == JsonSchema.TypeEnum.ARRAY) {
                JSONArray arr = data.getJSONArray(schema.name);
                dataNodeHandler(schema.children, arr, targetTable);
            }
        }
    }

    /**
     * json数据节点处理（递归处理所有节点）
     *
     * @return void
     * @description json数据节点处理
     * @author gy
     * @date 2022/1/20 15:50
     * @version v1.0
     * @params schemas json架构
     * @params data json数据
     * @params targetTable 最终处理结果
     */
    private static void dataNodeHandler(List<JsonSchema> schemas, JSONArray data, List<JsonTableData> targetTable) throws Exception {
        if (data == null)
            return;
        for (JsonTableData item : targetTable) {
            List<JsonSchema> tableSchema = schemas.stream()
                    .filter(e -> StringUtils.hasLength(e.targetTableName) && e.targetTableName.equals(item.table))
                    .collect(Collectors.toList());
            if (tableSchema.size() > 0) {
                for (Object rowData : data) {
                    JSONObject jsonRowData = (JSONObject) rowData;
                    JSONObject model = new JSONObject();
                    for (JsonSchema columnSchema : schemas) {
                        switch (columnSchema.type) {
                            case INT:
                                model.put(columnSchema.name, jsonRowData.getInteger(columnSchema.name));
                                break;
                            case STRING:
                                model.put(columnSchema.name, jsonRowData.getString(columnSchema.name));
                                break;
                            case DATETIME:
                                model.put(columnSchema.name, jsonRowData.getDate(columnSchema.name));
                                break;
                            case ARRAY:
                                dataNodeHandler(columnSchema.children, jsonRowData.getJSONArray(columnSchema.name), targetTable);
                                break;
                            default:
                                throw new Exception("未知类型");
                        }
                    }
                    item.data.add(model);
                }
            }
        }
    }

    /**
     * 获取Json的schema信息
     *
     * @return java.util.List<com.fisk.test.model.JsonSchema>
     * @description 获取Json的schema信息
     * @author gy
     * @date 2022/1/20 14:16
     * @version v1.0
     * @params
     */
    private static List<JsonSchema> getJsonSchema(List<ApiTableDTO> apiTableDtoList) {
        List<JsonSchema> root = new ArrayList<>();

        List<JsonSchema> dataSchema = new ArrayList<>();
        for (int i = 0; i < apiTableDtoList.size(); i++) {
            ApiTableDTO tableDto = apiTableDtoList.get(i);

            // 判断是否父级
            if (tableDto.pid) {
                JsonSchema jsonSchema = JsonSchema.builder()
                        .name("data")
                        .type(JsonSchema.TypeEnum.ARRAY)
                        .children(dataSchema)
                        .build();
                root.add(jsonSchema);
            }

            for (TableFieldsDTO fieldsDto : tableDto.list) {

                dataSchema.add(JsonSchema.builder()
                        .name(fieldsDto.fieldName)
                        .type(converFieldType(fieldsDto.fieldType))
                        .targetTableName(tableDto.tableName)
                        .build());
            }

            if (!CollectionUtils.isEmpty(tableDto.childTableName)) {
                for (String child : tableDto.childTableName) {
                    List<JsonSchema> childJsonSchema = new ArrayList<>();
                    dataSchema.add(JsonSchema.builder()
                            .name(child)
                            .type(JsonSchema.TypeEnum.ARRAY)
                            .targetTableName(tableDto.tableName)
                            .children(childJsonSchema)
                            .build());
                }
            }

        }

//        for (ApiTableDTO tableDto : apiTableDtoList) {
//            List<JsonSchema> dataSchema = new ArrayList<>();
//
//            // 判断是否父级
//            if (tableDto.pid) {
//                JsonSchema jsonSchema = JsonSchema.builder()
//                        .name("data")
//                        .type(JsonSchema.TypeEnum.ARRAY)
//                        .children(dataSchema)
//                        .build();
//                root.add(jsonSchema);
//            }
//
//            for (TableFieldsDTO fieldsDto : tableDto.list) {
//
//                dataSchema.add(JsonSchema.builder()
//                        .name(fieldsDto.fieldName)
//                        .type(converFieldType(fieldsDto.fieldType))
//                        .targetTableName(tableDto.tableName)
//                        .build());
//            }
//
//            if (!CollectionUtils.isEmpty(tableDto.childTableName)) {
//                List<JsonSchema> childJsonSchema = new ArrayList<>();
//                for (String child : tableDto.childTableName) {
//
//                    dataSchema.add(JsonSchema.builder()
//                            .name(child)
//                            .type(JsonSchema.TypeEnum.ARRAY)
//                            .targetTableName(tableDto.tableName)
//                            .children(childJsonSchema)
//                            .build());
//                }
//                dataSchema = childJsonSchema;
//            }
//        }

        return root;
    }

    private static List<JsonSchema> getJsonSchema02(List<ApiTableDTO02> apiTableDtoList) {
        List<JsonSchema> root = new ArrayList<>();

        for (ApiTableDTO02 tableDto : apiTableDtoList) {
            List<JsonSchema> dataSchema = new ArrayList<>();

            // 判断是否父级
            if (tableDto.pid) {
                JsonSchema jsonSchema = JsonSchema.builder()
                        .name("data")
                        .type(JsonSchema.TypeEnum.ARRAY)
                        .children(dataSchema)
                        .build();
                root.add(jsonSchema);
            }

            for (TableFieldsDTO fieldsDto : tableDto.list) {

                dataSchema.add(JsonSchema.builder()
                        .name(fieldsDto.fieldName)
                        .type(converFieldType(fieldsDto.fieldType))
                        .targetTableName(tableDto.tableName)
                        .build());
            }

            for (ApiTableDTO02 dto02 : tableDto.childTable) {

                List<JsonSchema> ch = new ArrayList<>();

                dataSchema.add(JsonSchema.builder()
                        .name(dto02.tableName)
                        .type(JsonSchema.TypeEnum.ARRAY)
                        .targetTableName(tableDto.tableName)
                        .children(ch)
                        .build());

                for (TableFieldsDTO fieldsDto : dto02.list) {

                    ch.add(JsonSchema.builder()
                            .name(fieldsDto.fieldName)
                            .type(converFieldType(fieldsDto.fieldType))
                            .targetTableName(tableDto.tableName)
                            .build());
                }

            }

        }

        return root;
    }

    /**
     * 获取目标表
     *
     * @return java.util.List<com.fisk.dataaccess.dto.json.JsonTableData>
     * @description 获取目标表
     * @author Lock
     * @date 2022/1/21 11:38
     * @version v1.1
     * @params tableNameList 表名集合
     */
    public static List<JsonTableData> getTargetTable(List<String> tableNameList) {
        return tableNameList.stream()
                .map(tableName -> JsonTableData.builder().table(tableName).data(new JSONArray()).build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 转换字段类型
     *
     * @return com.fisk.dataaccess.dto.json.JsonSchema.TypeEnum
     * @description 转换字段类型
     * @author Lock
     * @date 2022/1/21 14:50
     * @version v1.0
     * @params fieldType 字段类型
     */
    public static JsonSchema.TypeEnum converFieldType(String fieldType) {
        FieldTypeEnum fieldTypeEnum = FieldTypeEnum.getValue(fieldType);
        switch (Objects.requireNonNull(fieldTypeEnum)) {
            case INT:
                return JsonSchema.TypeEnum.INT;
            case DATETIME:
                return JsonSchema.TypeEnum.DATETIME;
            case STRING1:
            case STRING2:
            default:
                return JsonSchema.TypeEnum.STRING;
        }
    }

}
