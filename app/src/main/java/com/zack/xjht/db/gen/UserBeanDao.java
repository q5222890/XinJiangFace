package com.zack.xjht.db.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.zack.xjht.entity.UserBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "USER_BEAN".
*/
public class UserBeanDao extends AbstractDao<UserBean, Long> {

    public static final String TABLENAME = "USER_BEAN";

    /**
     * Properties of entity UserBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property UId = new Property(0, Long.class, "uId", true, "_id");
        public final static Property DeptName = new Property(1, String.class, "deptName", false, "DEPT_NAME");
        public final static Property PhoneNumber = new Property(2, String.class, "phoneNumber", false, "PHONE_NUMBER");
        public final static Property Sex = new Property(3, String.class, "sex", false, "SEX");
        public final static Property CardId = new Property(4, String.class, "cardId", false, "CARD_ID");
        public final static Property PoliceNo = new Property(5, String.class, "policeNo", false, "POLICE_NO");
        public final static Property UserName = new Property(6, String.class, "userName", false, "USER_NAME");
        public final static Property UserId = new Property(7, int.class, "userId", false, "USER_ID");
        public final static Property Email = new Property(8, String.class, "email", false, "EMAIL");
        public final static Property RoleKeys = new Property(9, String.class, "roleKeys", false, "ROLE_KEYS");
    }


    public UserBeanDao(DaoConfig config) {
        super(config);
    }
    
    public UserBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: uId
                "\"DEPT_NAME\" TEXT," + // 1: deptName
                "\"PHONE_NUMBER\" TEXT," + // 2: phoneNumber
                "\"SEX\" TEXT," + // 3: sex
                "\"CARD_ID\" TEXT," + // 4: cardId
                "\"POLICE_NO\" TEXT," + // 5: policeNo
                "\"USER_NAME\" TEXT," + // 6: userName
                "\"USER_ID\" INTEGER NOT NULL ," + // 7: userId
                "\"EMAIL\" TEXT," + // 8: email
                "\"ROLE_KEYS\" TEXT);"); // 9: roleKeys
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, UserBean entity) {
        stmt.clearBindings();
 
        Long uId = entity.getUId();
        if (uId != null) {
            stmt.bindLong(1, uId);
        }
 
        String deptName = entity.getDeptName();
        if (deptName != null) {
            stmt.bindString(2, deptName);
        }
 
        String phoneNumber = entity.getPhoneNumber();
        if (phoneNumber != null) {
            stmt.bindString(3, phoneNumber);
        }
 
        String sex = entity.getSex();
        if (sex != null) {
            stmt.bindString(4, sex);
        }
 
        String cardId = entity.getCardId();
        if (cardId != null) {
            stmt.bindString(5, cardId);
        }
 
        String policeNo = entity.getPoliceNo();
        if (policeNo != null) {
            stmt.bindString(6, policeNo);
        }
 
        String userName = entity.getUserName();
        if (userName != null) {
            stmt.bindString(7, userName);
        }
        stmt.bindLong(8, entity.getUserId());
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(9, email);
        }
 
        String roleKeys = entity.getRoleKeys();
        if (roleKeys != null) {
            stmt.bindString(10, roleKeys);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, UserBean entity) {
        stmt.clearBindings();
 
        Long uId = entity.getUId();
        if (uId != null) {
            stmt.bindLong(1, uId);
        }
 
        String deptName = entity.getDeptName();
        if (deptName != null) {
            stmt.bindString(2, deptName);
        }
 
        String phoneNumber = entity.getPhoneNumber();
        if (phoneNumber != null) {
            stmt.bindString(3, phoneNumber);
        }
 
        String sex = entity.getSex();
        if (sex != null) {
            stmt.bindString(4, sex);
        }
 
        String cardId = entity.getCardId();
        if (cardId != null) {
            stmt.bindString(5, cardId);
        }
 
        String policeNo = entity.getPoliceNo();
        if (policeNo != null) {
            stmt.bindString(6, policeNo);
        }
 
        String userName = entity.getUserName();
        if (userName != null) {
            stmt.bindString(7, userName);
        }
        stmt.bindLong(8, entity.getUserId());
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(9, email);
        }
 
        String roleKeys = entity.getRoleKeys();
        if (roleKeys != null) {
            stmt.bindString(10, roleKeys);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public UserBean readEntity(Cursor cursor, int offset) {
        UserBean entity = new UserBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // uId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // deptName
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // phoneNumber
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // sex
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // cardId
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // policeNo
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // userName
            cursor.getInt(offset + 7), // userId
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // email
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9) // roleKeys
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, UserBean entity, int offset) {
        entity.setUId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDeptName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setPhoneNumber(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setSex(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setCardId(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setPoliceNo(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setUserName(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setUserId(cursor.getInt(offset + 7));
        entity.setEmail(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setRoleKeys(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(UserBean entity, long rowId) {
        entity.setUId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(UserBean entity) {
        if(entity != null) {
            return entity.getUId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(UserBean entity) {
        return entity.getUId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
