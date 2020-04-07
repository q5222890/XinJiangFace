package com.zack.xjht.entity;

import com.zack.xjht.Utils.Utils;
import com.zack.xjht.ui.fragment.UrgentTaskListBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import com.zack.xjht.db.gen.DaoSession;
import com.zack.xjht.db.gen.UrgentBackListBeanDao;
import com.zack.xjht.db.gen.UrgentGetListBeanDao;
import com.zack.xjht.db.gen.UrgentOutBeanDao;

/**
 * 紧急领取枪弹数据实体类
 */
@Entity
public class UrgentOutBean implements Comparable<UrgentOutBean>{

    //    private String gunCabinetId; //枪柜id
//    private String apply;       //申请人id
//    private String approval;     //审批人id
//    private String outTime;     //领出时间
    private String remark;      //备注
//    private List<UrgentTaskListBean> urgentTaskList; //领出枪弹数据

    @Id(autoincrement = true)
    private Long tId;
    private String gunCabinetId; //枪柜id
    private String apply;       //申请人id
    private String approval;     //审批人id
    private String applyName;   //申请人姓名
    private String approvalName;  //审批人姓名
    private String outTime;     //领出时间
    private String inTime;      //归还时间
    private String updateTime;     //更新时间 提交到后台的时间
    private String urgentTaskId;  //任务id
    @ToMany(referencedJoinProperty = "taskGetId")
    private List<UrgentGetListBean> urgentGetList; //领出枪弹数据
    @ToMany(referencedJoinProperty = "taskBackId")
    private List<UrgentBackListBean> urgentBackList; //归还枪弹数据
    private boolean isGetUpload;  //领出是否已上传服务器 默认false，当联网后开始上传到后台 上传成功置为true
    private boolean isBackUpload;   //归还是否已上传服务器 默认false，当联网后开始上传到后台 上传成功置为true
    private boolean taskFinish;   //是否结束任务 默认false,当枪弹归还完成置为true;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1889184553)
    private transient UrgentOutBeanDao myDao;

    @Generated(hash = 140852010)
    public UrgentOutBean(String remark, Long tId, String gunCabinetId, String apply, String approval,
            String applyName, String approvalName, String outTime, String inTime, String updateTime,
            String urgentTaskId, boolean isGetUpload, boolean isBackUpload, boolean taskFinish) {
        this.remark = remark;
        this.tId = tId;
        this.gunCabinetId = gunCabinetId;
        this.apply = apply;
        this.approval = approval;
        this.applyName = applyName;
        this.approvalName = approvalName;
        this.outTime = outTime;
        this.inTime = inTime;
        this.updateTime = updateTime;
        this.urgentTaskId = urgentTaskId;
        this.isGetUpload = isGetUpload;
        this.isBackUpload = isBackUpload;
        this.taskFinish = taskFinish;
    }

    @Generated(hash = 1236654055)
    public UrgentOutBean() {
    }

    public String getGunCabinetId() {
        return gunCabinetId;
    }

    public void setGunCabinetId(String gunCabinetId) {
        this.gunCabinetId = gunCabinetId;
    }

    public String getApply() {
        return apply;
    }

    public void setApply(String apply) {
        this.apply = apply;
    }

    public String getApproval() {
        return approval;
    }

    public void setApproval(String approval) {
        this.approval = approval;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public Long getTId() {
        return this.tId;
    }

    public void setTId(Long tId) {
        this.tId = tId;
    }

    public String getApplyName() {
        return this.applyName;
    }

    public void setApplyName(String applyName) {
        this.applyName = applyName;
    }

    public String getApprovalName() {
        return this.approvalName;
    }

    public void setApprovalName(String approvalName) {
        this.approvalName = approvalName;
    }

    public String getInTime() {
        return this.inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getUrgentTaskId() {
        return this.urgentTaskId;
    }

    public void setUrgentTaskId(String urgentTaskId) {
        this.urgentTaskId = urgentTaskId;
    }

    public boolean getIsGetUpload() {
        return this.isGetUpload;
    }

    public void setIsGetUpload(boolean isGetUpload) {
        this.isGetUpload = isGetUpload;
    }

    public boolean getIsBackUpload() {
        return this.isBackUpload;
    }

    public void setIsBackUpload(boolean isBackUpload) {
        this.isBackUpload = isBackUpload;
    }

    public boolean getTaskFinish() {
        return this.taskFinish;
    }

    public void setTaskFinish(boolean taskFinish) {
        this.taskFinish = taskFinish;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 83564456)
    public List<UrgentGetListBean> getUrgentGetList() {
        if (urgentGetList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UrgentGetListBeanDao targetDao = daoSession.getUrgentGetListBeanDao();
            List<UrgentGetListBean> urgentGetListNew = targetDao
                    ._queryUrgentOutBean_UrgentGetList(tId);
            synchronized (this) {
                if (urgentGetList == null) {
                    urgentGetList = urgentGetListNew;
                }
            }
        }
        return urgentGetList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1562152543)
    public synchronized void resetUrgentGetList() {
        urgentGetList = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1174161938)
    public List<UrgentBackListBean> getUrgentBackList() {
        if (urgentBackList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UrgentBackListBeanDao targetDao = daoSession.getUrgentBackListBeanDao();
            List<UrgentBackListBean> urgentBackListNew = targetDao
                    ._queryUrgentOutBean_UrgentBackList(tId);
            synchronized (this) {
                if (urgentBackList == null) {
                    urgentBackList = urgentBackListNew;
                }
            }
        }
        return urgentBackList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1722397716)
    public synchronized void resetUrgentBackList() {
        urgentBackList = null;
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

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1011903592)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getUrgentOutBeanDao() : null;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setUrgentGetList(List<UrgentGetListBean> urgentGetList) {
        this.urgentGetList = urgentGetList;
    }

    public void setUrgentBackList(List<UrgentBackListBean> urgentBackList) {
        this.urgentBackList = urgentBackList;
    }

    @Override
    public int compareTo(UrgentOutBean o) {
        return (int) (Utils.stringTime2Long(outTime) -Utils.stringTime2Long(o.getOutTime()));
    }
}
