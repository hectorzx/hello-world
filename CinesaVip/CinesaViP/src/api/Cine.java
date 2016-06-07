package api;

public class Cine {

	private int id;
	private String nombre;
	
	public Cine(String id,String nombre){
		this.id = Integer.valueOf(id);
		this.nombre = nombre;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
