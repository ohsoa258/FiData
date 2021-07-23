package fk.atlas.api.model;

public class EntityRdbmsColumn {
    public static class entity_rdbms_column {
        public attributes_rdbms_column entity;
    }
    public static class attributes_rdbms_column {
        public attributes_field_rdbms_column attributes;
        public String typeName = "rdbms_column";
        public String status = "ACTIVE";
    }
    public static class attributes_field_rdbms_column {
        public String qualifiedName;
        public String name;
        public String data_type;
        public String owner;
        public String ownerName;
        public String comment;
        public String description;
        public instance_rdbms_table_column table;
    }
    public static class instance_rdbms_table_column{
        public String guid;
        public String typeName="rdbms_table";
    }
}
