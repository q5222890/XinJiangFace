package com.zack.xjht.entity;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import com.zack.xjht.db.gen.DaoSession;
import com.zack.xjht.db.gen.SubCabBeanDao;
import com.zack.xjht.db.gen.CabInfoBeanDao;

/**
 * 枪柜数据实体类
 */
@Entity
public class CabInfoBean {

    @Id(autoincrement = true)
    private Long cId;
    private String gunCabinetType; //枪柜类型 1:枪柜 2:弹柜 3:枪弹一体柜
    private String gunRoomName;  //枪库名称
    private String id;              //枪柜id
    private String gunCabinetNo;    //枪柜编号
    @ToMany(referencedJoinProperty = "cabId")
    private List<SubCabBean> listLocation;//枪弹数据列表
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 807014660)
    private transient CabInfoBeanDao myDao;

    @Generated(hash = 966587810)
    public CabInfoBean(Long cId, String gunCabinetType, String gunRoomName,
            String id, String gunCabinetNo) {
        this.cId = cId;
        this.gunCabinetType = gunCabinetType;
        this.gunRoomName = gunRoomName;
        this.id = id;
        this.gunCabinetNo = gunCabinetNo;
    }

    @Generated(hash = 634911876)
    public CabInfoBean() {
    }

    public String getGunCabinetType() {
        return gunCabinetType;
    }

    public void setGunCabinetType(String gunCabinetType) {
        this.gunCabinetType = gunCabinetType;
    }

    public String getGunRoomName() {
        return gunRoomName;
    }

    public void setGunRoomName(String gunRoomName) {
        this.gunRoomName = gunRoomName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGunCabinetNo() {
        return gunCabinetNo;
    }

    public void setGunCabinetNo(String gunCabinetNo) {
        this.gunCabinetNo = gunCabinetNo;
    }

    public void setListLocation(List<SubCabBean> listLocation) {
        this.listLocation = listLocation;
    }

    public Long getCId() {
        return this.cId;
    }

    public void setCId(Long cId) {
        this.cId = cId;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1894723591)
    public List<SubCabBean> getListLocation() {
        if (listLocation == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SubCabBeanDao targetDao = daoSession.getSubCabBeanDao();
            List<SubCabBean> listLocationNew = targetDao
                    ._queryCabInfoBean_ListLocation(cId);
            synchronized (this) {
                if (listLocation == null) {
                    listLocation = listLocationNew;
                }
            }
        }
        return listLocation;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 2015156655)
    public synchronized void resetListLocation() {
        listLocation = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 30733488)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCabInfoBeanDao() : null;
    }

}
