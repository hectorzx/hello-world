package api;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.SharedPreferences;

public class Parser {

	private static Parser instance = null;
	private List<String> ciudades;
	HashMap<String,List<Cine>> cines;
	private Pelicula peliActual;
	
	
	protected Parser() {
	      // Exists only to defeat instantiation.
	 }
	   
	public static Parser getInstance() {
	    if(instance == null) {
	       instance = new Parser();
	    }
    	return instance;
	}
	
	public void guardarCacheFavoritos(Context contexto,String ciudad, String idCine){
		
		SharedPreferences sh = contexto.getSharedPreferences("cinesavip", Context.MODE_PRIVATE);
		SharedPreferences.Editor shEditor = sh.edit();		
		shEditor.putString("ciudadF", ciudad);
		shEditor.putString("cineF", idCine);		
		
		shEditor.commit();

	}
	
	public String cargarCacheCiudadFavoritos(Context contexto){
		
		SharedPreferences sh = contexto.getSharedPreferences("cinesavip", Context.MODE_PRIVATE);
		return sh.getString("ciudadF", null);

	}
	public String cargarCacheCineFavoritos(Context contexto){
		
		SharedPreferences sh = contexto.getSharedPreferences("cinesavip", Context.MODE_PRIVATE);
		return sh.getString("cineF", null);

	}
	public void guardarCache(Context contexto){
		
		SharedPreferences sh = contexto.getSharedPreferences("cinesavip", Context.MODE_PRIVATE);
		SharedPreferences.Editor shEditor = sh.edit();		
		
		shEditor.putStringSet("ciudades", cines.keySet());
		for(String ciudad : cines.keySet()){
			
			Set<String> cinesGuardar = new HashSet<String>();
			for(Cine cine : cines.get(ciudad)){
			
				cinesGuardar.add(String.valueOf(cine.getId())+":"+String.valueOf(cine.getNombre()));
				
			}
			shEditor.putStringSet(ciudad, cinesGuardar);
		}		
		shEditor.commit();

		Log.w("guardarCache", "datosGuardados");
	}
	
	public boolean cargarCache(Context contexto){
		
		SharedPreferences sh = contexto.getSharedPreferences("cinesavip", Context.MODE_PRIVATE);
		
		Set<String> ciudadesSet = sh.getStringSet("ciudades", null);
		if(ciudadesSet==null){
			return false;
		}
		ciudades = new ArrayList<String>(ciudadesSet);
		
		Collections.sort(ciudades);
		
		cines = new HashMap<String, List<Cine>>();
		for(String ciudad : ciudades){
			Set<String> cinesCargar = sh.getStringSet(ciudad, null);
			List<Cine> listaCines = new ArrayList<Cine>();
			
			for(String cine : cinesCargar){
				String[] split = cine.split(":");
				String idCine = split[0];
				String nombreCine = split[1];
				Cine c = new Cine(idCine, nombreCine);
				listaCines.add(c);
			}
			
			Collections.sort(listaCines,new Comparator<Cine>(){
				   @Override
				   public int compare(final Cine o1,Cine o2) {				    
					   return o1.getNombre().compareTo(o2.getNombre());
				     }
				 });
			
			cines.put(ciudad, listaCines);			
		}	
		Log.w("cargarCache", "datosCargados");

		return true;
	}
	
