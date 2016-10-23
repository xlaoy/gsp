/** 
 */
package test;

import com.gewara.util.PKCoderUtil;



/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Apr 28, 2014  11:57:39 AM
 */
public class TestFTP {
	public static void main(String[] args) {
		String s = PKCoderUtil.encodeString("dz_ftp");
		String ss = PKCoderUtil.encodeString("gewara123");
		System.out.println("user:" + s);
		System.out.println("pwd:" + ss);
	}
}
