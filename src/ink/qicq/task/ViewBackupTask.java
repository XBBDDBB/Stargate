package ink.qicq.task;

import ink.qicq.utils.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewBackupTask extends BackupTask{

    public ViewBackupTask(Map<String, String> paramMap) {
        super(paramMap);
    }

    @Override
    public String getObjListByUser(String DBName, String user) {
        return SQLUtils.generateAllViewListSqlByTableSchema(DBName, user);
    }

    @Override
    public String getSqlByUserAndTableName(String DBName, String user, String tableName) {
        return SQLUtils.generateCreateViewSqlByTableSchemaAndName(DBName, user, tableName);
    }

    @Override
    public String getObjListColumnName() {
        if("GBase8AMPP".equals(super.dbUtils.getDBName()) || "GaussDB".equals(super.dbUtils.getDBName())){
            return "VIEW_FULL_NAME";
        }
        return "ERROR";
    }

    @Override
    public String getSqlColumnName() {
        if("GBase8AMPP".equals(super.dbUtils.getDBName())){
            return "CREATE VIEW";
        }else if("GaussDB".equals(super.dbUtils.getDBName())){
            return "PG_GET_VIEWDEF";
        }
        return "ERROR";
    }

    @Override
    public String getType() {
        return "View";
    }

    @Override
    public String specialTreatment(String tableName, String sql) {
        if("GBase8AMPP".equals(super.dbUtils.getDBName())){
            try {
                ResultSet newRs = super.dbUtils.getNewRs(SQLUtils.generateVCList(super.dbUtils.getDBName()));
                List<String> vcList = new ArrayList<String>();
                while(newRs.next()){
                    vcList.add(newRs.getString("VC"));
                }
                super.dbUtils.closeAllExceptConnection();
                for(String vc:vcList){
                    sql = sql.replace("\""+vc+"\".","");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return "DROP VIEW IF EXISTS "+tableName+";"+"\n"+sql+";";
        }else if("GaussDB".equals(super.dbUtils.getDBName())){
            return "DROP VIEW IF EXISTS "+tableName+";"+"\n"+"CREATE VIEW "+tableName+" AS "+sql;
        }
        return sql;
    }

}
