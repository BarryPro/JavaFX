package sample.dao;

import sample.db.OracleConn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: <p></p>
 * @Author: belong.
 * @Date: 2017/5/16.
 */
public class InCbtableSyncRelationDaoImpl {
    private Connection connection;

    /**
     * 用于连接Oracle数据库
     */
    public InCbtableSyncRelationDaoImpl() {
        connection = OracleConn.getConnection();
    }

    /**
     * 用于插入同步配置表的数据
     *
     * @param tablename
     */
    public String insert(String tablename) {
        tablename = tablename.toUpperCase();
        String message = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 查询工单同步配置表
            String sqlTableExits = "SELECT * FROM ALL_TABLES WHERE table_name = UPPER('" + tablename + "')";
            String sqlRepeatTable = "SELECT * FROM DBACCADM.IN_CBTABLE_SYNC_RELATION_TMP WHERE dest_table = UPPER('" + tablename + "')";
            String sql = "INSERT INTO IN_CBTABLE_SYNC_RELATION_TMP " +
                    " SELECT '" + tablename + "' SOURCE_TABLE, " +
                    " COLUMN_NAME SOURCE_COLUMN, '" +
                    tablename + "' DEST_TABLE, " +
                    " COLUMN_NAME DEST_COLUMN, " +
                    " DECODE(DATA_TYPE, 'NUMBER', '1', 'DATE', '2', '0') DATA_TYPE, " +
                    " 'N' INDEX_FLAG, " +
                    " '1' SYNC_FLAG, " +
                    " '0' DEAL_FLAG, " +
                    " '0' DEAL_VALUE, " +
                    " '0' TRIGGER_FLAG, " +
                    " ''  CHG_TABLE, " +
                    " '111' OPER_FLAG  " +
                    " FROM ALL_TAB_COLS " +
                    " WHERE TABLE_NAME = UPPER('" + tablename + "')";
            ps = connection.prepareStatement(sqlTableExits);
            rs = ps.executeQuery();
            if (rs.next()) {
                ps = connection.prepareStatement(sqlRepeatTable);
                rs = ps.executeQuery();
                if (rs.next()) {
                    message = tablename + "表的同步配置已经配完了,请检查,不要重复配置";
                } else {
                    ps = connection.prepareStatement(sql);
                    if (ps.execute()) {
                        message = tablename + "同步插入成功";
                    }
                }
            } else {
                message = tablename + "表不存在，请确认建立";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return message;
    }

    /**
     * 用于查出同步配置表的唯一标识列表，然后进行更新
     *
     * @param tableName
     * @return
     */
    public Map selectIndexFlag(String tableName) {
        // 用于信息传递
        Map<String, Object> map = new HashMap<>();
        // 用于存唯一标识字段
        List<String> list = new ArrayList<>();
        // 用于存放返回的提示信息
        String message = "";
        tableName = tableName.toUpperCase();
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean flag = false;
        try {
            // 查询工单同步配置表
            String sqlUnique = "SELECT * FROM All_Ind_Columns WHERE table_name = UPPER('" + tableName + "') AND index_name in (SELECT index_name FROM  all_indexes WHERE table_name = UPPER('" + tableName + "') AND uniqueness = UPPER('unique'))";
            String sqlPK = "SELECT * FROM ALL_CONS_COLUMNS WHERE table_name = UPPER('" + tableName + "') AND constraint_name LIKE UPPER('%pk%')";
            // 查询主键标识
            ps = connection.prepareStatement(sqlPK);
            rs = ps.executeQuery();
            if (rs.next()) {
                list.add(rs.getString("COLUMN_NAME"));
                while (rs.next()) {
                    list.add(rs.getString("COLUMN_NAME"));
                }
                message = "唯一标识是主键："+list;
            } else { // 没有主键，看是否有唯一索引
                ps = connection.prepareStatement(sqlUnique);
                rs = ps.executeQuery();
                if (rs.next()) {
                    list.add(rs.getString("COLUMN_NAME"));
                    while (rs.next()) {
                        list.add(rs.getString("COLUMN_NAME"));
                    }
                    message = "唯一标识是唯一索引："+list;
                } else {
                    message = tableName + "表没有唯一标识,请确认建立";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        map.put("indexFlag", list);
        map.put("message",message);
        return map;
    }

    /**
     * 用于更新对应表的索引标识，每张表可能有多个索引标识
     *
     * @param tableName
     * @param columnName
     */
    public String updateIndexFlag(String tableName, String columnName) {
        tableName = tableName.toUpperCase();
        String message= "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 查询工单同步配置表
            String sqlUpdate = "UPDATE IN_CBTABLE_SYNC_RELATION_TMP SET INDEX_FLAG = 'Y' WHERE DEST_TABLE = UPPER('"+tableName+"') AND DEST_COLUMN = UPPER('"+columnName+"')";
            ps = connection.prepareStatement(sqlUpdate);
            // 看是否更新成功
            if (ps.executeUpdate() > 0) {
                message = tableName + "表的同步配置更新索引标识成功";
            } else {
                message = tableName + "表的同步配置更新索引标识失败";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return message;
    }
}
