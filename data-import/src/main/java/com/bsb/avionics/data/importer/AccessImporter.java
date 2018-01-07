/**
 * 
 */
package com.bsb.avionics.data.importer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bsb.avionics.model.Product;
import com.bsb.avionics.utils.ConnectionProperties;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.util.Joiner;

/**
 * <p>
 * Data importer used to retrieve products from Microsoft's Access database.
 * </p>
 * 
 * @author Marc PEZZETTI
 *
 */
public class AccessImporter extends DataImporter {

	/**
	 * <p>
	 * The logger to use for this class.
	 * </p>
	 */
	private static final Logger LOGGER = LogManager.getLogger(AccessImporter.class);

	/**
	 * <p>
	 * The Microsoft Access file to use to import data.
	 * </p>
	 */
	private Path mdbFile;

	/**
	 * <p>
	 * Default constructor.
	 * </p>
	 * 
	 * @param aDataPath
	 *            the Path to the MDB file to import.
	 */
	public AccessImporter(Path aDataPath) {
		super();
		this.mdbFile = aDataPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void retrieveData() {
		ConnectionProperties properties = ConnectionProperties.getInstance();
		DatabaseBuilder dbb = new DatabaseBuilder(this.mdbFile.toFile()).setAutoSync(false).setReadOnly(true);
		try (Database db = dbb.open()) {
			db.getRelationships();
			Table productTable = db.getTable(properties.getAccessTableName());
			Table supplementsTable = db.getTable("Supplements");
			Index supIdIndex = productTable.getIndex("SupplementsParts");
			Index holderIdIndex = supplementsTable.getIndex("HoldersSupplements");

			Joiner supJoiner = Joiner.create(supIdIndex);
			Joiner holderJoiner = Joiner.create(holderIdIndex);
			Cursor productCursor = CursorBuilder.createCursor(productTable);
			Row supHolderRow, holderRow;
			String manufacturer, description, pn, alternatePN, uom;
			Set<Product> products;
			for (Row productRow : productCursor) {
				// Perform the Joins.
				supHolderRow = supJoiner.findFirstRow(productRow);
				holderRow = null;
				if (supHolderRow != null) {
					holderRow = holderJoiner.findFirstRow(supHolderRow);
				}
				manufacturer = "";
				if (holderRow != null) {
					manufacturer = holderRow.getString("Holder");
				}
				description = productRow.getString(properties.getAccessTableColumnDescription());
				pn = productRow.getString(properties.getAccessTableColumnPN());
				alternatePN = productRow.getString("ReplacedPartNumber");
				uom = productRow.getString(properties.getAccessTableColumnUOM());
				if (StringUtils.isEmpty(uom)) {
					uom = properties.getAccessTableColumnUOM();
				}


				Product current = new Product();
				current.setDescription(description);
				current.setManufacturer(manufacturer);
				current.setNote("");
				current.setPartNumber(pn);
				current.setUnitOfMeasure(uom);
				current.setAlternatePartNumber(alternatePN);
				products = productsByPN.get(pn);
				if (products == null) {
					products = new HashSet<>();
				}
				products.add(current);
				productsByPN.put(pn, products);
			}
		} catch (IOException ioe) {
			LOGGER.error("Exception in the Access DB opening / querying.", ioe);
		}
	}

}
