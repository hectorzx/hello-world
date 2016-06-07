package api;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Pair;

public class NetworkCinesa {

	private String handleActual;
	private String cookieActual;
	private String seatMapActual;
	private String siteIDActual;
	private String seatCode;
	private HashMap<String, Drawable> imagenesCache;

	public static final int CALIDAD_CERO = 0;
	public static final int CALIDAD_BAJA = 1;
	public static final int CALIDAD_NORMAL = 2;
	public static final int CALIDAD_ALTA = 3;

	public static final String IMAGEN_BAJA = "small.jpg";
	public static final String IMAGEN_NORMAL = "cartelera.jpg";
	public static final String IMAGEN_ALTA = "cartelera_full.jpg";

	private static NetworkCinesa instance = null;

	protected NetworkCinesa() {
		// Exists only to defeat instantiation.
		imagenesCache = new HashMap<String, Drawable>();
	}

	public static NetworkCinesa getInstance() {
		if (instance == null) {
			instance = new NetworkCinesa();
		}
		return instance;
	}

	public String obtenerSala(String codigoCine, String codigoSesion) throws IOException {
		HttpURLConnection urlConnection = null;

		URL url = new URL("http://entradas.cinesa.es/compra/?s=" + codigoCine + "&performanceCode=" + codigoSesion);
		urlConnection = (HttpURLConnection) url.openConnection();
		InputStream in = new BufferedInputStream(urlConnection.getInputStream());

		String web = convertStreamToString(in);

		// Log.w("ObtenerSala", web);

		String sala = obtenerSala(web);

		String cookie = urlConnection.getHeaderField("set-cookie");

		String busqueda = "NET_SessionId";
		int inicio = cookie.indexOf(busqueda) + busqueda.length() + 1;
		int fin = cookie.indexOf(";", inicio);

		cookieActual = cookie.substring(inicio, fin);

		String busqueda2 = "var SiteA1ID =";
		int inicio2 = web.indexOf(busqueda2) + busqueda2.length() + 2;
		int fin2 = web.indexOf(";", inicio2) - 1;
		siteIDActual = web.substring(inicio2, fin2);

		String busqueda3 = "data-seatCode";
		int inicio3 = web.indexOf(busqueda3) + busqueda3.length() + 2;
		int fin3 = web.indexOf("\"", inicio3);
		seatCode = web.substring(inicio3, fin3);
		Log.w("seatCode", seatCode);

		urlConnection.disconnect();

		return sala;

	}

