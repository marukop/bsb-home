/**
 * 
 */
package com.bsb.avionics.data.access;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bsb.avionics.data.access.dto.ProductAndMfgDTO;
import com.bsb.avionics.model.Product;
import com.bsb.avionics.utils.ConnectionProperties;

import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OracleTypes;
import oracle.jdbc.pool.OracleDataSource;
import oracle.sql.NUMBER;

/**
 * @author Marc PEZZETTI
 *
 */
public class ProductDAO {

	/**
	 * <p>
	 * Query to use to insert an ALTERNATE product.
	 * </p>
	 */
	private static final String ALT_PRODUCT_INSERT = "INSERT INTO ALTERNATES_PARTS_MASTER (APM_AUTO_KEY, PNM_AUTO_KEY, ALT_PNM_AUTO_KEY) "
			+ "VALUES (G_APM_AUTO_KEY.NEXTVAL, ?, ?)";

	/**
	 * <p>
	 * The logger to use for this class.
	 * </p>
	 */
	private static final Logger LOGGER = LogManager.getLogger(ProductDAO.class);

	/**
	 * <p>
	 * Query to use to insert and retrieve the Manufacturer identifier.
	 * </p>
	 */
	private static final String MFG_INSERT = "INSERT INTO MANUFACTURER (MFG_AUTO_KEY, DESCRIPTION, MFG_CODE) VALUES (G_MFG_AUTO_KEY.NEXTVAL, ' ', ?) RETURNING MFG_AUTO_KEY INTO ?";

	/**
	 * <p>
	 * Query to search for a specific Manufacturer.
	 * </p>
	 */
	private static final String MFG_SEARCH = "SELECT MFG_AUTO_KEY FROM MANUFACTURER WHERE MFG_CODE = ?";

	/**
	 * <p>
	 * Query to search for a specific Product.
	 * </p>
	 */
	private static final String PN_SEARCH = "SELECT PNM_AUTO_KEY, MFG_AUTO_KEY FROM PARTS_MASTER WHERE PN = ?";

	/**
	 * <p>
	 * Query to use to insert a PRODUCT.
	 * </p>
	 */
	private static final String PRODUCT_INSERT = "INSERT INTO PARTS_MASTER (PNM_AUTO_KEY, PN, PN_UPPER, DESCRIPTION, DESCRIPTION_UPPER, MFG_AUTO_KEY, UOM_AUTO_KEY, NOTES) "
			+ "VALUES (G_PNM_AUTO_KEY.NEXTVAL, ?, ?, ?, ?, ?, ?, ?) RETURNING PNM_AUTO_KEY INTO ?";

	/**
	 * <p>
	 * Query to use to insert a PRODUCT for an alternate PRODUCT.
	 * </p>
	 */
	private static final String PRODUCT_INSERT_FOR_ALT = "INSERT INTO PARTS_MASTER (PNM_AUTO_KEY, PN, PN_UPPER, DESCRIPTION, DESCRIPTION_UPPER, MFG_AUTO_KEY, UOM_AUTO_KEY, NOTES, REMARKS, SERIALIZED) "
			+ "VALUES (G_PNM_AUTO_KEY.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, 'OCDIA', 'F') RETURNING PNM_AUTO_KEY INTO ?";

	/**
	 * <p>
	 * Query to use to insert and retrieve the UOM identifier.
	 * </p>
	 */
	private static final String UOM_INSERT = "INSERT INTO UOM_CODES (UOM_AUTO_KEY, UOM_CODE) VALUES (G_UOM_AUTO_KEY.NEXTVAL, ?) RETURNING UOM_AUTO_KEY INTO ?";

	/**
	 * <p>
	 * Query to search for a specific UOM.
	 * </p>
	 */
	private static final String UOM_SEARCH = "SELECT UOM_AUTO_KEY FROM UOM_CODES WHERE UOM_CODE = ?";

	/**
	 * <p>
	 * The connection to use to query the database.
	 * </p>
	 */
	private Connection connection;

