package task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AvroSchemaGenerator {

    public static void main(String[] args) throws IOException {
        String jsonString = "{\"lsj\":[\"String\"],\"product\":[\"String\"],\"hahaha\":[\"String\"],\"lisss\":[\"List\",\"Double\"],\"price\":[\"Double\"],\"lsjk\":[\"Document\"],\"_id\":[\"ObjectId\",\"String\"],\"type\":[\"String\"],\"lsjk.age\":[\"String\"],\"username\":[\"String\"]}";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonString);

        Schema ordersSchema = generateSchema(rootNode);

        // 打印生成的 Avro 模式
        System.out.println(ordersSchema.toString(true));
    }

    /**
     * 将mongodb json格式的元数据结构转为nifi组件可用的avro结构
     *
     * @param jsonString
     * @return
     * @throws IOException
     */
    public static String getAvroSchema(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonString);

        Schema ordersSchema = generateSchema(rootNode);
        return ordersSchema.toString(true);
    }

    private static Schema generateSchema(JsonNode rootNode) {
        SchemaBuilder.FieldAssembler<Schema> fieldAssembler = SchemaBuilder.record("orders").fields();
        Map<String, SchemaBuilder.FieldAssembler<Schema>> nestedFieldsMap = new HashMap<>();
        Map<String, Boolean> addedFields = new HashMap<>();

        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode fieldTypeNode = entry.getValue();

            if (!fieldName.contains(".")) {
                String fieldType = fieldTypeNode.get(0).asText();
                if (!addedFields.containsKey(fieldName)) {
                    Schema fieldSchema = getSchema(fieldType, fieldTypeNode, nestedFieldsMap, fieldName);
                    if (!"Document".equals(fieldType)){
                        fieldAssembler.name(fieldName).type(fieldSchema).noDefault();
                        addedFields.put(fieldName, true);
                    }
                }
            } else {
                String[] parts = fieldName.split("\\.");
                String parentFieldName = parts[0];
                String childFieldName = parts[1];

                JsonNode parentFieldTypeNode = rootNode.path(parentFieldName);
                String parentFieldType = parentFieldTypeNode.get(0).asText();

                if ("Document".equals(parentFieldType)) {
                    if (!nestedFieldsMap.containsKey(parentFieldName)) {
                        nestedFieldsMap.put(parentFieldName, SchemaBuilder.record(capitalize(parentFieldName)).fields());
                    }
                    SchemaBuilder.FieldAssembler<Schema> nestedFieldAssembler = nestedFieldsMap.get(parentFieldName);
                    Schema childFieldSchema = getSchema(rootNode.path(fieldName).get(0).asText(), rootNode.path(fieldName), nestedFieldsMap, childFieldName);
                    nestedFieldAssembler.name(childFieldName).type(childFieldSchema).noDefault();
                }
            }
        }

        // Add nested schemas to the main schema
        for (Map.Entry<String, SchemaBuilder.FieldAssembler<Schema>> entry : nestedFieldsMap.entrySet()) {
            String parentFieldName = entry.getKey();
            Schema nestedSchema = entry.getValue().endRecord();
            if (!addedFields.containsKey(parentFieldName)) {
                fieldAssembler.name(parentFieldName).type(nestedSchema).noDefault();
                addedFields.put(parentFieldName, true);
            }
        }

        return fieldAssembler.endRecord();
    }

    private static Schema getSchema(String fieldType, JsonNode fieldTypeNode, Map<String, SchemaBuilder.FieldAssembler<Schema>> nestedFieldsMap, String fieldName) {
        switch (fieldType) {
            case "String":
            case "ObjectId":
                return Schema.create(Schema.Type.STRING);
            case "Double":
                return Schema.create(Schema.Type.DOUBLE);
            case "List":
                String itemTypeName = fieldTypeNode.get(1).asText();
                Schema itemTypeSchema = getSchema(itemTypeName, fieldTypeNode, nestedFieldsMap, fieldName);
                return Schema.createArray(itemTypeSchema);
            case "Document":
                // Handle Document type by creating a nested record
                String documentName = capitalize(fieldName);
                if (nestedFieldsMap.containsKey(documentName)) {
                    return nestedFieldsMap.get(documentName).endRecord();
                } else {
                    return SchemaBuilder.record(documentName).fields().endRecord();
                }
            default:
                throw new IllegalArgumentException("Unsupported field type: " + fieldType);
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