	private String obtenerSala(String web) {
		String con = "var datos =";

		int posI = web.indexOf(con) + con.length() + 2;
		int posF = web.indexOf(";", posI) - 2;
		return web.substring(posI, posF);
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private void setHeaders(HttpURLConnection conn, String codigoCine, String codigoSesion) {
		// private void setHeaders(HttpURLConnection conn){
		conn.setRequestProperty("Host", "entradas.cinesa.es");
		conn.setRequestProperty("Origin", "http://entradas.cinesa.es");
		conn.setRequestProperty("Referer",
				"http://entradas.cinesa.es/compra/?s=" + codigoCine + "&performanceCode=" + codigoSesion);
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.setRequestProperty("Accept-Language", "es-ES,es;q=0.8");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		// conn.setRequestProperty("Content-Length","161");
		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
		// conn.setRequestProperty("Cookie","ck=OK;
		// _ga=GA1.2.722036704.1442858053;
		// ASP.NET_SessionId=eowcqo3fxohfaps0zylpy4sv");
		conn.setRequestProperty("Cookie", "ck=OK; _ga=GA1.2.722036704.1442858053; ASP.NET_SessionId=" + cookieActual);

	}

	private String obtenerAsientosDisponibles(String codigoCine, String codigoSesion) throws IOException {

		// cookieActual = obtenerCookie(codigoCine,codigoSesion);

		String url = "http://entradas.cinesa.es/compra/gateway.ashx";

		HashMap<String, String> parametros = new HashMap<String, String>();

		parametros.put("method", "GetBestSeats");
		parametros.put("siteA1ID", siteIDActual);
		parametros.put("performanceCode", codigoSesion);

		String seats = "{\"seats\":{\"seat\":[{\"seatCode\":\"" + seatCode + "\",\"number\":1}]}";
		parametros.put("seats", seats);

		String respuesta = performPostCall(url, parametros, codigoCine, codigoSesion);

		// Log.w("obtenerAsientosDisponibles", respuesta);

		Log.i("GetBestSeats", respuesta);
		return respuesta;
	}

	public Pair<String, String> inicializarSala(String codigoCine, String codigoSesion)
			throws JSONException, IOException {
		String json = obtenerAsientosDisponibles(codigoCine, codigoSesion);
		// Log.i("json", json);

		String asientoActual = null;

		JSONObject obj = new JSONObject(json);
		handleActual = obj.getJSONObject("GetBestSeatsResponse").getJSONObject("reservedSeats").getString("handle");
		seatMapActual = obj.getJSONObject("GetBestSeatsResponse").getJSONObject("reservedSeats").getString("seatMap");
		asientoActual = obj.getJSONObject("GetBestSeatsResponse").getJSONObject("reservedSeats").getString("allocated");
		asientoActual = asientoActual.substring(0, asientoActual.indexOf(":"));

		return new Pair<String, String>(seatMapActual, asientoActual);
	}

	private String actualizarSala(String json) throws JSONException {

		JSONObject obj = new JSONObject(json);
		Log.i("json", json);
		handleActual = obj.getJSONObject("SwapSeatsResponse").getJSONObject("reservedSeats").getString("handle");
		seatMapActual = obj.getJSONObject("SwapSeatsResponse").getJSONObject("reservedSeats").getString("seatMap");

		return seatMapActual;

	}

	public String bloquearAsientos(String codigoCine, String codigoSesion, List<Integer> listaAsientos)
			throws IOException {
		String br = bloquear(codigoCine, codigoSesion, handleActual, listaAsientos);
		Log.i("bloquearAsientos", br);

		/*if (br == null) {
			return null;
		}
		try {
			JSONObject json = new JSONObject(br);
			String descripcion = json.getJSONObject("SwapSeatsResponse").getString("returnDescription");
			return descripcion;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}*/
		
		return br;

	}

	public String desBloquearAsientos(String codigoCine, String codigoSesion) throws JSONException, IOException {
		List<Integer> asientosVacios = new ArrayList<Integer>();
		asientosVacios.add(-1);
		String br = bloquear(codigoCine, codigoSesion, handleActual, asientosVacios);
		Log.i("desBloquearAsientos", br);

		return actualizarSala(br);
	}

	private String bloquear(String codigoCine, String codigoSesion, String handle, List<Integer> listaAsientos)
			throws IOException {

		String url = "http://entradas.cinesa.es/compra/gateway.ashx";

		HashMap<String, String> parametros = new HashMap<String, String>();

		parametros.put("method", "SwapSeats");
		parametros.put("siteA1ID", siteIDActual);
		parametros.put("performanceCode", codigoSesion);
		parametros.put("handle", handle);

		String asientos = new String();
		asientos = TextUtils.join("/", listaAsientos);

		// String asientos = "152/153/154/155/156/157";
		String seats = "{\"seats\":{\"seat\":{\"seatCode\":\"1\",\"seatIdentifiers\":\"" + asientos + "\"}}}";
		parametros.put("seats", seats);

		String resultado = performPostCall(url, parametros, codigoCine, codigoSesion);
		return resultado;

	}

	public String performPostCall(String requestURL, HashMap<String, String> postDataParams, String codigoCine,
			String codigoSesion) throws IOException {

		URL url;
		String response = "";

		url = new URL(requestURL);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(15000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		setHeaders(conn, codigoCine, codigoSesion);

		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		writer.write(getPostDataString(postDataParams));

		writer.flush();
		writer.close();
		os.close();
		int responseCode = conn.getResponseCode();

		if (responseCode == HttpsURLConnection.HTTP_OK) {
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = br.readLine()) != null) {
				response += line;
			}
		} else {
			response = "";

		}

		return response;
	}

