package com.zml.shiro_oauth2_server.cache;

import java.io.Serializable;

public class CacheJson implements Serializable {

	private static final long serialVersionUID = 811157572797534004L;

	private String key;

	private String json;

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}