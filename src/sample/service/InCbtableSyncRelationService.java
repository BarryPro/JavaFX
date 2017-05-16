package sample.service;

import sample.dao.InCbtableSyncRelationDaoImpl;

import java.util.List;
import java.util.Map;

/**
 * @Description: <p>用于数据工单的同步表配置</p>
 * @Author: belong.
 * @Date: 2017/5/16.
 */
public class InCbtableSyncRelationService {
    private static InCbtableSyncRelationDaoImpl dao = new InCbtableSyncRelationDaoImpl();

    /**
     * 数据工单的同步表配置具体实现
     */
    public void orderSyncRelation(String tableName){
        dao.insert(tableName);
        Map<String, List<String>> map = dao.selectIndexFlag(tableName);
        List<String> list = map.get("indexFlag");
        System.out.println("list:"+list);
        if (!list.isEmpty()) {
            for (String columnName:list) {
                dao.updateIndexFlag(tableName,columnName);
            }
        }
    }
}
