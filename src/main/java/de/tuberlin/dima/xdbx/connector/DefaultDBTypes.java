package de.tuberlin.dima.xdbx.connector;

public enum DefaultDBTypes implements DBType{
    POSTGRES {
        @Override
        public String getName() {
            return "postgresql";
        }

        @Override
        public String formatURI(String host, int port, String db) {
            return "jdbc:postgresql://" + host + ":" + port + "/" + db;
        }
    },
    MARIADB {
        @Override
        public String getName() {
            return "mariadb";
        }

        @Override
        public String formatURI(String host, int port, String db) {
            return "jdbc:mariadb://" + host + ":" + port + "/" + db;
        }
    },
    EXASOL {
        @Override
        public String getName() {
            return "exasol";
        }

        @Override
        public String formatURI(String host, int port, String db) {
            return "jdbc:exa:" + host + ":" + port + ";schema=SYS";
        }
    },
    HIVE {
        @Override
        public String getName() {
            return "hive";
        }

        @Override
        public String formatURI(String host, int port, String db) {
            return "jdbc:hive://" + host + ":" + port + "/" + db;
        }
    };

    public static DBType fromName(String name){
        for (DBType type : DefaultDBTypes.values()){
            if (type.getName().equals(name)){
                return type;
            }
        }
        return null;
    }


}
