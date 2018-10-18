package com.zml.shiro_oauth2_server.cache;

/**
 * 模块代码常量类<br>
 * 代码分类三类：<br>
 * 1:模块类型,两位编码<br>
 * 2:模块下业务类型,五位编码（模块编码+业务编码）<br>
 * 3:模块下业务子类型,八位编码（模块编码+业务编码+子类型编号）<br>
 */
public class BasicCode {
	/**
	 * 公共模块
	 */
	public final static String PUBLIC = "00";
	// # 缓存
	public static final String PUBLIC_CACHE = "00001";
	/**
	 * 微信
	 */
	public final static String BIZ_WX = "01";

	// 微信模版
	public static final String WX_TEMPLATE = "01001";
	//
	public static final String WX_TEMPLATE_SUB01 = "01001001";

}
