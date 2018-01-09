/**
 * 
 */
package com.bsb.avionics.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <p>
 * Represent the product.
 * </p>
 * 
 * @author Marc PEZZETTI
 *
 */
public class Product {

	/**
	 * <p>
	 * The Manufacturer MFG_CODE (manufacturer name) length.
	 * </p>
	 */
	public static final int MFG_CODE_LENGTH = 20;

	/**
	 * <p>
	 * The Parts table's Description length.
	 * </p>
	 */
	public static final int PARTS_DESCRITPION_LENGTH = 50;

	/**
	 * <p>The Parts table's PN length.</p>
	 */
	public static final int PARTS_PN_LENGTH = 40;

	/**
	 * <p>
	 * The alternate part number (i.e. an alternative to the current product's part
	 * number).
	 * </p>
	 */
	private String alternatePartNumber;

	/**
	 * <p>
	 * The description (limited to 255 characters).
	 * </p>
	 */
	private String description;

	/**
	 * <p>
	 * The manufacturer identifier (limited to 255 characters).
	 * </p>
	 */
	private String manufacturer;

	/**
	 * <p>
	 * The product's note (limited to 255 characters).
	 * </p>
	 */
	private String note;

	/**
	 * <p>
	 * The part number (i.e. identifier).
	 * </p>
	 */
	private String partNumber;

	/**
	 * <p>
	 * The unit of measure as stored in database.
	 * </p>
	 */
	private String unitOfMeasure;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		Product rhs = (Product) obj;
		return new EqualsBuilder().append(description, rhs.getDescription()).append(manufacturer, rhs.getManufacturer())
				.append(note, rhs.getNote()).append(partNumber, rhs.getPartNumber())
				.append(unitOfMeasure, rhs.getUnitOfMeasure()).append(alternatePartNumber, rhs.getAlternatePartNumber())
				.isEquals();
	}

	/**
	 * @return the alternate part number.
	 */
	public String getAlternatePartNumber() {
		return alternatePartNumber;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		return manufacturer;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @return the partNumber.
	 */
	public String getPartNumber() {
		return partNumber;
	}

	/**
	 * @return the unitOfMeasure
	 */
	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(description).append(manufacturer).append(note).append(partNumber)
				.append(unitOfMeasure).toHashCode();
	}

	/**
	 * @param aAlternatePartNumber
	 *            the alternate part number to set.
	 */
	public void setAlternatePartNumber(String aAlternatePartNumber) {
		this.alternatePartNumber = aAlternatePartNumber;
	}

	/**
	 * @param aDescription
	 *            the description to set
	 */
	public void setDescription(String aDescription) {
		this.description = aDescription;
	}

	/**
	 * @param aManufacturer
	 *            the manufacturer to set
	 */
	public void setManufacturer(String aManufacturer) {
		this.manufacturer = aManufacturer;
	}

	/**
	 * @param aNote
	 *            the note to set
	 */
	public void setNote(String aNote) {
		this.note = aNote;
	}

	/**
	 * @param aPartNumber
	 *            the partNumber to set.
	 */
	public void setPartNumber(String aPartNumber) {
		this.partNumber = aPartNumber;
	}

	/**
	 * @param aUnitOfMeasure
	 *            the unit of measure (UOM) to set.
	 */
	public void setUnitOfMeasure(String aUnitOfMeasure) {
		this.unitOfMeasure = aUnitOfMeasure;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("pn", partNumber).append("description", description)
				.append("manufacturer", manufacturer).append("note", note).append("UOM", unitOfMeasure).toString();
	}
}
