package socialnetwork.utils;

import java.time.format.DateTimeFormatter;

public class Constants {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_BASIC = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final String SQL_ACTION_UPDATE = "update";
    public static final String SQL_ACTION_QUERY = "query";

}
