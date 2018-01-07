/**
 * 
 */
package com.bsb.avionics.data.importer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.bsb.avionics.model.Product;
import com.monitorjbl.xlsx.StreamingReader;
import com.monitorjbl.xlsx.impl.StreamingCell;

/**
 * <p>
 * Data importer for XLSX file kind.
 * </p>
 * 
 * @author Marc PEZZETTI
 *
 */
public class XLSXDataImporter extends DataImporter {

	/**
	 * <p>
	 * The logger to use for this class.
	 * </p>
	 */
	private static final Logger LOGGER = LogManager.getLogger(XLSXDataImporter.class);

	/**
	 * <p>
	 * The XLSX file currently importer.
	 * </p>
	 */
	private Path xlsxFile;

	/**
	 * <p>
	 * Default constructor.
	 * </p>
	 * 
	 * @param aFile
	 *            the XLSX file to import in the system.
	 */
	public XLSXDataImporter(final Path aFile) {
		super();
		xlsxFile = aFile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void retrieveData() {
		long t0 = System.currentTimeMillis();
		try (InputStream is = new FileInputStream(xlsxFile.toFile());
				Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(is)) {
			long tRead = System.currentTimeMillis();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Time spent reading file: " + (tRead - t0));
			}
			Sheet currentSheet = workbook.getSheetAt(0);
			int rowIndex = 0;
			for (Row row : currentSheet) {
				if (rowIndex == 0) {
					// Skip the first row. It contains the row's titles.
					rowIndex++;
					continue;
				}
				Cell pnCell = row.getCell(0);
				Cell descriptionCell = row.getCell(1);
				Cell manufacturerCell = row.getCell(2);
				Cell uomCell = row.getCell(3);
				StreamingCell noteCell = (StreamingCell) row.getCell(4);
				String pn = pnCell.getRichStringCellValue().toString();
				String description = descriptionCell.getRichStringCellValue().getString();
				String manufacturer = manufacturerCell.getRichStringCellValue().getString();
				String uom = uomCell.getRichStringCellValue().getString();
				String note = noteCell.getStringCellValue();

				Product current = new Product();
				current.setDescription(description);
				current.setManufacturer(manufacturer);
				current.setNote(note);
				current.setPartNumber(pn);
				current.setUnitOfMeasure(uom);
				Set<Product> products = productsByPN.get(pn);
				if (products == null) {
					products = new HashSet<>();
				}
				products.add(current);
				productsByPN.put(pn, products);
			}

			if (LOGGER.isDebugEnabled()) {
				// Retrieved all parts.
				LOGGER.debug("Retrieved all parts: " + (System.currentTimeMillis() - tRead));
			}
			int productCount = 0;
			for (Entry<String, Set<Product>> product : productsByPN.entrySet()) {
				productCount += product.getValue().size();
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Number of different products retrieved: " + productCount);
			}
		} catch (IOException ioe) {
			LOGGER.error("Exception while reading the XLSX file.", ioe);
		}
	}
}
