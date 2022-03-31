package com.fisk.chartvisual.contentsplit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/17 16:27
 */
public class Lift {
    private int id;
    @JsonProperty("tableName")
    private String tablename;
    @JsonProperty("myScreen")
    private List<String> myscreen;
    @JsonProperty("columnName")
    private List<Columnname> columnname;
    @JsonProperty("tableKey")
    private int tablekey;
    private String temp;
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }
    public String getTablename() {
        return tablename;
    }

    public void setMyscreen(List<String> myscreen) {
        this.myscreen = myscreen;
    }
    public List<String> getMyscreen() {
        return myscreen;
    }

    public void setColumnname(List<Columnname> columnname) {
        this.columnname = columnname;
    }
    public List<Columnname> getColumnname() {
        return columnname;
    }

    public void setTablekey(int tablekey) {
        this.tablekey = tablekey;
    }
    public int getTablekey() {
        return tablekey;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }
    public String getTemp() {
        return temp;
    }
}
