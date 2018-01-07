/**
 * 
 */
package com.bsb.avionics.data.access.dto;

import oracle.sql.NUMBER;

/**
 * <p>
 * A Data Transfer object used to store Manufacturer & Product identifiers.
 * </p>
 * 
 * @author Marc PEZZETTI
 *
 */
public class ProductAndMfgDTO {

	/**
	 * <p>
	 * The manufacturer identifier.
	 * </p>
	 */
	private NUMBER manufacturerKey;

	/**
	 * <p>
	 * The product identifier.
	 * </p>
	 */
	private NUMBER productKey;

	/**
	 * <p>
	 * Default constructor.
	 * </p>
	 * 
	 * @param aProductKey
	 *            the current product key.
	 * @param aMfgKey
	 *            the current manufacturer key.
	 */
	public ProductAndMfgDTO(NUMBER aProductKey, NUMBER aMfgKey) {
		manufacturerKey = aMfgKey;
		productKey = aProductKey;
	}

	/**
	 * @return the Manufacturer key.
	 */
	public NUMBER getManufacturerKey() {
		return manufacturerKey;
	}

	/**
	 * @return the Product key.
	 */
	public NUMBER getProductKey() {
		return productKey;
	}

	/**
	 * @param aManufacturerKey
	 *            the manufacturer key to set.
	 */
	public void setManufacturerKey(NUMBER aManufacturerKey) {
		this.manufacturerKey = aManufacturerKey;
	}

	/**
	 * @param aProductKey
	 *            the Product key to set.
	 */
	public void setProductKey(NUMBER aProductKey) {
		this.productKey = aProductKey;
	}
}