	public void getCiudadesYCines() throws IOException{
		
		if(ciudades!=null){			
			return;
		}
		ciudades = new ArrayList<String>();
		cines = new HashMap<String, List<Cine>>();

		Document doc = Jsoup.connect("http://www.cinesa.es/Cines").get();
		Element menuMapa = doc.select("#menu_mapa").first();
		Elements menuCiudades = menuMapa.select("div");
		
	     //Log.w("menuMapa", menuMapa.html());
		menuCiudades.remove(0);
		for(Element e : menuCiudades){
			
		    // Log.w("menuCiudades", e.html());


			Element ciudad = e.select("li").first();
		    // Log.w("ciudad", ciudad.html());

			ciudades.add(ciudad.ownText());
		   // Log.w("ciudad", ciudad.ownText());

			Elements cinesA = e.select("li").get(1).select("li");
			
			List<Cine> lCines = new ArrayList<Cine>();
			cinesA.remove(0);
			for(Element cA : cinesA){
				
				String idC = cA.attr("data-id");
				String nombre = cA.select("a").first().ownText();
				
			    //Log.w("idC", idC);
			   // Log.w("nombre", nombre);
			    
			    if(!idC.equals("")){
			    	Cine ci = new Cine(idC,nombre);
				    lCines.add(ci);	   

			    }

			}
			cines.put(ciudad.ownText(), lCines);

		}


	}

