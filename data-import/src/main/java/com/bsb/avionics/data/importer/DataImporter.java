/**
 * 
 */
package com.bsb.avionics.data.importer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.bsb.avionics.model.Product;

/**
 * @author Marc PEZZETTI
 *
 */
public abstract class DataImporter {

	/**
	 * 
	 */
	protected Map<String, Set<Product>> productsByPN;

	/**
	 * <p>
	 * Default constructor.
	 * </p>
	 */
	public DataImporter() {
		productsByPN = new HashMap<>();
	}

	/**
	 * @return the map containing the products by their Part Numbers.
	 */
	public final Map<String, Set<Product>> getProductsByPN() {
		return productsByPN;
	}

	/**
	 * <p>
	 * Retrieve data from the data source.
	 * </p>
	 */
	public abstract void retrieveData();
}
