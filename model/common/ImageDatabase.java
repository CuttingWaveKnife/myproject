package com.cb.model.common;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import com.cb.model.security.User;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * 图片库
 */
@Entity
@Table(name = "c_image_database")
@Where(clause = Constants.DELETED_FALSE)
public class ImageDatabase extends CommonEntity {

    public enum TypeEnum {
        //产品
        PRODUCT,
        //订单
        ORDER,
        //头部轮播图
        BANNER,
    }

    private TypeEnum type;  //图片所属

    private String foreignId;   //外键id

    private String name;    //图片显示名字

    private String fileType;    //图片类型

    private String fileName;    //图片名称

    private String filePath;    //图片路径

    private Integer sort;   //排序

    private User user;  //上传用户

    @Enumerated(value = EnumType.STRING)
    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    @Column(name = "foreign_id")
    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "file_type")
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Column(name = "file_name")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "file_path")
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return this.filePath;
    }
}
