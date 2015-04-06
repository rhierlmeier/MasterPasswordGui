package com.lyndir.masterpassword.gui;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class RB {

	private static ResourceBundle RESB = ResourceBundle.getBundle("de.hierlmeier.masterpassword.i18n");
	
	public static String msg(String msg, Object ... args) {
		
		String ret = RESB.getString(msg);
		
		if(args.length != 0) {
			ret = new MessageFormat(ret).format(args);
		}
		
		return ret;
		
	}
}
