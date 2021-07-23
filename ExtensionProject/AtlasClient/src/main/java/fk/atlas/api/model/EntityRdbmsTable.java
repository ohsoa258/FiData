package fk.atlas.api.model;

public class EntityRdbmsTable {
    public static class entity_rdbms_table {
        public attributes_rdbms_table entity;
    }
    public static class attributes_rdbms_table {
        public attributes_field_rdbms_table attributes;
        public String typeName = "rdbms_table";
        public String status = "ACTIVE";
    }
    public static class attributes_field_rdbms_table {
        public String qualifiedName;
        public String name;
        public String description;
        public String owner;
        public String ownerName;
        public instance_rdbms_table db;
    }
    public static class instance_rdbms_table{
        public String guid;
        public String typeName="rdbms_db";
    }
}
