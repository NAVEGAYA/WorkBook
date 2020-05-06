package com.house.springboot.workbook.backend.models;

public class T_SYST_CAT {
	private String MODUL;
	private String CATEG;
	private String DESCR;
	private String TYPPE;
	private String LABEL;
	private String TICON;
	private int ORDEN;
	
	public String getMODUL() {
		return MODUL;
	}
	
	public void setMODUL(String mODUL) {
		MODUL = mODUL;
	}
	
	public String getCATEG() {
		return CATEG;
	}
	
	public void setCATEG(String cATEG) {
		CATEG = cATEG;
	}
	
	public String getDESCR() {
		return DESCR;
	}
	
	public void setDESCR(String dESCR) {
		DESCR = dESCR;
	}
	
	public String getTYPPE() {
		return TYPPE;
	}

	public void setTYPPE(String tYPPE) {
		TYPPE = tYPPE;
	}

	public String getLABEL() {
		return LABEL;
	}
	
	public void setLABEL(String lABEL) {
		LABEL = lABEL;
	}
	
	public String getTICON() {
		return TICON;
	}
	
	public void setTICON(String tICON) {
		TICON = tICON;
	}
	
	public int getORDEN() {
		return ORDEN;
	}
	
	public void setORDEN(int oRDEN) {
		ORDEN = oRDEN;
	}
}
