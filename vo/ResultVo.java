package com.cb.vo;

import com.cb.common.util.JsonUtil;
import com.cb.common.util.StringUtil;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 返回信息封装
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResultVo {


    private String message;     //返回信息

    private boolean success;    //返回状态

    private Integer code = 0;   //操作码

    private Map<String, Object> data = new HashMap<>();        //返回数据

    public ResultVo() {
        this.message = "系统错误";
    }

    public ResultVo(boolean success) {
        this.success = success;
        this.message = "成功";
    }

    public ResultVo(String message) {
        this.message = message;
    }

    public ResultVo(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public ResultVo(String message, boolean success, Map<String, Object> data) {
        this.message = message;
        this.success = success;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message, String... params) {
        if (params != null && params.length > 0) {
            this.message = MessageFormat.format(message, params);
        } else {
            this.message = message;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 向data中存放数据
     *
     * @param key   键
     * @param value 值
     */
    public void put(String key, Object value) {
        if (StringUtil.isNotBlank(key)) {
            getData().put(key, value);
        }
    }

    /**
     * 从data中获取数据
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        if (getData() != null) {
            return getData().get(key);
        }
        return null;
    }

    /**
     * 将result转为json字符串
     *
     * @return json字符串
     */
    public String toJsonString() {
        return JsonUtil.toFullJson(this);
    }

    /**
     * 设置成功
     */
    public void success() {
        setSuccess(true);
        setMessage("成功");
    }

    @Override
    public String toString() {
        return "ResultVo{" +
                "message='" + message + '\'' +
                ", success=" + success +
                ", code=" + code +
                ", data=" + data +
                '}';
    }
}
