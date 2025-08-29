package org.modelseeed.vault.dto;

/**
 * DataTables request parameters for node pagination
 */
public class NodePageRequest {
    
    private int draw;
    private int start;
    private int length;
    private String searchValue;
    private String sortColumn;
    private String sortDirection;
    private String nodeType; // Optional filter by node type/label

    public NodePageRequest() {}

    public NodePageRequest(int draw, int start, int length) {
        this.draw = draw;
        this.start = start;
        this.length = length;
    }

    // Getters and setters
    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
}
