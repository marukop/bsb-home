/**
 * 
 */
package com.bsb.avionics.utils;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Marc PEZZETTI
 *
 */
public class ConnectionProperties {

	/**
	 * <p>
	 * Unique instance of this class.
	 * </p>
	 */
	private static final ConnectionProperties PROPERTIES = new ConnectionProperties();

	/**
	 * @return the singleton instance.
	 */
	public static ConnectionProperties getInstance() {
		return PROPERTIES;
	}

	private Properties connectionProperties;

	/**
	 * <p>Default constructor.</p>
	 */
	private ConnectionProperties() {
		connectionProperties = new Properties();
		try {
			connectionProperties.load(ConnectionProperties.class.getResourceAsStream("/conf/connection.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the description column name for the Access table.
	 */
	public String getAccessTableColumnDescription() {
		return connectionProperties.getProperty("access.table.column.desc");
	}

	/**
	 * @return the manufacturer column name for the Access table.
	 */
	public String getAccessTableColumnManufacturer() {
		return connectionProperties.getProperty("access.table.column.manu");
	}

	/**
	 * @return the note column name for the Access table.
	 */
	public String getAccessTableColumnNote() {
		return connectionProperties.getProperty("access.table.column.note");
	}

	/**
	 * @return the PN column name for the Access table.
	 */
	public String getAccessTableColumnPN() {
		return connectionProperties.getProperty("access.table.column.pn");
	}

	/**
	 * @return the UOM column name for the Access table.
	 */
	public String getAccessTableColumnUOM() {
		return connectionProperties.getProperty("access.table.column.uom");
	}

	/**
	 * @return the table name for the Access database.
	 */
	public String getAccessTableName() {
		return connectionProperties.getProperty("access.table.name");
	}

	/**
	 * @return the Oracle driver type to use.
	 */
	public String getDriverType() {
		return connectionProperties.getProperty("oracle.driver.type");
	}

	/**
	 * @return the host to use to connect to the Oracle DB.
	 */
	public String getHost() {
		return connectionProperties.getProperty("oracle.host");
	}

	/**
	 * @return the password to use for user authentication when connecting to the
	 *         Oracle DB.
	 */
	public String getPassword() {
		return connectionProperties.getProperty("oracle.passwd");
	}

	/**
	 * @return the port to use to connect to the Oracle DB.
	 */
	public String getPort() {
		return connectionProperties.getProperty("oracle.port");
	}

	/**
	 * @param aKey
	 *            the key to look for.
	 * @return the property.
	 */
	public String getProperty(final String aKey) {
		return connectionProperties.getProperty(aKey);
	}

	/**
	 * @return the SID to connect to.
	 */
	public String getSID() {
		return connectionProperties.getProperty("oracle.sid");
	}

	/**
	 * @return the user to use to connect to the Oracle DB.
	 */
	public String getUser() {
		return connectionProperties.getProperty("oracle.user");
	}
}
