package asyn;

import java.io.IOException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import api.NetworkCinesa;
import api.Preferencias;
import api.Sala;

public class ParsearSalaCine extends AsyncTask<String, String, String> {

	private Sala salaCine;
	private Activity contexto;
	private String codigoCine,codigoSesion;
	
	public ParsearSalaCine(Activity contexto, Sala salaCine, String codigoCine, String codigoSesion ){
		this.salaCine = salaCine;
		this.contexto = contexto;
		this.codigoCine = codigoCine;
		this.codigoSesion = codigoSesion;
	}
	
	@Override
	protected String doInBackground(String... arg) {
		// TODO Auto-generated method stub		
		final NetworkCinesa nC = NetworkCinesa.getInstance();
      	String sala=null;
		try {
			sala = nC.obtenerSala(codigoCine, codigoSesion);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sala;
	}
	
	@Override
	protected void onPostExecute(String result) 
	{
		if(result == null){
			salaCine.errorEscena();
			return;
		}
		if(isCancelled()){return;}
		salaCine.parsear(result);	        
		if(isCancelled()){return;}
		RellenarSala rellenarSala = new RellenarSala(contexto, salaCine, codigoCine, codigoSesion);
		rellenarSala.execute();
    }

}
