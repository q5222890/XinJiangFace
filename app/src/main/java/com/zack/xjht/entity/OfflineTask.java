package com.zack.xjht.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.ToOne;

import com.zack.xjht.db.gen.DaoSession;
import com.zack.xjht.db.gen.OfflineTaskItemDao;
import com.zack.xjht.db.gen.OfflineTaskDao;
import com.zack.xjht.db.gen.UserBeanDao;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class OfflineTask {

    @Id(autoincrement = true)
    private Long id;
    private long startTime;  //添加时间
    private long finishTime;    //结束时间
    private int taskStatus;     //任务状态 1.已领出2.结束
    private long applyId;   //申请人id
    private long approveId;  //审批人id
    @ToOne(joinProperty ="applyId")
    private UserBean apply;   //申请人
    @ToOne(joinProperty ="approveId")
    private UserBean approve; //审批人
    private long updateTime;  //更新时间
    @ToMany(referencedJoinProperty = "taskId")
    private List<OfflineTaskItem> offlineTaskItemList;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 904032842)
    private transient OfflineTaskDao myDao;
    @Generated(hash = 268720065)
    private transient Long apply__resolvedKey;
    @Generated(hash = 1949177076)
    private transient Long approve__resolvedKey;

    @Generated(hash = 246834221)
    public OfflineTask(Long id, long startTime, long finishTime, int taskStatus, long applyId,
            long approveId, long updateTime) {
        this.id = id;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.taskStatus = taskStatus;
        this.applyId = applyId;
        this.approveId = approveId;
        this.updateTime = updateTime;
    }
    @Generated(hash = 878896513)
    public OfflineTask() {
    }
    
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getTaskStatus() {
        return this.taskStatus;
    }
    public void setTaskStatus(int taskStatus) {
        this.taskStatus = taskStatus;
    }
    public long getUpdateTime() {
        return this.updateTime;
    }
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 694932284)
    public List<OfflineTaskItem> getOfflineTaskItemList() {
        if (offlineTaskItemList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            OfflineTaskItemDao targetDao = daoSession.getOfflineTaskItemDao();
            List<OfflineTaskItem> offlineTaskItemListNew = targetDao
                    ._queryOfflineTask_OfflineTaskItemList(id);
            synchronized (this) {
                if (offlineTaskItemList == null) {
                    offlineTaskItemList = offlineTaskItemListNew;
                }
            }
        }
        return offlineTaskItemList;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1224828500)
    public synchronized void resetOfflineTaskItemList() {
        offlineTaskItemList = null;
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
    @Generated(hash = 1800424183)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getOfflineTaskDao() : null;
    }
    public long getStartTime() {
        return this.startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public long getFinishTime() {
        return this.finishTime;
    }
    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }
    public Long getApplyId() {
        return this.applyId;
    }
    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }
    public Long getApproveId() {
        return this.approveId;
    }
    public void setApproveId(Long approveId) {
        this.approveId = approveId;
    }
    public void setApplyId(long applyId) {
        this.applyId = applyId;
    }
    public void setApproveId(long approveId) {
        this.approveId = approveId;
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 712270442)
    public UserBean getApply() {
        long __key = this.applyId;
        if (apply__resolvedKey == null || !apply__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserBeanDao targetDao = daoSession.getUserBeanDao();
            UserBean applyNew = targetDao.load(__key);
            synchronized (this) {
                apply = applyNew;
                apply__resolvedKey = __key;
            }
        }
        return apply;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 201896636)
    public void setApply(@NotNull UserBean apply) {
        if (apply == null) {
            throw new DaoException(
                    "To-one property 'applyId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.apply = apply;
            applyId = apply.getUId();
            apply__resolvedKey = applyId;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 960345437)
    public UserBean getApprove() {
        long __key = this.approveId;
        if (approve__resolvedKey == null || !approve__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserBeanDao targetDao = daoSession.getUserBeanDao();
            UserBean approveNew = targetDao.load(__key);
            synchronized (this) {
                approve = approveNew;
                approve__resolvedKey = __key;
            }
        }
        return approve;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 58038658)
    public void setApprove(@NotNull UserBean approve) {
        if (approve == null) {
            throw new DaoException(
                    "To-one property 'approveId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.approve = approve;
            approveId = approve.getUId();
            approve__resolvedKey = approveId;
        }
    }


}
