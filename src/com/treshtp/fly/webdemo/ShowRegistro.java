package com.treshtp.fly.webdemo;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShowRegistro extends HttpServlet {

	private static final long serialVersionUID = -8851167207060370967L;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	private Log log = LogFactory.getLog(ShowRegistro.class);

	public ShowRegistro() {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.trace("doGet");
		List<String[]> data = getData();
		log.debug("data: " + data.size());
		printData(data, response.getWriter());
		log.trace("fin doGet");
	}

	private void printData(List<String[]> data, PrintWriter writer) {
		String html = "<html>";
		html += "<head><title>Registro de ingreso</title><style>\n" + "body{font-family: Arial, Helvetica, sans-serif;} \n "
				+ "table{border-collapse:collapse;}\n"
				+ ".claseTitulo{background-color:#5555ff;color: #ffffff; width:100px;}\n"
				+ ".claseTituloLargo{background-color:#5555ff;color: #ffffff; width:300px;}\n"
				+ ".claseTituloCorto{background-color:#5555ff;color: #ffffff; width:70px;}\n"
				+ ".claseTituloMedio{background-color:#5555ff;color: #ffffff; width:170px;}\n"
				+ ".claseTituloExtraLargo{background-color:#5555ff;color: #ffffff; width:300px;}\n"
				+ ".tituloOrigen{background-color:#55ff55;}\n"
				+ ".tituloInterfaz{background-color:#ff5555;}</style></head>";
		html += "<body><br/><br/><form action='showregistro' method='POST'>Ingrese un identificador: 	<input type='text' id='data' name='data' size='50' maxlength='30'> <input type='submit' value='Insertar'/>   <br/>"
				+ "</form>";
		html += "<table border='1'>";
		html += "<tr><td class='claseTitulo'>Identificador</td>" + "<td class='claseTituloLargo'>Server Name</td>"
				+ "<td class='claseTituloCorto'>Java Version</td>"
				+ "<td class='claseTitulo'>Hostname</td>" + "<td class='claseTitulo'>Port</td>"
				+ "<td class='claseTituloExtraLargo'>Browser</td>" + "<td class='claseTitulo'>Ip cliente</td>"
				+ "<td class='claseTituloMedio'>Fecha Ingreso</td></tr>";
		for (int index = 0; data != null && index < data.size(); index++) {
			String[] fila = data.get(index);
			html += "<tr><td>" + fila[0] + "</td><td>" + fila[1] + "</td><td>" + fila[7] + "</td><td>" + fila[2] + "</td><td>" + fila[3]
					+ "</td><td>" + fila[4] + "</td><td>" + fila[5] + "</td><td>" + fila[6] + "</td></tr>";
		}

		html += "</table></body></html>";
		log.info("pagina impresa : " + html.length() + " bytes");
		writer.print(html);
	}

	private List<String[]> getData() {
		List<String[]> temp = new ArrayList<String[]>();
		Connection conn = null;
		PreparedStatement cs = null;
		ResultSet rs = null;
		log.debug("getData");
		try {
			InitialContext ic = new InitialContext();
			Context webContext = (Context) ic.lookup("java:");
			DataSource ds = (DataSource) webContext.lookup("jdbc/postgres");
			conn = ds.getConnection();
			log.debug("getData: conexion a la db: " + conn);
			String sql = "select * from registro order by fecha_ingreso desc";
			log.debug("getData: sql: " + sql);
			cs = conn.prepareStatement(sql);
			rs = (ResultSet) cs.executeQuery();
			log.debug("getData: resultSet: " + rs);
			while (rs != null && rs.next()) {
				String data = rs.getString("data");
				String hostname = rs.getString("hostname");
				String serverName = rs.getString("server_name");
				String port = rs.getString("port");
				Date fechaIngreso = rs.getTimestamp("fecha_ingreso");
				String browser = rs.getString("browser");
				String ip = rs.getString("ipCliente");
				String javaVersion = rs.getString("jdk");
				String[] fila = new String[] { data, serverName, hostname, port, browser, ip, formateaFecha(fechaIngreso), javaVersion };
				log.debug("getData: fila: " + Arrays.asList(fila));
				temp.add(fila);
			}
		} catch (NamingException e) {
			log.error("query NamingException: " + e, e);
		} catch (SQLException e) {
			log.error("query SQLException: " + e, e);
		} catch (Exception e) {
			log.error("query Exception: " + e, e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					log.error("close Exception: " + e, e);
				}
			}
			if (cs != null) {
				try {
					cs.close();
				} catch (Exception e) {
					log.error("close Exception: " + e, e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					log.error("close Exception: " + e, e);
				}
			}
		}
		return temp;
	}

	private String formateaFecha(Date fechaIngreso) {
		try{
			log.trace("formateado fecha " + fechaIngreso);
			return sdf.format(fechaIngreso);
		} catch(Exception e) {
			log.error("Error al parsear la fecha: " + e, e);
		}
		return null;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.trace("doPost");
		ServletContext servletContext = getServletContext();
		log.debug("servername: " + request.getServerName() + ":" + request.getServerPort() + "  info: "
				+ servletContext.getServerInfo() + " java version: " +  System.getProperty("java.version"));
		Connection conn = null;
		String data = request.getParameter("data");
		log.info("data a insertar :" + data);
		String userAgent = request.getHeader("User-Agent");
		String ipCliente = request.getRemoteAddr();
		PreparedStatement cs = null;
		try {
			InitialContext ic = new InitialContext();
			Context webContext = (Context) ic.lookup("java:");
			DataSource ds = (DataSource) webContext.lookup("jdbc/postgres");
			conn = ds.getConnection();
			String sql = "insert into registro(data, server_name, hostname, port, fecha_ingreso, browser, ipcliente, jdk) values (?,?,?,?,current_timestamp,?,?,?)"; 
			cs = conn.prepareStatement(sql);
			setstring(1, data, cs, 100);
			setstring(2, servletContext.getServerInfo(), cs, 100);
			setstring(3, request.getServerName(), cs, 100);
			cs.setInt(4, request.getServerPort());
			setstring(5, userAgent, cs, 200);
			setstring(6, ipCliente, cs, 100);
			setstring(7, System.getProperty("java.version"), cs, 50);
			boolean insertoP = cs.execute();
			log.debug("inserto?: " + insertoP);
		} catch (NamingException e) {
			log.error("insert NamingException: " + e, e);
		} catch (SQLException e) {
			log.error("insert SQLException: " + e, e);
		} catch (Exception e) {
			log.error("insert Exception: " + e, e);
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (Exception e) {
					log.error("close2 Exception: " + e, e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					log.error("close2 Exception: " + e, e);
				}
			}
		}
		log.trace("fin doPost");
		doGet(request, response);
	}

	private void setstring(int fila, String data, PreparedStatement cs, int largo) throws SQLException {
		log.debug("setstring: " + fila + " : " + data + " length: " + (data == null ? -1 : data.length()) + " largo maximo: " + largo);
		cs.setString(fila, (data == null ? null : data.length() < largo? data: data.substring(0, largo)));
	}

}
