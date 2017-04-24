package com.yijia.elasticsearch;

public enum TypeJDBCES {

    /** boolean类型 */
    BIT("bit", "boolean"),
    /** 数字类型 */
    NUMERIC("decimal", "double"),
    DOUBLE("double", "double"), 
    FLOAT("float", "float"), 
    REAL("real", "float"), 
    BIGINT("bigint", "long"), 
    INT("int", "integer"), 
    INTEGER("integer", "integer"), 
    SMALLINT("smallint", "short"), 
    /** byte类型 */
    TINYINT("tinyint", "byte"), 
    DATETIME("datetime", "date", "ik"), 
    TIMESTAMP("timestamp", "date", "ik"), 
    TIME("time", "date", "ik"), 
    DATE("date", "date", "ik"), 
    VARCHAR("varchar", "string"),
    CHAR("char", "string"),
    NVARCHAR("nvarchar", "string"),
    NCHAR("nchar", "string"),
    LONGVARCHAR("longvarchar", "string");

    /** jdbc的类型 */
    private String jdbc;
    /** es的类型 */
    private String es;
    /** 分词方式 */
    private String analyzer;

    private TypeJDBCES(String jdbc, String es) {
        this.jdbc = jdbc;
        this.es = es;
    }
    
    private TypeJDBCES(String jdbc, String es, String analyzer) {
        this.jdbc = jdbc;
        this.es = es;
        this.analyzer = analyzer;
    }

    /** jdbc的类型 */
    public String getJdbc() {
        return jdbc;
    }

    /** jdbc的类型 */
    public void setJdbc(String jdbc) {
        this.jdbc = jdbc;
    }

    /** es的类型 */
    public String getEs() {
        return es;
    }

    /** es的类型 */
    public void setEs(String es) {
        this.es = es;
    }

    /** 分词方式 */
    public String getAnalyzer() {
        return analyzer;
    }

    /** 分词方式 */
    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

}
