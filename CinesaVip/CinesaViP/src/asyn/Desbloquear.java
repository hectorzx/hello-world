package asyn;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import api.NetworkCinesa;
import api.Sala;
import com.cinesavip.R;

public class Desbloquear extends AsyncTask<String, String, String> {
	
	private Sala salaCine;
	private Activity contexto;
	private String codigoCine,codigoSesion;
	private boolean msg;
	
	public Desbloquear(Activity contexto, Sala salaCine, String codigoCine, String codigoSesion, boolean msg ){
		this.salaCine = salaCine;
		this.contexto = contexto;
		this.codigoCine = codigoCine;
		this.codigoSesion = codigoSesion;
		this.msg = msg;
	}

	@Override
	protected String doInBackground(String... params) {
		final NetworkCinesa nC = NetworkCinesa.getInstance();
  	  		String seatMap = null;
			try {
				seatMap = nC.desBloquearAsientos(codigoCine, codigoSesion);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		return seatMap;
	}
	
	@Override
	protected void onPostExecute(String seatMap) {
		Button b = (Button) contexto.findViewById(R.id.buttonBloquear);				
		b.setEnabled(true);

		salaCine.resetSeleccionados();
		if(seatMap==null){
			salaCine.errorEscena();
			return;
		}
		salaCine.setSeatMap(seatMap);	
	    salaCine.prepararEscena();

		if(msg){
    		cambiarBloqueo();	
    	}   
	}	
	
	
	private void cambiarBloqueo(){
		Button b = (Button) contexto.findViewById(R.id.buttonBloquear);				
		TextView tvA = (TextView) contexto.findViewById(R.id.textViewAtencion);				

		Toast.makeText(contexto, "Asientos Desbloqueados", Toast.LENGTH_SHORT).show();
	  	b.setText("Bloquear");		  	
	  	tvA.setVisibility(TextView.GONE);
		
		
	}
}
