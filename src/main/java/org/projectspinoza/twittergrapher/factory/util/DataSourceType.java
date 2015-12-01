package org.projectspinoza.twittergrapher.factory.util;

public enum DataSourceType {
    ELASTICSEARCH("elasticsearch"), MONGODB("mongodb"), MYSQL("mysql"), FILE("file");

    private final String dataSourceType;

    DataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public static boolean contains(String type) {
        for (DataSourceType dst : DataSourceType.values()) {
            if (dst.getDataSourceType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return dataSourceType;
    }
}
