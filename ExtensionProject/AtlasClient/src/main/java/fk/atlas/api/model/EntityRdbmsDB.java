package fk.atlas.api.model;

public class EntityRdbmsDB {
    public static class entity_rdbms_db {
        public attributes_rdbms_db entity;
    }
    public static class attributes_rdbms_db {
        public attributes_field_rdbms_db attributes;
        public String typeName = "rdbms_db";
        public String status = "ACTIVE";
    }
    public static class attributes_field_rdbms_db {
        public String qualifiedName;
        public String name;
        public String description;
        public String displayText;
        public String owner;
        public String ownerName;
        public instance_rdbms_db instance;
    }
    public static class instance_rdbms_db{
        public String guid;
        public String typeName="rdbms_instance";
        public String entityStatus;
    }
}
