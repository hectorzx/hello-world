package servicios;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import api.NetworkCinesa;
import api.Preferencias;

public class BloquearAlarma extends BroadcastReceiver {
	public final static int INTERVAL = 1000 * 30 * 1; // 5 minutos
	private final static int INTERVAL_ERROR = 1000 * 1 * 30; // 30 segundos

	@Override
	public void onReceive(Context contexto, Intent intent) {
		// TODO Auto-generated method stub
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contexto);
		
		String codigoCine = prefs.getString("s_codigoCine", "error");
		String codigoSesion = prefs.getString("s_codigoSesion", "error");
		String handler = prefs.getString("s_handler", "error");
		String siteId = prefs.getString("s_siteId", "error");
		String cookie = prefs.getString("s_cookie", "error");

		Set<String> asientos = prefs.getStringSet("s_listaAsientos", null);
		Log.w("BloquearServicio", codigoCine);
		Log.w("BloquearServicio", codigoSesion);
		Log.w("BloquearServicio", String.valueOf(asientos.size()));

		List<Integer> listaAsientos = new ArrayList<Integer>();
		for (String asiento : asientos) {
			listaAsientos.add(Integer.valueOf(asiento));
		}

		try {
			doSomething(codigoCine, codigoSesion, listaAsientos, handler, siteId, cookie);
			//actualizarAlarmaPref(contexto);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//alarmaError(contexto);
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//alarmaError(contexto);
			e.printStackTrace();
		}

	}

	/*private void alarmaError(Context contexto) {
		//Preferencias.guardarAlarmaError(contexto);

		BloquearAlarma alarma = new BloquearAlarma();
		alarma.resetAlarm(contexto, INTERVAL_ERROR);


	}*/

	/*public void resetAlarm(Context contexto, int intervalo) {
		// Cancelo anteriores
		Intent intent = new Intent(contexto, BloquearAlarma.class);
		PendingIntent sender = PendingIntent.getBroadcast(contexto, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
		//

		// Nueva alarma
		AlarmManager am = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(contexto, BloquearAlarma.class);

		PendingIntent pi = PendingIntent.getBroadcast(contexto, 0, i, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalo, pi);
		//
	}*/

	public void setAlarm(Context contexto, String codigoCine, String codigoSesion, List<Integer> listaAsientos,
			int intervalo) {
		AlarmManager am = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(contexto, BloquearAlarma.class);

		NetworkCinesa nC = NetworkCinesa.getInstance();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contexto);
		SharedPreferences.Editor shEditor = prefs.edit();
		shEditor.putString("s_codigoCine", codigoCine);
		shEditor.putString("s_codigoSesion", codigoSesion);
		shEditor.putString("s_handler", nC.getHandleActual());
		shEditor.putString("s_siteId", nC.getSiteIDActual());
		shEditor.putString("s_cookie", nC.getCookieActual());

		Set<String> asientos = new HashSet<String>();
		for (Integer asiento : listaAsientos) {
			asientos.add(String.valueOf(asiento));
		}
		shEditor.putStringSet("s_listaAsientos", asientos);
		shEditor.commit();

		PendingIntent pi = PendingIntent.getBroadcast(contexto, 0, i, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalo, pi);
		
        Preferencias.guardarAlarmaActiva(contexto);
		Log.w("MyApp", "alarma activada");


	}

	public void cancelAlarm(Context contexto) {
		Intent intent = new Intent(contexto, BloquearAlarma.class);
		PendingIntent sender = PendingIntent.getBroadcast(contexto, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
        Preferencias.guardarAlarmaParada(contexto);

		Log.w("MyApp", "alarma Cancelada");

	}

	private void doSomething(String codigoCine, String codigoSesion, List<Integer> listaAsientos, String handler,
			String siteId, String cookie) throws IOException, JSONException {

		String resultado;

		resultado = bloquearAsientos(codigoCine, codigoSesion, listaAsientos, handler, siteId, cookie);
		Log.w("BloquearServicio", resultado);

	}

	private String bloquearAsientos(String codigoCine, String codigoSesion, List<Integer> listaAsientos, String handler,
			String siteId, String cookie) throws IOException, JSONException {
		String br = bloquear(codigoCine, codigoSesion, handler, listaAsientos, siteId, cookie);
		Log.i("bloquearAsientos", br);

		if (br == null) {
			return null;
		}

		JSONObject json = new JSONObject(br);
		String descripcion = json.getJSONObject("SwapSeatsResponse").getString("returnDescription");
		return descripcion;

	}

	private String bloquear(String codigoCine, String codigoSesion, String handle, List<Integer> listaAsientos,
			String siteID, String cookie) throws IOException {

		String url = "http://entradas.cinesa.es/compra/gateway.ashx";

		HashMap<String, String> parametros = new HashMap<String, String>();

		parametros.put("method", "SwapSeats");
		parametros.put("siteA1ID", siteID);
		parametros.put("performanceCode", codigoSesion);
		parametros.put("handle", handle);

		String asientos = new String();
		asientos = TextUtils.join("/", listaAsientos);

		// String asientos = "152/153/154/155/156/157";
		String seats = "{\"seats\":{\"seat\":{\"seatCode\":\"1\",\"seatIdentifiers\":\"" + asientos + "\"}}}";
		parametros.put("seats", seats);

		String resultado = performPostCall(url, parametros, codigoCine, codigoSesion, cookie);
		return resultado;

	}

	public String performPostCall(String requestURL, HashMap<String, String> postDataParams, String codigoCine,
			String codigoSesion, String cookie) throws IOException {

		URL url;
		String response = "";

		url = new URL(requestURL);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(15000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		setHeaders(conn, codigoCine, codigoSesion, cookie);

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

	private void setHeaders(HttpURLConnection conn, String codigoCine, String codigoSesion, String cookie) {
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
		conn.setRequestProperty("Cookie", "ck=OK; _ga=GA1.2.722036704.1442858053; ASP.NET_SessionId=" + cookie);

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

}
