/**
 * GS
 */
package com.gs.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 读取Json文件
 * 
 * @author GaoShen
 * @packageName com.gs.io
 */
public class JsonReader<T> implements Closeable {

	private RandomAccessFile rf;
	private File file;
	private Class<T> clazz;

	/**
	 * 读出指定偏移量的PagePOJO 输出的编码格式为utf8
	 * 
	 * @param <T>
	 * 
	 * @param file
	 * @param startoffset
	 * @return
	 * @throws FileNotFoundException
	 */
	public T read(long startoffset) throws FileNotFoundException, IOException {
		String json = null;
		RandomAccessFile r = new RandomAccessFile(file, "r");
		r.seek(startoffset);
		json = new String(r.readLine().getBytes("iso8859-1"), "utf-8");
		Gson gson = new Gson();
		try {
			r.close();
			return gson.fromJson(json, clazz);
		} catch (JsonSyntaxException e) {
			return null;
		}

	}

	/**
	 * 用commons IO包里面的ReadLines方法实现，当问价过大的时候内存会溢出
	 * 将这个文件里的所有json格式的内容制成PagePOJO格式，然后封装在LinkedList中返回
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public LinkedList<T> readFileToPOJOs(String path, Class<T> clazz)
			throws IOException {
		List<String> list = null;
		list = FileUtils.readLines(new File(path));
		Gson g = new Gson();
		LinkedList<T> re = new LinkedList<T>();
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			re.add(g.fromJson(it.next(), clazz));
		}
		return re;
	}

	/**
	 * 如果用到next方法必须调用此构造函数来初始化File
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 */
	public JsonReader(final File file, final Class<T> clazz)
			throws FileNotFoundException {
		this.rf = new RandomAccessFile(file, "r");
		this.file = file;
		this.clazz = clazz;
	}

	public void close() throws IOException {
		this.rf.close();
	}

	/**
	 * 调用next方法之前,查询是否还有下一个Json内容
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean hasNext() throws IOException {
		long p = rf.getFilePointer();
		if (rf.read() == -1) {
			rf.seek(p);
			return false;
		} else {
			rf.seek(p);
			return true;
		}
	}

	/**
	 * 读取下一个Json，前提是已经初始化了FileinputStream
	 * 
	 * @return
	 * @throws Exception
	 */
	public T next() throws Exception {
		if (rf == null)
			throw new Exception(
					"RandomAccessFile未初始化，调用JsonReader(File)来初始化RandomAccessFile");
		String json = new String(rf.readLine().getBytes("iso8859-1"), "utf-8");
		return new Gson().fromJson(json, clazz);
	}

}
