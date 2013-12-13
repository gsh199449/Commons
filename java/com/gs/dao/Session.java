/**
 * GS
 */
package com.gs.dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * The Session Class that made by myself
 * @author GaoShen
 * @param <E>
 *            the model which want to be save or load
 * @packageName com.gs.DAO
 */
public class Session<T extends Object> {
	/**
	 * Commons Util
	 * 
	 * @author GaoShen
	 * @packageName com.gs.DAO
	 * @param <K>
	 *            key
	 * @param <V>
	 *            value
	 */
	class Item<K extends Object, V extends Object> {
		public K k;

		public V v;

		/**
		 * @param k
		 * @param v
		 */
		public Item(K k, V v) {
			this.k = k;
			this.v = v;
		}

		@Override
		public String toString() {
			return "Item [k=" + k + ", v=" + v + "]";
		}
	}

	private List<String> columnsName;
	private Connection conn = null;


	/**
	 * @param url
	 * @param username
	 * @param password
	 */
	public Session(String url, String username, String password) {
		try {
			Class.forName("com.mysql.jdbc.Driver");// ����Mysql�����
			conn = DriverManager.getConnection(url, username, password);// �����������

		} catch (Exception e) {
		}

	}

	/**
	 * @param clazz
	 *            the class which will be referenced to create the table
	 * @throws IllegalArgumentException if the model have not be annotated by the @com.gs.DAO.Entity
	 */
	public void createTable(Class<T> clazz) throws IllegalArgumentException {
		T model = null;
		String PKName = null;
		String dbname = clazz.getSimpleName().toLowerCase();
		try {
			model = (T) clazz.newInstance();
			if (!model.getClass().isAnnotationPresent(Entity.class)) {
				throw new IllegalArgumentException(
						"the model must be annotated by the @com.gs.DAO.Entity");
			}
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		Method[] m = model.getClass().getDeclaredMethods();
		Map<String, String> methodMap = new HashMap<String, String>();
		String sql = "CREATE  TABLE `" + dbname + "`.`"
				+ model.getClass().getSimpleName().toLowerCase() + "` ( ";
		for (int j = 0; j < m.length; j++) {

			if (m[j].getName().startsWith("get")) {
				String name = m[j].getName().split("get")[1].toLowerCase();
				if (m[j].isAnnotationPresent(PK.class))
					PKName = name;
				Class<?> retype = m[j].getReturnType();
				String sqltype = null;
				if (retype.isInstance(new String())) {
					sqltype = "VARCHAR(100)";
				} else if (retype.getSimpleName().equals("int")) {
					sqltype = "INT";
				} else if (retype.getSimpleName().equals("long")) {
					sqltype = "INT";
				} else if (retype.getSimpleName().equals("double")) {
					sqltype = "INT";
				} else
					throw new IllegalArgumentException("the item" + name
							+ "can't be resolved");
				methodMap.put(name, sqltype);
				sql += " `" + name + "` " + sqltype + " NOT NULL";
				if (j != (m.length - 1)) {
					sql += ",";
				}
			}
		}
		if (PKName != null) {
			sql += ", PRIMARY KEY (`" + PKName + "`)";
		}

		sql += ");";

		try {
			Statement st;
			st = (Statement) conn.createStatement(); // ��������ִ�о�̬sql����Statement����
			st.executeUpdate(sql);
			st.close();// ִ�в��������sql��䣬�����ز�����ݵĸ���
		} catch (SQLException e) {
		}
	}

	/**
	 * @param clazz
	 *            to Declare the table name
	 */
	public void deleteTable(Class<T> clazz) {
		String sql = "drop table " + clazz.getSimpleName().toLowerCase() + ";";
		try {
			Statement st;
			st = (Statement) conn.createStatement(); // ��������ִ�о�̬sql����Statement����
			st.executeUpdate(sql);
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param tableName
	 */
	public void deleteTable(String tableName) {
		String sql = "drop table " + tableName + ";";
		try {
			Statement st;
			st = (Statement) conn.createStatement(); // ��������ִ�о�̬sql����Statement����
			st.executeUpdate(sql);
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param t
	 *            the class to be referenced
	 * @return the T list
	 * @throws IllegalArgumentException
	 */
	public List<T> list(Class<T> t) throws IllegalArgumentException {
		String PKName = null;
		Method[] m = t.getMethods();
		columnsName = new LinkedList<String>();
		for (int j = 0; j < m.length; j++) {
			if (m[j].getName().startsWith("get")) {
				String name = m[j].getName().split("get")[1];
				columnsName.add(name);
				if (m[j].isAnnotationPresent(PK.class)) {
					PKName = name.toLowerCase();
				}
			}
		}
		if (PKName == null) {
			throw new IllegalArgumentException(
					"there must have a method which annotated by @com.gs.DAO.PK ");
		}
		String sql = "select * from " + t.getSimpleName().toLowerCase();
		ResultSet rs = null;
		try {
			Statement st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		List<T> res = new LinkedList<T>();
		try {
			while (rs.next()) {
				T re = null;
				try {
					re = (T) t.newInstance();
					for (int i = 0; i < m.length; i++) {
						if (m[i].getName().startsWith("set")) {
							Class<?>[] ts = m[i].getParameterTypes();
							String type = ts[0].getSimpleName();
							String column = m[i].getName().toLowerCase()
									.substring(3);
							if (type.equals("int")) {
								m[i].invoke(re, rs.getInt(column));
							} else if (type.equals("double")) {
								m[i].invoke(re, rs.getDouble(column));
							} else if (type.equals("long")) {
								m[i].invoke(re, rs.getLong(column));
							} else if (type.equalsIgnoreCase("String")) {
								m[i].invoke(re, rs.getString(column));
							} else {
								throw new IllegalArgumentException(
										"the type can't be resolved");
							}
						}
					}
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				res.add(re);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * @param t
	 * @param id
	 * @return
	 * @throws IllegalArgumentException
	 *             if there isn't a Primary key or there is a type that can't
	 *             resolved by the method
	 */
	public <E extends Object> T load(Class<T> t, E id)
			throws IllegalArgumentException {
		String PKName = null;
		Method[] m = t.getMethods();
		columnsName = new LinkedList<String>();
		for (int j = 0; j < m.length; j++) {
			if (m[j].getName().startsWith("get")) {
				String name = m[j].getName().split("get")[1];
				columnsName.add(name);
				if (m[j].isAnnotationPresent(PK.class)) {
					PKName = name.toLowerCase();
				}
			}
		}
		if (PKName == null) {
			throw new IllegalArgumentException(
					"there must have a method which annotated by @com.gs.DAO.PK ");
		}
		String sql = "select * from " + t.getSimpleName().toLowerCase()
				+ " where " + PKName + " = " + id;
		ResultSet rs = null;
		try {
			Statement st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);
			rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		T re = null;
		try {
			re = (T) t.newInstance();
			for (int i = 0; i < m.length; i++) {
				if (m[i].getName().startsWith("set")) {
					Class<?>[] ts = m[i].getParameterTypes();
					String type = ts[0].getSimpleName();
					String column = m[i].getName().toLowerCase().substring(3);
					if (type.equals("int")) {
						m[i].invoke(re, rs.getInt(column));
					} else if (type.equals("double")) {
						m[i].invoke(re, rs.getDouble(column));
					} else if (type.equals("long")) {
						m[i].invoke(re, rs.getLong(column));
					} else if (type.equalsIgnoreCase("String")) {
						m[i].invoke(re, rs.getString(column));
					} else {
						throw new IllegalArgumentException(
								"the type can't be resolved");
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return re;
	}

	/**
	 * @param model
	 * @return
	 */
	public boolean save(T model) {
		Method[] m = model.getClass().getDeclaredMethods();
		List<Item<String, Object>> items = new LinkedList<Item<String, Object>>();
		String dbname = model.getClass().getSimpleName().toLowerCase();
		for (int j = 0; j < m.length; j++) {
			if (m[j].getName().startsWith("get")) {
				try {
					items.add(new Item<String, Object>(m[j].getName().split(
							"get")[1].toLowerCase(), m[j].invoke(model, null)));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
		String sql = "INSERT INTO " + dbname + " ( ";
		
		for (int i = 0; i < items.size(); i++) {
			sql += items.get(i).k;
			if (i != (items.size() - 1)) {
				sql += " , ";
			}

		}
		
		sql += " ) VALUES ( ";
		
		for (int i = 0; i < items.size(); i++) {
			boolean stringFlag = false;
			if (items.get(i).v instanceof String) {
				stringFlag = true;
				sql += "'";
			}
			sql += items.get(i).v;
			if (stringFlag) {
				sql += "'";
			}
			if (i != (items.size() - 1)) {
				sql += " , ";
			}

		}
		
		sql += " ) ;";
		int count = 0;
		
		try {
			Statement st;
			st = (Statement) conn.createStatement(); // ��������ִ�о�̬sql����Statement����
			count = st.executeUpdate(sql);
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return true;
	}

}
