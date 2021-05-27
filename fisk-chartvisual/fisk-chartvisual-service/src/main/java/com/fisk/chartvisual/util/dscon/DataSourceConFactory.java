package com.fisk.chartvisual.util.dscon;

public class DataSourceConFactory {

    public void Connection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");

            Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
