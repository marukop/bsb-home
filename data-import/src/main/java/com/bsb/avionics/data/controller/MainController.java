/**
 * 
 */
package com.bsb.avionics.data.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.Option;

import com.bsb.avionics.data.access.ProductDAO;
import com.bsb.avionics.data.importer.AccessImporter;
import com.bsb.avionics.data.importer.DataImporter;
import com.bsb.avionics.data.importer.XLSXDataImporter;
import com.bsb.avionics.model.Product;

/**
 * <p>
 * Main controller launching the genuine import for the currently handled file.
 * </p>
 * 
 * @author Marc PEZZETTI
 *
 */
public class MainController {

	/**
	 * The logger to use.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The file to import. It can either be a XLSX or a MDB file.
	 */
	private Path fileToImport;

	/**
	 * <p>
	 * Default constructor.
	 * </p>
	 */
	public MainController() {
	}

	/**
	 * <p>
	 * Launch the main import process.
	 * </p>
	 */
	public void launchProcess() {
		String fileToImportName = this.fileToImport.getFileName().toString();
		String fileExtension = fileToImportName.substring(fileToImportName.lastIndexOf(".") + 1);
		DataImporter importer;
		switch (fileExtension.toUpperCase()) {
		case "MDB":
		case "ACCDB":
			importer = new AccessImporter(this.fileToImport);
			break;
		case "XLSX":
			importer = new XLSXDataImporter(this.fileToImport);
			break;
		default:
			importer = null;
			break;
		}
		importer.retrieveData();
		List<Product> products = importer.getProductsByPN().values().stream().flatMap(Set::stream)
				.collect(Collectors.toList());

		ProductDAO dao = new ProductDAO();
		dao.handleProductList(products);
	}

	/**
	 * @param aFileToImport
	 *            the file to import to use.
	 */
	@Option(name = "-file", aliases = {
			"-f" }, usage = "Sets the file to import in Quantum - either XLSX, MDB or ACCDB file")
	public void setFileToImport(String aFileToImport) {
		Path path = Paths.get(aFileToImport);
		if (Files.exists(path)) {
			this.fileToImport = path;
		} else {
			LOGGER.error("Provided file doesn't exists.");
		}
	}
}