	public List<String> getCiudades() {
		if(ciudades==null){
			try {
				getCiudadesYCines();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ciudades;
	}	

	public HashMap<String, List<Cine>> getCines() {
		if(cines==null){
			try {
				getCiudadesYCines();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return cines;
	}
	
	public List<Pelicula> getPeliculas(String json) throws JSONException{
		 List<Pelicula> lista = new ArrayList<Pelicula>();
		 
		JSONObject obj = new JSONObject(json);
		
		JSONArray arrC = obj.getJSONArray("cartelera");
		for (int i = 0; i < arrC.length(); i++)
		{
			
			JSONArray arrP = arrC.getJSONObject(i).getJSONArray("peliculas");
			for (int o = 0; o < arrP.length(); o++)
			{
			
				 String hora = arrC.getJSONObject(i).getString("hora");
				 lista.add(getPeli(arrP.getJSONObject(o),hora));
			}
		}
			
		

		 
		 return lista;
	 }
	
	private Set<String> getFechasPeli(JSONObject json, String cineA) throws JSONException{
		 Set<String> fechas = new HashSet<String>();
		 
		    JSONArray arrC =json.getJSONArray("cines");
		    if(arrC==null || arrC.length()==0){
				return null;
			}
		    
		    for(int i=0;i<arrC.length();i++){
				JSONObject arro = arrC.getJSONObject(i);	
				
				if(arro==null){
					continue;
				}
				String cine = arro.getString("idCine");
				if(!cine.equals(cineA)){
					continue;
				}
				JSONArray f= arro.getJSONArray("fechas");	
	
				for (int o = 0; o < f.length(); o++)
				{
					fechas.add(f.getJSONObject(o).getString("fecha"));				 
				}
				return fechas;
		    }
			return null;
		 
	 }
	
	private Pelicula getPeli(JSONObject json, String hora) throws NumberFormatException, JSONException{
		 
		   if(json.getBoolean("isevento")){
			   return null;
		   }

		 
		    Pelicula p = new Pelicula();
		    int id = Integer.valueOf(json.getString("idgrupo"));
		    String nombre = json.getString("titulo");
		    String urlImagen = json.getString("imagespath");

		    String directores = json.getString("directores");
		    String actores = json.getString("actores");
		    String genero = json.getString("genero");
		    String duracion = json.getString("duracion");
		    String votos_media = json.getString("votos_media");
		    String votos_num = json.getString("votos_num");
		    String sinopsis = json.getString("sinopsis");
		    String observaciones = json.getString("observaciones");
		    String trailer = json.getString("trailer");
		    
		    
		    if(hora!=null){
			    String ao = json.getString("ao");
			    String perf = new String("performanceCode=");
			    int iniAo = ao.indexOf(perf) + perf.length();
		    
			    int idSesion = Integer.valueOf(ao.substring(iniAo));
			    Sesion s = new Sesion(hora, idSesion);
			    p.setSesionTemporal(s);
		    }
		    
		    
		    p.setId(id);
		    p.setNombre(nombre);
		    
		    p.setUrlImagen(urlImagen);
		    
		    p.setDirectores(directores);
		    p.setActores(actores);
		    p.setGenero(genero);
		    p.setDuracion(duracion);
		    p.setValoracion( Float.parseFloat(votos_media));
		    p.setVotos(Integer.parseInt(votos_num));
		    p.setSinopsis(sinopsis);
		    p.setObservaciones(observaciones.equals("")? null: observaciones);
		    p.setTralier(trailer);
		    
		    return p;
		 
	 }
	 public LinkedHashMap<String, List<Pelicula>> getPeliculasFinal(String json, String cineA) throws JSONException{
		 LinkedHashMap<String, List<Pelicula>> mapa = new LinkedHashMap<String, List<Pelicula>>();
		 
		JSONObject obj = new JSONObject(json);
		
		JSONArray arrC = obj.getJSONArray("peliculas");		
			
		for (int o = 0; o < arrC.length(); o++)		{
		
			Pelicula p = getPeli(arrC.getJSONObject(o),null);
			if(p==null){
				continue;
			}
			Set<String> fechasS = getFechasPeli(arrC.getJSONObject(o), cineA);
			
			if(fechasS == null){
				continue;
			}
			p.setFechas(fechasS);
			
			for(String s : fechasS){
				List<Pelicula> fechasLista = mapa.get(s);
				if(fechasLista==null){
					 fechasLista = new ArrayList<Pelicula>();
					 fechasLista.add(p);
					 mapa.put(s, fechasLista);
				}else{
					fechasLista.add(p);
				}
				
			}			
			
		}
		
		 return mapa;
	 }
	
	public List<Pelicula> getPeliculasCompleja(String json) throws JSONException{
		 List<Pelicula> lista = new ArrayList<Pelicula>();
		 
		JSONObject obj = new JSONObject(json);
		
		JSONArray arrC = obj.getJSONArray("cartelera");
		for (int i = 0; i < arrC.length(); i++)			
		{	
				lista.add(getPeli(arrC.getJSONObject(i),null));			
		}
		
		 return lista;
	 }

	public String[] ordenarFechas(Set<String> ordenar) throws ParseException{
		List<String> fechas = new ArrayList<String>(ordenar);
		final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		List<Date> fechasDate = new ArrayList<Date>(); 
		
		for(String fecha : fechas){
			fechasDate.add(formatter.parse(fecha));
			
		}
		
		Collections.sort(fechasDate, new Comparator<Date>() {
			  public int compare(Date o1, Date o2) {
			      return o1.compareTo(o2);
			  }
			});
		
		fechas = new ArrayList<String>();
		for(Date fecha : fechasDate){
		 	String fechaA= new SimpleDateFormat("dd/MM/yyyy").format(fecha);
		 	fechas.add(fechaA);	 	
		}
	  

		return  fechas.toArray(new String[fechas.size()]);
		
	}
	public String getDiaSemana(String fecha){
		String dia = null;
		final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		try {
			Date fechaDate = formatter.parse(fecha);			
			dia = new SimpleDateFormat("EEEE").format(fechaDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 	
		return dia.substring(0, 1).toUpperCase() + dia.substring(1);
	}
	
	public String getDia(String fecha){		
		int fin = fecha.indexOf("-")-1;
		if(fin==-2){
			return fecha;
		}
		return fecha.substring(0,fin);		
	}
	
	public boolean isDisponibleFecha(String fecha, String fechaActual){
		
		final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy/HH:mm"); 	
		Date horario;
		try {
			horario = formatter.parse(fechaActual +"/" + fecha );

		Calendar c = Calendar.getInstance();
	 	c.setTime(horario);
	 	c.add(Calendar.MINUTE,30);
	 	if(c.get(Calendar.HOUR_OF_DAY)<3){
		 	c.add(Calendar.DAY_OF_MONTH,1);
	 	}
	 	
		return (c.getTime().compareTo(new Date())>=0);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
		
	}

	
	
	public Pelicula getPeliActual() {
		return peliActual;
	}

	public void setPeliActual(Pelicula peliActual) {
		this.peliActual = peliActual;
	}
}
