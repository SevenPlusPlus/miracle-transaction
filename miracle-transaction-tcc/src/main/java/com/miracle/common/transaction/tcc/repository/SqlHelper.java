package com.miracle.common.transaction.tcc.repository;


public class SqlHelper {

    public static String buildCreateTableSql(String dbType, String tableName) {
        String createTableSql;
        if(dbType.equalsIgnoreCase("mysql"))
        {
            createTableSql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n" +
                    "  `transaction_id` bigint NOT NULL,\n" +
                    "  `status` int(2) NOT NULL,\n" +
                    "  `role` int(2) NOT NULL,\n" +
                    "  `mode` int(2),\n" +
                    "  `target_class` varchar(256) ,\n" +
                    "  `target_method` varchar(128) ,\n" +
                    "  `confirm_method` varchar(128) ,\n" +
                    "  `cancel_method` varchar(128) ,\n" +
                    "  `retried_count` int(3) NOT NULL,\n" +
                    "  `create_time` datetime NOT NULL,\n" +
                    "  `last_update_time` datetime NOT NULL,\n" +
                    "  `version` int(6) NOT NULL,\n" +
                    "  `invocation` longblob,\n" +
                    "  PRIMARY KEY (`transaction_id`)\n" +
                    ")";
        }
        else if(dbType.equalsIgnoreCase("postgresql"))
        {
        	 createTableSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (\n" +
                     "  transaction_id bigint NOT NULL,\n" +
                     "  status integer NOT NULL,\n" +
                     "  role integer NOT NULL,\n" +
                     "  mode integer,\n" +
                     "  target_class text ,\n" +
                     "  target_method text ,\n" +
                     "  confirm_method text ,\n" +
                     "  cancel_method text ,\n" +
                     "  retried_count integer NOT NULL,\n" +
                     "  create_time timestamp without time zone NOT NULL,\n" +
                     "  last_update_time timestamp without time zone NOT NULL,\n" +
                     "  version integer NOT NULL,\n" +
                     "  invocation bytea,\n" +
                     "  PRIMARY KEY (transaction_id)\n" +
                     ")";
        }
        else
        {
            throw new RuntimeException("dbType类型不支持,目前仅支持mysql postgresql.");
        }
        return createTableSql;
    }

}
