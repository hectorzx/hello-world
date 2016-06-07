package api;

import android.graphics.Color;

public class Butaca {
	private int col;
	private int row;
			
	private int idbutaca;
	private boolean vip;
	private int codigoseccion;
	private int fila;
	private int columna;
	private int atributos;
	private EstadoButaca estado; 
	
	public final static int VIPCOLOR = Color.BLUE;
	public final static int SELECCIONADOCOLOR = Color.RED;
	public final static int DISPONIBLECOLOR = Color.GREEN;
	public final static int NODISPONIBLECOLOR = Color.BLACK;
	public final static int OCUPADOCOLOR = Color.GRAY;
	public final static int VACIOCOLOR = Color.WHITE;

	public final static int ERRORCOLOR = Color.YELLOW;

	
	
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getIdbutaca() {
		return idbutaca;
	}
	public void setIdbutaca(int idbutaca) {
		this.idbutaca = idbutaca;
	}
	
	public int getCodigoseccion() {
		return codigoseccion;
	}
	public void setCodigoseccion(int codigoseccion) {
		this.codigoseccion = codigoseccion;
	}
	public int getFila() {
		return fila;
	}
	public void setFila(int fila) {
		this.fila = fila;
	}
	public int getColumna() {
		return columna;
	}
	public void setColumna(int columna) {
		this.columna = columna;
	}
	public int getAtributos() {
		return atributos;
	}
	public void setAtributos(int atributos) {
		this.atributos = atributos;
	}
	public EstadoButaca getEstado() {
		return estado;
	}
	public void setEstado(EstadoButaca estado) {
		this.estado = estado;
	}
	public boolean isVip() {
		return vip;
	}
	public void setVip(boolean vip) {
		this.vip = vip;
	}
}