	private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	public List<Sesion> getSesiones(String codigoCine, String codigoPelicula, String fecha)
			throws IOException, JSONException {

		HttpURLConnection conn = null;
		String response = null;

		URL url = new URL("http://www.cinesa.es/MovilCWV3/?accion=horarios&cine=" + codigoCine + "&fecha=" + fecha
				+ "&pelicula=" + codigoPelicula + "&ex=0");

		conn = (HttpURLConnection) url.openConnection();

		int responseCode = conn.getResponseCode();

		if (responseCode == HttpsURLConnection.HTTP_OK) {
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = br.readLine()) != null) {
				response += line;
			}
		} else {
			response = "";

		}
		// Log.w("getSesiones", response);

		return parsearSesiones(response);
	}

	private List<Sesion> parsearSesiones(String json) throws JSONException {

		List<Sesion> sesiones = new ArrayList<Sesion>();
		String origen = json.startsWith("null") ? json.substring(4) : json;

		JSONObject obj = new JSONObject(origen);

		JSONObject pelicula = obj.getJSONArray("horarios").getJSONObject(0);

		JSONArray horarios = pelicula.getJSONArray("se");

		for (int o = 0; o < horarios.length(); o++) {

			JSONObject horario = horarios.getJSONObject(o);
			String hora = horario.getString("hora");
			String sala = horario.getString("sala");
			String tipo = horario.getString("tipo");

			String compra = horario.getString("ao");

			String perf = new String("performanceCode=");
			int iniAo = compra.indexOf(perf) + perf.length();
			int id = Integer.valueOf(compra.substring(iniAo));

			Sesion s = new Sesion(hora, id, tipo, sala, compra);
			sesiones.add(s);
		}

		return sesiones;

	}

	public String getCartelera(String codigoCine, String sesion) throws IOException, JSONException {
		HttpURLConnection conn = null;
		String response = null;

		URL url = new URL("http://www.cinesa.es/MovilCWV3/?accion=cartelera&cine=" + codigoCine + "&sesion=" + sesion);

		conn = (HttpURLConnection) url.openConnection();

		int responseCode = conn.getResponseCode();

		if (responseCode == HttpsURLConnection.HTTP_OK) {
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = br.readLine()) != null) {
				response += line;
			}
		} else {
			response = "";

		}
		return response;
	}	

	public String getCarteleraFinal(String codigoCine) throws IOException, JSONException {
		HttpURLConnection conn = null;
		String response = null;

		URL url = new URL("http://www.cinesa.es/MovilCWV3/?accion=peliculas&cine=" + codigoCine);

		conn = (HttpURLConnection) url.openConnection();

		int responseCode = conn.getResponseCode();

		if (responseCode == HttpsURLConnection.HTTP_OK) {
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = br.readLine()) != null) {
				response += line;
			}
		} else {
			response = "";

		}
		return response;
	}

	public LinkedHashMap<String, List<Pelicula>> getCarteleraComplejaFinal(String codigoCine)
			throws IOException, JSONException {
		String result = getCarteleraFinal(codigoCine);

		return Parser.getInstance().getPeliculasFinal(result.substring(4), codigoCine);
	}
	

	public Drawable cargarImagen(String url) throws MalformedURLException, IOException {
		String tipo = IMAGEN_BAJA;

		return cargarImagen(url, tipo);
	}

	public Drawable cargarImagen(String url, int calidad) throws MalformedURLException, IOException {

		int calidadR = calidad;

		String tipo = IMAGEN_NORMAL;
		switch (calidadR) {
		case CALIDAD_BAJA:
			tipo = IMAGEN_BAJA;
			break;
		case CALIDAD_NORMAL:
			tipo = IMAGEN_NORMAL;
			break;
		case CALIDAD_ALTA:
			tipo = IMAGEN_ALTA;
			break;
		default:
			break;
		}
		return cargarImagen(url, tipo);

	}

	public Drawable cargarImagen(String url, String tipo) throws MalformedURLException, IOException {
		Drawable imgTmp = imagenesCache.get(url + "/" + tipo);
		if (imgTmp != null) {
			Log.w("cacheImagen", "imagen obtenida de cache");
			return imgTmp;
		}

		InputStream is = (InputStream) new URL("http://www.cinesa.es/" + url + "/" + tipo).getContent();
		Drawable d = Drawable.createFromStream(is, "src name");
		imagenesCache.put(url + "/" + tipo, d);
		return d;

	}

	public boolean isWifiActivo(Context contesto) {
		ConnectivityManager connManager = (ConnectivityManager) contesto
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		return mWifi.isConnected();

	}

	public String getHandleActual() {
		return handleActual;
	}

	public String getCookieActual() {
		return cookieActual;
	}

	public String getSiteIDActual() {
		return siteIDActual;
	}

	public void setHandleActual(String handleActual) {
		this.handleActual = handleActual;
	}

	public void setCookieActual(String cookieActual) {
		this.cookieActual = cookieActual;
	}

	public void setSiteIDActual(String siteIDActual) {
		this.siteIDActual = siteIDActual;
	}

}
