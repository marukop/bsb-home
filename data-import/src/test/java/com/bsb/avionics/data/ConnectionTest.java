/**
 * 
 */
package com.bsb.avionics.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bsb.avionics.utils.ConnectionProperties;

import oracle.jdbc.OracleResultSet;
import oracle.jdbc.pool.OracleDataSource;

/**
 * @author Marc PEZZETTI
 *
 */
public class ConnectionTest {

	public static void main(String[] args) {
		ConnectionProperties properties = ConnectionProperties.getInstance();
		Connection connection = null;
		Logger logger = LogManager.getLogger();
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			OracleDataSource ods = new OracleDataSource();
			ods.setTNSEntryName(properties.getSID());
			ods.setUser(properties.getUser());
			ods.setPassword(properties.getPassword());
			ods.setDriverType(properties.getDriverType());
			connection = ods.getConnection();
			connection.setAutoCommit(false);

			String request = properties.getProperty("oracle.request");
			PreparedStatement statement = connection.prepareStatement(request);
			ResultSet result = statement.executeQuery();
			int currentLine = 0;
			while (result.next()) {
				currentLine++;
				ResultSetMetaData metaData = result.getMetaData();
				int size = metaData.getColumnCount();
				StringBuilder resultString = new StringBuilder("Result line ");
				resultString.append("#").append(currentLine).append(": ");
				for (int i = 1; i < size + 1; i++) {
					resultString.append(metaData.getColumnLabel(i)).append(": ");
					switch (metaData.getColumnType(i)) {
					case Types.BIGINT:
					case Types.INTEGER:
						resultString.append(result.getInt(i));
						break;
					case Types.NUMERIC:
						resultString.append(((OracleResultSet) result).getNUMBER(i).intValue());
						break;
					case Types.BOOLEAN:
						resultString.append(result.getBoolean(i));
						break;
					case Types.BLOB:
					case Types.CLOB:
						resultString.append(result.getObject(i));
						break;
					case Types.DATE:
						resultString.append(result.getDate(i));
						break;
					case Types.DOUBLE:
						resultString.append(result.getDouble(i));
						break;
					case Types.FLOAT:
						resultString.append(result.getFloat(i));
						break;
					case Types.TIME:
						resultString.append(result.getTime(i));
						break;
					case Types.TIMESTAMP:
						resultString.append(result.getTimestamp(i));
						break;
					case Types.VARCHAR:
					case Types.NCHAR:
					case Types.NVARCHAR:
					case Types.LONGNVARCHAR:
					case Types.LONGVARCHAR:
						resultString.append(result.getString(i));
						break;
					}
					resultString.append("; ");
				}
				logger.info(resultString.toString());
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
