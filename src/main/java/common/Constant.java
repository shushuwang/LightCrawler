package common;



public class Constant {
	/* Executor */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 10;
    public static final String DEFAULT_URL_ENCODING = "UTF-8";
    public static final int MSG_CODE_BUILD_AND_WRITE = 200;//build and write(write to file default)
    public static final int MSG_CODE_BUILD_AND_WRITE_TO_DB = 2001;//build and write to db
    public static final int MSG_CODE_BUILD = 201;//build next crawler
    public static final int MSG_CODE_WRITE = 202;//write (write to file default)
    public static final int MSG_CODE_WRITE_TO_DB = 2021;//write (write to db)
    public static final int MSG_CODE_EXCEPTION_IPBLOCK =500;
    public static final int MSG_CODE_EXCEPTION_CRAWLER =501;
    public static final String COLUMS_TYPE_INT ="INT";
    public static final String COLUMS_TYPE_DOUBLE ="DOUBLE";
    public static final String COLUMS_TYPE_LONG ="LONG";
    public static final String COLUMS_TYPE_BOOLEAN ="BOOLEAN";
    public static final String COLUMS_TYPE_STRING ="STRING";
    public static final String COLUMS_TYPE_SKIP = "SKIP";//skip the colum and the value will not be written to db

    public static final String PERSIST_TYPE_UPDATE = "UPDATE";
    public static final String PERSIST_TYPE_INSERT = "INSERT";	//Default
    
    public static final String EXTRA_TASK_TYPE = "taskType";
    public static final String PROXY_SERVER_URL_PREFIX = "http://106.14.197.46:8103";
    public static final String PROXY_EXCEPTION_SPLITTER = "!==!";

	//phantomJS
//    public static final String PHANTOMJS_EXEC_PATH = "/var/driver";
    public static final String PHANTOMJS_EXEC_PATH = "C:/phantomjs/bin";
    public static final String PHANTOMJS_JS_SCRIPT_PATH = "/var/crawl";
}