package api;

public class Sesion {

	private String horario;
	private String tipo;
	private String sala;
	private int id;
	private String compra;
	
	public Sesion(String horario, int id){
		this.horario = horario;
		this.id = id;		
	}
	
	public Sesion(String horario, int id, String tipo, String sala, String compra){
		this.horario = horario;
		this.id = id;		
		this.tipo = tipo;
		this.sala = sala;
		this.compra = compra;
	}
	
	public String getHorario() {
		return horario;
	}
	public void setHorario(String horario) {
		this.horario = horario;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getSala() {
		return sala;
	}

	public void setSala(String sala) {
		this.sala = sala;
	}

	public String getCompra() {
		return compra;
	}

	public void setCompra(String compra) {
		this.compra = compra;
	}
}
