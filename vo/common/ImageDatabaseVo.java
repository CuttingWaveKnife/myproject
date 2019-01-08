package com.cb.vo.common;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.common.ImageDatabase;
import com.cb.model.common.ImageDatabase.TypeEnum;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ImageDatabaseVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7233710910054557630L;
	
    private String name;    	//图片显示名字
    private String filePath;    //图片路径
    
    public static ImageDatabaseVo toImageDatabaseVo(ImageDatabase imageDatabase){    
    	ImageDatabaseVo imageDatabaseVo = new ImageDatabaseVo();
    	BeanUtil.copyPropertiesWithoutNullValues(imageDatabaseVo,imageDatabase);
    	//额外处理
    	return imageDatabaseVo;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
   

    
    
	
}