	/**
	 * <p>
	 * Default constructor establishing the DB connection.
	 * </p>
	 */
	public ProductDAO() {
		ConnectionProperties properties = ConnectionProperties.getInstance();
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			OracleDataSource ods = new OracleDataSource();
			ods.setTNSEntryName(properties.getSID());
			ods.setUser(properties.getUser());
			ods.setPassword(properties.getPassword());
			ods.setDriverType(properties.getDriverType());
			connection = ods.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException | ClassNotFoundException e) {
			LOGGER.fatal("An exception occurred while establishing the connection.", e);
		}
	}

	/**
	 * @throws SQLException
	 *             if an error occurs while shutting down the connection.
	 * 
	 */
	public void dispose() throws SQLException {
		connection.close();
	}

	/**
	 * <p>
	 * Check whether the PartNumber - Manufacturer combination exists in DB.
	 * </p>
	 * 
	 * @param aPartNumber
	 *            the part number (truncated) to check.
	 * @param aManufacturerId
	 *            the manufacturer identifier to use.
	 * @return <code>true</code> if the combination exists, <code>false</code>
	 *         otherwise.
	 * @throws SQLException
	 *             if an error occurs while querying the DB.
	 */
	private boolean existsProductAndManufacturer(String aPartNumber, NUMBER aManufacturerId) throws SQLException {
		boolean exists = false;
		try (OraclePreparedStatement productStatement = (OraclePreparedStatement) connection
				.prepareStatement(PN_SEARCH);) {
			productStatement.setString(1, getTruncatedOrValue(aPartNumber, Product.PARTS_PN_LENGTH));
			OracleResultSet result = (OracleResultSet) productStatement.executeQuery();
			if (result.next()) {
				do {
					NUMBER mfgId = result.getNUMBER(2);
					exists = mfgId.longValue() == aManufacturerId.longValue();
				} while (result.next() && !exists);
			}
		}
		return exists;
	}

	/**
	 * <p>
	 * Retrieve the manufacturer identifier. If none can be found, it will be
	 * inserted in DB.
	 * </p>
	 * 
	 * @param aManufacturer
	 *            the Manufacturer code to use to query the DB.
	 * @return the manufacturer identifier.
	 * @throws SQLException
	 *             if an error occurs while querying the DB.
	 */
	private NUMBER getManufacturerOrInsert(final String aManufacturer) throws SQLException {
		NUMBER manufacturerId;
		String truncatedManufacturerCode = getTruncatedOrValue(aManufacturer, Product.MFG_CODE_LENGTH);
		try (OraclePreparedStatement manufacturerStatement = (OraclePreparedStatement) connection
				.prepareStatement(MFG_SEARCH);) {
			manufacturerStatement.setString(1, truncatedManufacturerCode);
			OracleResultSet result = (OracleResultSet) manufacturerStatement.executeQuery();
			if (result.next()) {
				manufacturerId = result.getNUMBER(1);
			} else {
				try (OraclePreparedStatement manufacturerInsert = (OraclePreparedStatement) connection
						.prepareStatement(MFG_INSERT);) {
					manufacturerInsert.setString(1, truncatedManufacturerCode);
					manufacturerInsert.registerReturnParameter(2, OracleTypes.NUMBER);
					manufacturerInsert.execute();
					OracleResultSet manufacturerResult = (OracleResultSet) manufacturerInsert.getReturnResultSet();
					manufacturerResult.next();
					manufacturerId = manufacturerResult.getNUMBER(1);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Manufacturer inserted: " + aManufacturer + " (truncated to "
								+ truncatedManufacturerCode + "). New id: " + manufacturerId.intValue());
					}
				}
			}
		}
		return manufacturerId;
	}

	/**
	 * <p>
	 * Get the value of the provided string of its truncated value if the length is
	 * greater or equals than the second argument.
	 * </p>
	 * 
	 * @param aValue
	 *            the value to use.
	 * @param aMaxLength
	 *            the maximum length of the string.
	 * @return the provided string or its truncated version.
	 */
	private String getTruncatedOrValue(String aValue, final int aMaxLength) {
		String truncatedValue = aValue;
		if (truncatedValue.length() >= aMaxLength) {
			truncatedValue = truncatedValue.substring(0, aMaxLength - 1);
		}
		return truncatedValue;
	}

	/**
	 * <p>
	 * Retrieve the UOM identifier. If none can be found, it will be inserted in DB.
	 * </p>
	 * 
	 * @param aUOM
	 *            the Unit Of Measure to use.
	 * @return the UOM identifier.
	 * @throws SQLException
	 *             if an error occurs while querying the DB.
	 */
	private NUMBER getUOMOrInsert(final String aUOM) throws SQLException {
		NUMBER uomId;
		try (OraclePreparedStatement uomStatement = (OraclePreparedStatement) connection
				.prepareStatement(UOM_SEARCH);) {
			uomStatement.setString(1, aUOM);
			OracleResultSet result = (OracleResultSet) uomStatement.executeQuery();
			if (result.next()) {
				uomId = result.getNUMBER(1);
			} else {
				try (OraclePreparedStatement uomQuery = (OraclePreparedStatement) connection
						.prepareStatement(UOM_INSERT)) {
					uomQuery.setString(1, aUOM);
					uomQuery.registerReturnParameter(2, OracleTypes.NUMERIC);
					uomQuery.execute();
					OracleResultSet uomResult = (OracleResultSet) uomQuery.getReturnResultSet();
					uomResult.next();
					uomId = uomResult.getNUMBER(1);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("UOM inserted: " + aUOM + ". New Id: " + uomId.intValue());
					}
				}
			}
		}
		return uomId;
	}

	/**
	 * <p>
	 * Handle a product, i.e. check if it exists and then: <br/>
	 * - either insert it. <br/>
	 * - or insert an alternate product.
	 * </p>
	 * 
	 * @param aProduct
	 *            the product to handle.
	 * @throws SQLException
	 *             if an exception occurs.
	 */
	private void handleProduct(Product aProduct) throws SQLException {
		NUMBER mfgId = getManufacturerOrInsert(aProduct.getManufacturer());
		// SELECT pn?
		try (OraclePreparedStatement productStatement = (OraclePreparedStatement) connection
				.prepareStatement(PN_SEARCH);) {
			productStatement.setString(1, getTruncatedOrValue(aProduct.getPartNumber(), Product.PARTS_PN_LENGTH));
			OracleResultSet result = (OracleResultSet) productStatement.executeQuery();
			if (result.next()) {
				// IF FOUND: insert an alternate to it !
				// Same Manufacturer ?
				Map<Long, ProductAndMfgDTO> resultMap = new HashedMap<>(result.getFetchSize());
				do {
					ProductAndMfgDTO currentDTO = new ProductAndMfgDTO(result.getNUMBER(1), result.getNUMBER(2));
					resultMap.put(currentDTO.getManufacturerKey().longValue(), currentDTO);
				} while (result.next());
				ProductAndMfgDTO foundDTO = resultMap.get(mfgId.longValue());
				if (foundDTO == null) {
					foundDTO = resultMap.values().iterator().next();
					insertAlternateProduct(aProduct, foundDTO.getProductKey(), mfgId);
				} else {
					// log nothing to do.
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Current Product not insert with no alternate. Product already found in database.");
						LOGGER.info("Found Product: " + aProduct.getPartNumber() + "; manufacturer: "
								+ aProduct.getManufacturer());
					}
				}
			} else {
				// IF not found: INSERT !
				insertProduct(aProduct, mfgId);
			}
		}
	}

	/**
	 * <b>Handle the given {@link Product} list.</b>
	 * 
	 * @param aProductList
	 *            the product list to use.
	 */
	public void handleProductList(List<Product> aProductList) {
		int batchCount = 0;
		for (Product currentProduct : aProductList) {
			try {
				handleProduct(currentProduct);
				batchCount++;
				if (batchCount % 10 == 0) {
					connection.commit();
				}
			} catch (SQLException sqle) {
				LOGGER.error("Error while handling the product list.", sqle);
			}
		}
	}

	/**
	 * <p>
	 * When a duplicate is found, an alternate product is created.
	 * </p>
	 * 
	 * @param aProduct
	 *            the product to use for the alternate description.
	 * @param aPartNumberId
	 *            the part number identifier corresponding to the alternate to
	 *            create.
	 * @param aMFGIdentifier
	 *            the Manufacturer identifier (either created or retrieved from
	 *            Database).
	 * @throws SQLException
	 *             if an error occurs while querying the DB.
	 */
	private void insertAlternateProduct(Product aProduct, NUMBER aPartNumberId, NUMBER aMFGIdentifier)
			throws SQLException {
		insertAlternateProduct(aProduct, aPartNumberId, aMFGIdentifier, false);
	}

	/**
	 * <p>
	 * When a duplicate is found, an alternate product is created.
	 * </p>
	 * 
	 * @param aProduct
	 *            the product to use for the alternate description.
	 * @param aPartNumberId
	 *            the part number identifier corresponding to the alternate to
	 *            create.
	 * @param aMFGIdentifier
	 *            the Manufacturer identifier (either created or retrieved from
	 *            Database).
	 * @param isForAlternate
	 *            flag indicating if this method is called to insert an alternate
	 *            for the PN (when <code>false</code>) or for the Alternate PN (when
	 *            <code>true</code>).
	 * @throws SQLException
	 *             if an error occurs while querying the DB.
	 */
	private void insertAlternateProduct(Product aProduct, NUMBER aPartNumberId, NUMBER aMFGIdentifier,
			boolean isForAlternate) throws SQLException {
		NUMBER uomId = getUOMOrInsert(aProduct.getUnitOfMeasure());

		String partNumber = isForAlternate ? aProduct.getAlternatePartNumber() : aProduct.getPartNumber();
		String truncatedPN = getTruncatedOrValue(partNumber, Product.PARTS_PN_LENGTH);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Trying to insert alternate product for PN: " + partNumber + " (truncated to " + truncatedPN
					+ "). Existing one: " + aPartNumberId.longValue());
		}
		// Insert an alternate part
		NUMBER altProductId = null;
		if (!existsProductAndManufacturer(truncatedPN, aMFGIdentifier)) {
			try (OraclePreparedStatement altProductStatement = (OraclePreparedStatement) connection
					.prepareStatement(PRODUCT_INSERT_FOR_ALT);) {
				altProductStatement.setString(1, truncatedPN);
				altProductStatement.setString(2, truncatedPN.toUpperCase());
				String truncatedDescription = getTruncatedOrValue(aProduct.getDescription(),
						Product.PARTS_DESCRITPION_LENGTH);
				altProductStatement.setString(3, truncatedDescription);
				altProductStatement.setString(4, truncatedDescription.toUpperCase());
				altProductStatement.setNUMBER(5, aMFGIdentifier);
				altProductStatement.setNUMBER(6, uomId);
				altProductStatement.setClob(7, new StringReader(aProduct.getNote()));
				altProductStatement.registerReturnParameter(8, OracleTypes.NUMBER);
				altProductStatement.execute();
				OracleResultSet altResultSet = (OracleResultSet) altProductStatement.getReturnResultSet();
				altResultSet.next();
				altProductId = altResultSet.getNUMBER(1);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Inserting in alternate parts master. PartNumber " + aPartNumberId.longValue()
						+ "; Alternate PartNumber: " + altProductId.longValue());
			}
			try (OraclePreparedStatement productStatement = (OraclePreparedStatement) connection
					.prepareStatement(ALT_PRODUCT_INSERT);) {
				productStatement.setNUMBER(1, aPartNumberId);
				productStatement.setNUMBER(2, altProductId);
				productStatement.executeUpdate();
			}
			if (!isForAlternate && altProductId != null) {
				insertAlternateProduct(aProduct, altProductId, aMFGIdentifier, true);
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Alternate product creation is skipped because the combination part number (" + truncatedPN
						+ ") and manufacturer (" + aProduct.getManufacturer() + ") already exists in database.");
			}
		}
	}

	/**
	 * <p>
	 * Insert a new product in database. Check whether the manufacturer & unit of
	 * measure are to be created.
	 * </p>
	 * 
	 * @param aProduct
	 *            the product to insert.
	 * @param aMFGIdentifier
	 *            the Manufacturer identifier (either created or retrieved from
	 *            Database).
	 * @throws SQLException
	 *             if an error occurs while querying the DB.
	 */
	private void insertProduct(Product aProduct, NUMBER aMFGIdentifier) throws SQLException {
		NUMBER uomId = getUOMOrInsert(aProduct.getUnitOfMeasure());

		NUMBER partNumberId = null;
		try (OraclePreparedStatement productStatement = (OraclePreparedStatement) connection
				.prepareStatement(PRODUCT_INSERT);) {
			String truncatedPN = getTruncatedOrValue(aProduct.getPartNumber(), Product.PARTS_PN_LENGTH);
			productStatement.setString(1, truncatedPN);
			productStatement.setString(2, truncatedPN.toUpperCase());
			String truncatedDescription = getTruncatedOrValue(aProduct.getDescription(),
					Product.PARTS_DESCRITPION_LENGTH);
			productStatement.setString(3, truncatedDescription);
			productStatement.setString(4, truncatedDescription.toUpperCase());
			productStatement.setNUMBER(5, aMFGIdentifier);
			productStatement.setNUMBER(6, uomId);
			productStatement.setClob(7, new StringReader(aProduct.getNote()));
			productStatement.registerReturnParameter(8, OracleTypes.NUMBER);
			productStatement.execute();
			OracleResultSet productResultSet = (OracleResultSet) productStatement.getReturnResultSet();
			productResultSet.next();
			partNumberId = productResultSet.getNUMBER(1);

		}
		if (partNumberId != null && aProduct.getAlternatePartNumber() != null) {
			insertAlternateProduct(aProduct, partNumberId, aMFGIdentifier, true);
		}
	}
}
