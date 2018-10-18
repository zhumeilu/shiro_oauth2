package com.zml.shiro_oauth2_server.cache;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.Enumeration;

public class Utils {

	private final static DecimalFormat df          = new DecimalFormat();
	private final static DecimalFormat dfString    = new DecimalFormat();
	private static       String        mqServiceIp = null;

	static {
		df.setMinimumFractionDigits(0);
		df.setMaximumFractionDigits(0);
		df.setGroupingUsed(false);
		dfString.setMinimumFractionDigits(0);
		dfString.setMaximumFractionDigits(2);
		dfString.setGroupingUsed(false);
	}

	protected static final org.apache.logging.log4j.Logger log = LogManager.getLogger(Utils.class);

	public static boolean isNumber(String s) {
		if (StringUtils.isNotBlank(s)) {
			return s.matches("[\\d.]+");
		}
		return false;
	}

	public static String getLocalIp() {
		String localip = null;// 本地ip,如果没有配置外网ip则返回它
		String netip = null;// 外网ip
		Enumeration<NetworkInterface> netInterfaces;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			boolean finded = false;// 是否找到外网ip
			while (netInterfaces.hasMoreElements() && !finded) {
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> addresses = ni.getInetAddresses();
				while (addresses.hasMoreElements()) {
					ip = addresses.nextElement();
					// 外网ip
					if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
							&& ip.getHostAddress().indexOf(":") == -1) {
						netip = ip.getHostAddress();
						finded = true;
						break;
						// 内网ip
					} else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
							&& ip.getHostAddress().indexOf(":") == -1) {
						localip = ip.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (StringUtils.isNotBlank(netip)) {
			log.info("net ip address" + netip);
			return netip;
		} else {
			log.info("local ip address" + localip);
			return localip;
		}
	}

	/**
	 * 将 二进制转换为16进制
	 *
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = "0" + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 将16进制转换为2进制
	 *
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	/**
	 * 将传入实体类的属性值为null且该属性为字符串类型，则将其置为空字符串
	 *
	 * @param o
	 */
	public static void setEmptyNoException(Object o) {
		try {
			setEmptyForNull(o);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将传入实体类的属性值为null且该属性为字符串类型，则将其置为空字符串
	 *
	 * @param obj
	 * @throws Exception
	 */
	public static void setEmptyForNull(Object obj) throws Exception {
		Class clazz = obj.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			String methodSuffix = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			String getMethod = "get" + methodSuffix;
			if (containMethod(clazz, getMethod)) {
				Method method = clazz.getMethod(getMethod);
				if ("java.lang.String".equalsIgnoreCase(method.getReturnType().getName())) {
					Object value = method.invoke(obj);
					if (value == null) {
						String setMethod = "set" + methodSuffix;
						if (containMethod(clazz, setMethod)) {
							method = clazz.getMethod(setMethod, String.class);
							method.invoke(obj, "");
						}
					}
				}
			}
		}
	}

	/**
	 * 查询该类是否包含指定的方法
	 *
	 * @param clazz
	 * @param methodName
	 * @return
	 */
	public static boolean containMethod(Class clazz, String methodName) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		byte[] b = new byte[] { 4, 2, 12, 16 };
		System.out.println(Utils.parseByte2HexStr(b));
	}

	/**
	 * 获取用户真实的IP
	 *
	 * @param req
	 * @return
	 */
	public static String getRemortIP(HttpServletRequest req) {
		String ip = req.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getRemoteAddr();
		}
		return ip;
	}
}
