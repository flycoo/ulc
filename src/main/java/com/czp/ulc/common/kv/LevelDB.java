package com.czp.ulc.common.kv;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.czp.ulc.common.ShutdownCallback;
import com.czp.ulc.common.util.Utils;

/**
 * Function:KVDB,需要记录每一步的耗时,方便排问题,考虑性能,不采用动态代理
 *
 * @date:2016年6月26日/下午8:44:52
 * @Author:coder_czp@126.com
 * @version:1.0
 */
public class LevelDB implements ShutdownCallback, KVDB {

	private DB db;
	private Options options = new Options();
	private Charset charset = Charset.forName("utf-8");
	private Logger log = LoggerFactory.getLogger(LevelDB.class);
	private static final List<String> EMPTY = new ArrayList<String>();
	private static ConcurrentHashMap<String, LevelDB> dbs = new ConcurrentHashMap<String, LevelDB>();

	private LevelDB(String path) {
		try {
			options.compressionType(CompressionType.SNAPPY);
			options.createIfMissing(true);
			db = factory.open(new File(path), options);
			log.info("kvdb is inited,db:{} path:{}", db, path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static KVDB getGlobDB() {
		return open("./lv_db_glob");
	}

	public static KVDB open(String path) {
		if (!dbs.containsKey(path)) {
			synchronized (dbs) {
				if (!dbs.containsKey(path)) {
					dbs.put(path, new LevelDB(path));
				}
			}
		}
		return dbs.get(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#append(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean append(String key, String values) {
		log.debug("start append key:{},value:{}", key, values);
		String valuesbs = get(key);
		if (valuesbs == null) {
			log.debug("append not found will add key:{}", key);
			put(key, values);
		} else {
			String newVal = values.concat(",").concat(valuesbs);
			db.put(key.getBytes(charset), newVal.getBytes(charset));
		}
		log.debug("success append key:{},value:{}", key, values);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#put(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean put(String key, String values) {
		log.debug("start put to kvdb,key:{},value:{}", key, values);
		db.put(key.getBytes(charset), values.getBytes(charset));
		log.debug("success put to kvdb,key:{},value:{}", key, values);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#put(java.lang.String, long)
	 */
	@Override
	public boolean put(String key, long values) {
		log.debug("start put to kvdb,key:{},value:{}", key, values);
		db.put(key.getBytes(charset), Utils.longToBytes(values));
		log.debug("success put to kvdb,key:{},value:{}", key, values);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#get(java.lang.String)
	 */
	@Override
	public String get(String key) {
		log.info("start get from to kvdb,key:{}", key);
		byte[] values = db.get(key.getBytes(charset));
		if (values == null)
			return null;
		String value = new String(values, charset);
		log.info("success get from to kvdb,key:{},val:", key);
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#get(java.lang.Integer)
	 */
	@Override
	public String get(Integer key) {
		log.debug("start get from to kvdb,key:{}", key);
		byte[] values = db.get(Utils.intToBytes(key));
		if (values == null)
			return null;
		log.debug("success get from to kvdb,key:{},val:", key);
		return new String(values, charset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#hget(java.lang.String, java.lang.String)
	 */
	@Override
	public String hget(String key, String haskKey) {
		String values = get(key);
		if (values == null)
			return null;
		JSONObject value = JSONObject.parseObject(values);
		return value.getString(haskKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#hincr(java.lang.String, java.lang.String)
	 */
	@Override
	public void hincr(String key, String haskKey) {
		String values = get(key);
		if (values == null) {
			JSONObject json = new JSONObject();
			json.put(haskKey, 1);
			put(key, json.toJSONString());
		} else {
			JSONObject value = JSONObject.parseObject(values);
			value.put(haskKey, value.getIntValue(haskKey) + 1);
			put(key, value.toJSONString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#hgetAll(java.lang.String)
	 */
	@Override
	public JSONObject hgetAll(String key) {
		String values = get(key);
		if (values == null) {
			return new JSONObject();
		}
		return JSONObject.parseObject(values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#hput(java.lang.String, java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public boolean hput(String key, String hashKey, Object obj) {
		String values = get(key);
		if (values == null) {
			JSONObject json = new JSONObject();
			json.put(hashKey, obj);
			json.put(key, json.toJSONString());
		} else {
			JSONObject value = JSONObject.parseObject(values);
			value.put(hashKey, obj);
			value.put(key, value.toJSONString());
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#getList(java.lang.String)
	 */
	@Override
	public List<String> getList(String key) {
		String value = get(key);
		if (value == null)
			return EMPTY;
		String[] arr = value.split(",");
		List<String> res = new ArrayList<String>(arr.length);
		for (String string : arr) {
			res.add(string);
		}
		log.debug("success get from to kvdb,key:{},val:{}", key, res);
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#put(byte[], byte[])
	 */
	@Override
	public void put(byte[] key, byte[] value) {
		//log.debug("start put key:{},value:{}", key, value);
		db.put(key, value);
		//log.debug("success put key:{},value:{}", key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#put(byte[], java.lang.String)
	 */
	@Override
	public void put(byte[] key, String value) {
		log.debug("start put key:{},value:{}", key, value);
		db.put(key, value.getBytes(charset));
		log.debug("success put key:{},value:{}", key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#get(byte[])
	 */
	@Override
	public String get(byte[] key) {
		log.debug("start get key:{}", key);
		byte[] valuesBs = db.get(key);
		String string = new String(valuesBs, charset);
		log.debug("success get key:{},value:{}", string);
		return string;
	}

	@Override
	public void onSystemExit() {
		try {
			for (LevelDB levelDB : dbs.values()) {
				if (levelDB.db != null)
					levelDB.db.close();
			}
			dbs.clear();
			log.info("kbdb is closed,{}", db);
		} catch (Exception e) {
			log.error("close db error", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#putInt(java.lang.Integer,
	 * java.lang.String)
	 */
	@Override
	public void putInt(Integer key, String value) {
		log.debug("start put key:{},value:{}", key, value);
		db.put(Utils.intToBytes(key), value.getBytes(charset));
		log.debug("success put key:{},value:{}", key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.czp.ulc.common.kv.KVDB#getLong(java.lang.String, long)
	 */
	@Override
	public long getLong(String key, long dftVal) {
		byte[] values = db.get(key.getBytes(charset));
		if (values == null)
			return dftVal;
		return Utils.bytesToLong(values);
	}

	public static void close(String path) {
		LevelDB levelDB = dbs.get(path);
		if (levelDB != null)
			levelDB.onSystemExit();
	}

	@Override
	public byte[] getBytes(byte[] key) {
		return db.get(key);
	}
}
