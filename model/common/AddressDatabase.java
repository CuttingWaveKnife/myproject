package com.cb.model.common;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 * 地址库
 */
@Entity
@Table(name = "c_address_database")
@Where(clause = Constants.DELETED_FALSE)
public class AddressDatabase extends CommonEntity {

    private String name;    //地址名称

    private String code;    //地址code

    private LevelEnum level;    //地址等级（0，1，2）

    private String type;    //地址类型（省、市、区）

    private String deliver; //京东仓库对应地址

    private AddressDatabase parent; //父级地址

    private List<AddressDatabase> children; //子集合地址

    public enum LevelEnum {
        PROVINCE("省"), CITY("市"), AREA("区");

        private String desc;

        private LevelEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    @Column(nullable = false, length = 45)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = 5)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Enumerated(value = EnumType.ORDINAL)
    @Column(nullable = false)
    public LevelEnum getLevel() {
        return level;
    }

    public void setLevel(LevelEnum level) {
        this.level = level;
    }

    @Column(nullable = false, length = 1)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeliver() {
        return deliver;
    }

    public void setDeliver(String deliver) {
        this.deliver = deliver;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public AddressDatabase getParent() {
        return parent;
    }

    public void setParent(AddressDatabase parent) {
        this.parent = parent;
    }

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    public List<AddressDatabase> getChildren() {
        return children;
    }

    public void setChildren(List<AddressDatabase> children) {
        this.children = children;
    }
}
