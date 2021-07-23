package fk.atlas.api.model;

import java.util.List;

public class EntityProcess {
    enum inpOutpType
    {
        rdbms_instance, rdbms_db, rdbms_table,rdbms_column;
    }
    enum entityType
    {
        instance, db, table;
    }
    public static class entity_rdbms_process {
        public List<attributes_rdbms_process> entities;
    }
    public static class attributes_rdbms_process {
        public attributes_field_rdbms_process attributes;
        public String typeName = "Process";
    }

    public static class attributes_field_rdbms_process {
        public String qualifiedName;
        public String name;
        public String description;
        public String owner;
        public String ownerName;
        public String comment;
        public String contact_info;
        public String type;
        public String createTime;
        public String updateTime;
        public List<entity> inputs;
        public List<entity> outputs;
    }
    public static class entity{
        public String guid;
        public String typeName;
    }
}
