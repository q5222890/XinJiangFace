package com.zack.xjht.db.gen;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import com.zack.xjht.entity.SubCabBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SUB_CAB_BEAN".
*/
public class SubCabBeanDao extends AbstractDao<SubCabBean, Long> {

    public static final String TABLENAME = "SUB_CAB_BEAN";

    /**
     * Properties of entity SubCabBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property SId = new Property(0, Long.class, "sId", true, "_id");
        public final static Property LocationNo = new Property(1, int.class, "locationNo", false, "LOCATION_NO");
        public final static Property IsUse = new Property(2, String.class, "isUse", false, "IS_USE");
        public final static Property GunNo = new Property(3, String.class, "gunNo", false, "GUN_NO");
        public final static Property ObjectNumber = new Property(4, int.class, "objectNumber", false, "OBJECT_NUMBER");
        public final static Property LocationType = new Property(5, String.class, "locationType", false, "LOCATION_TYPE");
        public final static Property ObjectName = new Property(6, String.class, "objectName", false, "OBJECT_NAME");
        public final static Property Id = new Property(7, String.class, "id", false, "ID");
        public final static Property ObjectId = new Property(8, String.class, "objectId", false, "OBJECT_ID");
        public final static Property GunState = new Property(9, String.class, "gunState", false, "GUN_STATE");
        public final static Property IsTemporary = new Property(10, String.class, "isTemporary", false, "IS_TEMPORARY");
        public final static Property CabId = new Property(11, Long.class, "cabId", false, "CAB_ID");
        public final static Property GetNum = new Property(12, int.class, "getNum", false, "GET_NUM");
        public final static Property UserId = new Property(13, String.class, "userId", false, "USER_ID");
        public final static Property UserName = new Property(14, String.class, "userName", false, "USER_NAME");
    }

    private Query<SubCabBean> cabInfoBean_ListLocationQuery;

    public SubCabBeanDao(DaoConfig config) {
        super(config);
    }
    
    public SubCabBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SUB_CAB_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: sId
                "\"LOCATION_NO\" INTEGER NOT NULL ," + // 1: locationNo
                "\"IS_USE\" TEXT," + // 2: isUse
                "\"GUN_NO\" TEXT," + // 3: gunNo
                "\"OBJECT_NUMBER\" INTEGER NOT NULL ," + // 4: objectNumber
                "\"LOCATION_TYPE\" TEXT," + // 5: locationType
                "\"OBJECT_NAME\" TEXT," + // 6: objectName
                "\"ID\" TEXT," + // 7: id
                "\"OBJECT_ID\" TEXT," + // 8: objectId
                "\"GUN_STATE\" TEXT," + // 9: gunState
                "\"IS_TEMPORARY\" TEXT," + // 10: isTemporary
                "\"CAB_ID\" INTEGER," + // 11: cabId
                "\"GET_NUM\" INTEGER NOT NULL ," + // 12: getNum
                "\"USER_ID\" TEXT," + // 13: userId
                "\"USER_NAME\" TEXT);"); // 14: userName
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SUB_CAB_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, SubCabBean entity) {
        stmt.clearBindings();
 
        Long sId = entity.getSId();
        if (sId != null) {
            stmt.bindLong(1, sId);
        }
        stmt.bindLong(2, entity.getLocationNo());
 
        String isUse = entity.getIsUse();
        if (isUse != null) {
            stmt.bindString(3, isUse);
        }
 
        String gunNo = entity.getGunNo();
        if (gunNo != null) {
            stmt.bindString(4, gunNo);
        }
        stmt.bindLong(5, entity.getObjectNumber());
 
        String locationType = entity.getLocationType();
        if (locationType != null) {
            stmt.bindString(6, locationType);
        }
 
        String objectName = entity.getObjectName();
        if (objectName != null) {
            stmt.bindString(7, objectName);
        }
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(8, id);
        }
 
        String objectId = entity.getObjectId();
        if (objectId != null) {
            stmt.bindString(9, objectId);
        }
 
        String gunState = entity.getGunState();
        if (gunState != null) {
            stmt.bindString(10, gunState);
        }
 
        String isTemporary = entity.getIsTemporary();
        if (isTemporary != null) {
            stmt.bindString(11, isTemporary);
        }
 
        Long cabId = entity.getCabId();
        if (cabId != null) {
            stmt.bindLong(12, cabId);
        }
        stmt.bindLong(13, entity.getGetNum());
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(14, userId);
        }
 
        String userName = entity.getUserName();
        if (userName != null) {
            stmt.bindString(15, userName);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, SubCabBean entity) {
        stmt.clearBindings();
 
        Long sId = entity.getSId();
        if (sId != null) {
            stmt.bindLong(1, sId);
        }
        stmt.bindLong(2, entity.getLocationNo());
 
        String isUse = entity.getIsUse();
        if (isUse != null) {
            stmt.bindString(3, isUse);
        }
 
        String gunNo = entity.getGunNo();
        if (gunNo != null) {
            stmt.bindString(4, gunNo);
        }
        stmt.bindLong(5, entity.getObjectNumber());
 
        String locationType = entity.getLocationType();
        if (locationType != null) {
            stmt.bindString(6, locationType);
        }
 
        String objectName = entity.getObjectName();
        if (objectName != null) {
            stmt.bindString(7, objectName);
        }
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(8, id);
        }
 
        String objectId = entity.getObjectId();
        if (objectId != null) {
            stmt.bindString(9, objectId);
        }
 
        String gunState = entity.getGunState();
        if (gunState != null) {
            stmt.bindString(10, gunState);
        }
 
        String isTemporary = entity.getIsTemporary();
        if (isTemporary != null) {
            stmt.bindString(11, isTemporary);
        }
 
        Long cabId = entity.getCabId();
        if (cabId != null) {
            stmt.bindLong(12, cabId);
        }
        stmt.bindLong(13, entity.getGetNum());
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(14, userId);
        }
 
        String userName = entity.getUserName();
        if (userName != null) {
            stmt.bindString(15, userName);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public SubCabBean readEntity(Cursor cursor, int offset) {
        SubCabBean entity = new SubCabBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // sId
            cursor.getInt(offset + 1), // locationNo
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // isUse
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // gunNo
            cursor.getInt(offset + 4), // objectNumber
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // locationType
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // objectName
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // id
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // objectId
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // gunState
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // isTemporary
            cursor.isNull(offset + 11) ? null : cursor.getLong(offset + 11), // cabId
            cursor.getInt(offset + 12), // getNum
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // userId
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14) // userName
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, SubCabBean entity, int offset) {
        entity.setSId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setLocationNo(cursor.getInt(offset + 1));
        entity.setIsUse(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setGunNo(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setObjectNumber(cursor.getInt(offset + 4));
        entity.setLocationType(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setObjectName(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setId(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setObjectId(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setGunState(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setIsTemporary(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setCabId(cursor.isNull(offset + 11) ? null : cursor.getLong(offset + 11));
        entity.setGetNum(cursor.getInt(offset + 12));
        entity.setUserId(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setUserName(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(SubCabBean entity, long rowId) {
        entity.setSId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(SubCabBean entity) {
        if(entity != null) {
            return entity.getSId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(SubCabBean entity) {
        return entity.getSId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "listLocation" to-many relationship of CabInfoBean. */
    public List<SubCabBean> _queryCabInfoBean_ListLocation(Long cabId) {
        synchronized (this) {
            if (cabInfoBean_ListLocationQuery == null) {
                QueryBuilder<SubCabBean> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.CabId.eq(null));
                cabInfoBean_ListLocationQuery = queryBuilder.build();
            }
        }
        Query<SubCabBean> query = cabInfoBean_ListLocationQuery.forCurrentThread();
        query.setParameter(0, cabId);
        return query.list();
    }

}