package com.cb.vo.active;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.cb.common.hibernate.query.Page;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.active.Banner;
import com.cb.model.active.Banner.StatusEnum;
import com.cb.vo.PageVo;
import com.cb.vo.common.ImageDatabaseVo;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class BannerVo implements Serializable {

	/**
	 * 
	 */

	private String code;		//编码
    private String title;		//标题
    private String url;			//链接
    private String remark;		//备注
    private Integer status;	//状态 (ON上架 1 ，OFF下架 －1) 
    private String sort;		//排序
    private String description;	//描述详情
    private String  datepicker;	//活动时间
    private Long startTime;		//起始时间
    private Long endTime;		//结束时间
    private ImageDatabaseVo image;	//图片
	//private ProductVo product;	//活动下的产品
	
    public static BannerVo toBannerVo(Banner banner){    
    	BannerVo bannerVo = new BannerVo();
    	BeanUtil.copyPropertiesWithoutNullValues(bannerVo,banner);
    	//额外处理
    	bannerVo.setStatus(banner.getStatus());
    	bannerVo.setStartTime(banner.getStartTime().getTime());
    	bannerVo.setEndTime(banner.getEndTime().getTime());    	
    	bannerVo.setImage(ImageDatabaseVo.toImageDatabaseVo(banner.getImage()));
    	
    	return bannerVo;
    }
    
    public static PageVo<BannerVo> toPageVo(Page<Banner> page){
    	PageVo<BannerVo> voPage = new PageVo<>();
    	BeanUtil.copyPropertiesWithoutNullValues(voPage, page);
    	List<BannerVo> list = new ArrayList<BannerVo>(page.getResult().size());
    	for(Banner banner : page.getResult()){
    		BannerVo bannerVo = toBannerVo(banner);
    		list.add(bannerVo);
    	}
    	voPage.setList(list);
    	return voPage;
    }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	 //(ON上架 1 ，OFF下架 －1)
	public void setStatus(StatusEnum statusEnum){
		if(statusEnum == StatusEnum.ON){
			this.status = 1;
		}
		if(statusEnum == StatusEnum.OFF){
			this.status = -1;
		}
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public String getDatepicker() {
		return datepicker;
	}

	public void setDatepicker(String datepicker) {
		this.datepicker = datepicker;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public ImageDatabaseVo getImage() {
		return image;
	}

	public void setImage(ImageDatabaseVo image) {
		this.image = image;
	}


}
