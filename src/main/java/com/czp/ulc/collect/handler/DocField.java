package com.czp.ulc.collect.handler;

/**
 * 请添加描述
 * <li> 创建人：Jeff.cao </li>
 * <li> 创建时间：2017年5月13日 下午3:28:59 </li>
 * @version 0.0.1
 */

public interface DocField {
	String TIME = "t";
	String FILE = "f";
	String LINE = "l";
	String HOST = "h";
	String OFFSET = "o";
	String META_FILE = "k";
	String[] ALL_FEILD = { TIME, FILE, LINE,  OFFSET, HOST, META_FILE };
}