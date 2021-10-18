package org.evomaster.client.java.instrumentation.coverage.methodreplacement.classes;

import org.evomaster.client.java.instrumentation.SqlInfo;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.MethodReplacementClass;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.Replacement;
import org.evomaster.client.java.instrumentation.shared.ReplacementType;
import org.evomaster.client.java.instrumentation.staticstate.ExecutionTracer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;


public class StatementClassReplacement implements MethodReplacementClass {

    @Override
    public Class<?> getTargetClass() {
        return Statement.class;
    }

    private static void handleSql(String sql, boolean exception, long executionTime){
        /*
            TODO need to provide proper info data here.
            Bit tricky, need to check actual DB implementations, see:
            https://stackoverflow.com/questions/867194/java-resultset-how-to-check-if-there-are-any-results/15750832#15750832

            Anyway, not needed till we support constraint solving for DB data, as then
            we can skip the branch distance computation

            Man: skip null sql for e.g., "com.zaxxer.hikari.pool"
         */
        if(sql != null){
            SqlInfo info = new SqlInfo(sql, false, exception, executionTime);
            ExecutionTracer.addSqlInfo(info);
        }

    }

    @Replacement(type = ReplacementType.TRACKER)
    public static ResultSet executeQuery(Statement caller, String sql) throws SQLException{
        return executeSql(()->caller.executeQuery(sql), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static int executeUpdate(Statement caller, String sql) throws SQLException{
        return executeSql(()->caller.executeUpdate(sql), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static boolean execute(Statement caller,String sql) throws SQLException{
        return executeSql(()->caller.execute(sql), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static int executeUpdate(Statement caller, String sql, int autoGeneratedKeys) throws SQLException{
        return executeSql(()->caller.executeUpdate(sql, autoGeneratedKeys), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static int executeUpdate(Statement caller, String sql, int columnIndexes[]) throws SQLException{
        return executeSql(()->caller.executeUpdate(sql, columnIndexes), sql);
    }


    @Replacement(type = ReplacementType.TRACKER)
    public static int executeUpdate(Statement caller, String sql, String columnNames[]) throws SQLException{
        return executeSql(()->caller.executeUpdate(sql, columnNames), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static boolean execute(Statement caller, String sql, int autoGeneratedKeys) throws SQLException{
        return executeSql(()->caller.execute(sql, autoGeneratedKeys), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static boolean execute(Statement caller, String sql, int columnIndexes[]) throws SQLException{
        return executeSql(()->caller.execute(sql, columnIndexes), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static boolean execute(Statement caller, String sql, String columnNames[]) throws SQLException{
        return executeSql(()->caller.execute(sql, columnNames), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static long executeLargeUpdate(Statement caller, String sql) throws SQLException {
        return executeSql(()->caller.executeLargeUpdate(sql), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static long executeLargeUpdate(Statement caller, String sql, int autoGeneratedKeys) throws SQLException {
        return executeSql(()->caller.executeLargeUpdate(sql, autoGeneratedKeys), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static long executeLargeUpdate(Statement caller, String sql, int columnIndexes[]) throws SQLException {
        return executeSql(()-> caller.executeLargeUpdate(sql, columnIndexes), sql);
    }

    @Replacement(type = ReplacementType.TRACKER)
    public static long executeLargeUpdate(Statement caller, String sql, String columnNames[]) throws SQLException {
        return executeSql(()-> caller.executeLargeUpdate(sql, columnNames), sql);
    }

    public static <T> T executeSql(SqlExecutionSupplier<T, SQLException> executeStatement, String sql) throws SQLException{

        long start = System.currentTimeMillis();
        try{
            T result = executeStatement.get();
            long end = System.currentTimeMillis();
            handleSql(sql, false, end -start);
            return result;
        }catch (SQLException e){
            // trace sql anyway, set exception true and executionTime FAILURE_EXTIME
            handleSql(sql, true, SqlInfo.FAILURE_EXTIME);
            throw e;
        }
    }

    /**
     * extend supplier for sql execution with sql exception
     * @param <T> outputs
     * @param <E> type of exceptions
     */
    @FunctionalInterface
    public interface SqlExecutionSupplier<T, E extends Exception> {
        T get() throws E;
    }

}
