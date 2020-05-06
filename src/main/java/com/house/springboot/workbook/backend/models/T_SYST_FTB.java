package com.house.springboot.workbook.backend.models;

public class T_SYST_FTB {
	private String MODUL;
	private String CATEG;
	private String FORMA;
	private String TABLA;
	private String DESCR;
	private int ORDEN;
	private boolean VROWS;
	
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
	
	public String getFORMA() {
		return FORMA;
	}
	
	public void setFORMA(String fORMA) {
		FORMA = fORMA;
	}
	
	public String getTABLA() {
		return TABLA;
	}
	
	public void setTABLA(String tABLA) {
		TABLA = tABLA;
	}
	
	public String getDESCR() {
		return DESCR;
	}
	
	public void setDESCR(String dESCR) {
		DESCR = dESCR;
	}
	
	public int getORDEN() {
		return ORDEN;
	}
	
	public void setORDEN(int oRDEN) {
		ORDEN = oRDEN;
	}
	
	public boolean getVROWS() {
		return VROWS;
	}
	
	public void setVROWS(boolean vROWS) {
		VROWS = vROWS;
	}
}
