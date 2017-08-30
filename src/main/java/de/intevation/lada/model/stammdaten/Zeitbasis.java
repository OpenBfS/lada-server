package de.intevation.lada.model.stammdaten;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the zeitbasis database table.
 * 
 */
@Entity
@NamedQuery(name="Zeitbasis.findAll", query="SELECT z FROM Zeitbasis z")
public class Zeitbasis implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private Integer id;

	private String bezeichnung;

	public Zeitbasis() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getBezeichnung() {
		return this.bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

}