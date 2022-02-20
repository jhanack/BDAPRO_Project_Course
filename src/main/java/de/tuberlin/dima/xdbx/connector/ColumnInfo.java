package de.tuberlin.dima.xdbx.connector;

import java.util.Objects;

public final class ColumnInfo {
    private final String name;
    private final String type;
    private final int columnWidth;

    public ColumnInfo(String name, String type, int columnWidth) {
        this.name = name;
        this.type = type;
        this.columnWidth = columnWidth;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnInfo that = (ColumnInfo) o;
        return columnWidth == that.columnWidth && Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, columnWidth);
    }
}
