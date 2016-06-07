package api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferencias {

	public final static int ALARMA_PARADA = 0;
	public final static int ALARMA_ACTIVA = 1;
	public final static int ALARMA_ERROR = 2;
	private final static String KEY_ALARMA = "estado_alarma";
	
	
	public static void guardarAlarmaParada(Context contexto){
		guardarAlarma(contexto, ALARMA_PARADA);
	}
	public static void guardarAlarmaActiva(Context contexto){
		guardarAlarma(contexto, ALARMA_ACTIVA);
	}
	public static void guardarAlarmaError(Context contexto){
		guardarAlarma(contexto, ALARMA_ERROR);
	}

	
	public static void guardarAlarma(Context contexto, int estado){
		
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contexto);
        SharedPreferences.Editor shEditor = prefs.edit();		
		shEditor.putInt(KEY_ALARMA,  estado);
		shEditor.commit();		
	}
	
	public static int getEstadoAlarma(Context contexto){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contexto);
        return prefs.getInt(KEY_ALARMA, 0);
	}
	
}
