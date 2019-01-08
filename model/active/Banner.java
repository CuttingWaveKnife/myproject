package com.cb.model.active;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import com.cb.common.util.DateUtil;
import com.cb.common.util.StringUtil;
import com.cb.model.common.ImageDatabase;
import com.cb.model.product.Product;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by GuoMIn on 2017/3/21.
 */
@Entity
@Table(name = "a_active_banner")
@Where(clause = Constants.DELETED_FALSE)
public class Banner extends CommonEntity {

    public enum StatusEnum {
        ON("上架"),
        OFF("未上架");

        private String desc;

        private StatusEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    private String code;//编码

    private String title;//标题

    private String url;//链接

    private String remark;//备注

    private StatusEnum status;//状态

    private String sort;//排序

    private String description;//描述详情

    private Date startTime;//起始时间

    private String datepicker;//活动时间

    private Date endTime;//结束时间

    private ImageDatabase image;//图片

    private Product product;//活动下的产品

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getSort() {
        return sort;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Column(name = "start_time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public Date getStartTime() {
        return startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Column(name = "end_time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public Date getEndTime() {
        return endTime;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id", unique = true)
    @NotFound(action = NotFoundAction.IGNORE)
    public Product getProduct() {
        return product;
    }

   /*
   * @OneToOne注释指明Person 与IDCard为一对一关系，@OneToOne注释五个属性：targetEntity、cascade、fetch、optional 和mappedBy，
   *fetch属性默认值是FetchType.EAGER。optional = true设置idcard属性可以为null,也就是允讦没有身份证，未成年人就是没有身份证的。
   *
   *targetEntity属性:Class类型的属性。定义关系类的类型，默认是该成员属性对应的类类型，所以通常不需要提供定义。
   *cascade属性：CascadeType[]类型。该属性定义类和类之间的级联关系。定义的级联关系将被容器视为对当前类对象及其关联类对象采取相同的操作，
   *而且这种关系是递归调用的。cascade的值只能从CascadeType.PERSIST（级联新建）、CascadeType.REMOVE（级联删除）、
   *CascadeType.REFRESH（级联刷新）、CascadeType.MERGE（级联更新）中选择一个或多个。还有一个选择是使用CascadeType.ALL，表示选择全部四项。
   *
   *fetch属性：FetchType类型的属性。可选择项包括：FetchType.EAGER 和FetchType.LAZY。
   *FetchType.EAGER表示关系类(本例是OrderItem类)在主类加载的时候同时加载，FetchType.LAZY表示关系类在被访问时才加载。默认值是FetchType.LAZY。
   *
   *@OrderBy(value = "id ASC")注释指明加载元组时按id的升序排序（降序 "DESC"）
   */
    public void setProduct(Product product) {
        this.product = product;
    }

    @Transient
    public String getDatepicker() {
        if (this.getStartTime() != null && this.getEndTime() != null) {
            return DateUtil.dateToString(this.getStartTime(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS) + " - " + DateUtil.dateToString(this.getEndTime(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS);
        }
        return this.datepicker;
    }

    public void setDatepicker(String datepicker) {
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                this.setStartTime(DateUtil.parse(temps[0], DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
                this.setEndTime(DateUtil.parse(temps[1], DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
                this.datepicker = datepicker;
            }
        }
    }

    public void setImage(ImageDatabase image) {
        this.image = image;
    }

    @OneToOne
    @JoinColumn(name = "image_id")
    public ImageDatabase getImage() {
        return image;
    }
}
