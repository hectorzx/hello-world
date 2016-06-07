package api;

import java.util.List;
import java.util.Set;

public class Pelicula {

	private String nombre;
	private int id;
	private String urlImagen;
	private List<Sesion> sesiones;
	private Sesion sesionTemporal;
	private String directores;
	private String actores;
	private String genero;
	private String duracion;
	private Set<String> fechas;
	private float valoracion;
	private int votos;
	private String sinopsis;
	private String observaciones;
	private String tralier;
	
	public void copiarValores(Pelicula p){
		this.setNombre(p.getNombre());
		this.setId(p.getId());
		this.setUrlImagen(p.getUrlImagen());
		this.setDirectores(p.getDirectores());
		this.setActores(p.getActores());
		this.setGenero(p.getGenero());
		this.setDuracion(p.getDuracion());

	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUrlImagen() {
		return urlImagen;
	}
	public void setUrlImagen(String urlImagen) {
		this.urlImagen = urlImagen;
	}
	public List<Sesion> getSesiones() {
		return sesiones;
	}
	public void setSesiones(List<Sesion> sesiones) {
		this.sesiones = sesiones;
	}
	public Sesion getSesionTemporal() {
		return sesionTemporal;
	}
	public void setSesionTemporal(Sesion sesionTemporal) {
		this.sesionTemporal = sesionTemporal;
	}
	/*public Drawable getImagen() {
		if(urlImagen!=null && imagen==null){
			NetworkCinesa nC = NetworkCinesa.getInstance();
			Drawable imgTemp = nC.cargarImagen(urlImagen);
			imagen = imgTemp;
		}
		
		return imagen;
	}
	public void setImagen(Drawable imagen) {
		this.imagen = imagen;
	}*/
	public String getDirectores() {
		return directores;
	}
	public void setDirectores(String directores) {
		this.directores = directores;
	}
	public String getActores() {
		return actores;
	}
	public void setActores(String actores) {
		this.actores = actores;
	}
	public String getGenero() {
		return genero;
	}
	public void setGenero(String genero) {
		this.genero = genero;
	}
	public String getDuracion() {
		return duracion;
	}
	public void setDuracion(String duracion) {
		this.duracion = duracion;
	}

	public Set<String> getFechas() {
		return fechas;
	}

	public void setFechas(Set<String> fechas) {
		this.fechas = fechas;
	}

	public float getValoracion() {
		return valoracion;
	}

	public void setValoracion(float valoracion) {
		this.valoracion = valoracion;
	}

	public int getVotos() {
		return votos;
	}

	public void setVotos(int votos) {
		this.votos = votos;
	}

	public String getSinopsis() {
		return sinopsis;
	}

	public void setSinopsis(String sinopsis) {
		this.sinopsis = sinopsis;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public String getTralier() {
		return tralier;
	}

	public void setTralier(String tralier) {
		this.tralier = tralier;
	}
	
}
