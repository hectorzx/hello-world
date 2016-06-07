package asyn;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import api.NetworkCinesa;
import api.Sala;
import servicios.BloquearAlarma;

import com.cinesavip.R;

public class Bloquear extends AsyncTask<String, String, String>{
	private Activity contexto;
	private String codigoCine,codigoSesion;
	//private BloquearServ bloqueo;
	private List<Integer> listaAsientos;
	private Sala salaCine;

	public Bloquear(Activity contexto, Sala salaCine,String codigoCine, String codigoSesion, List<Integer> listaAsientos){
		this.salaCine = salaCine;
		//this.bloqueo = bloqueo;
		this.contexto = contexto;
		this.codigoCine = codigoCine;
		this.codigoSesion = codigoSesion;
		this.listaAsientos = listaAsientos;			

	}

	@Override
	protected String doInBackground(String... params) {
		
		NetworkCinesa nC = NetworkCinesa.getInstance();
		String resultado=null;
		
		try {
			String resultadoB = nC.bloquearAsientos(codigoCine, codigoSesion, listaAsientos);
			
			try {
				JSONObject json = new JSONObject(resultadoB);
				String descripcion = json.getJSONObject("SwapSeatsResponse").getString("returnDescription");
				return descripcion;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}	
		
		return resultado;
	}
	
	@Override
	protected void onPostExecute(String resultado) {
		
		if(resultado != null && resultado.equals("")){
			cambiarBloqueo();
			BloquearAlarma alarma = new BloquearAlarma();
			alarma.setAlarm(contexto, codigoCine, codigoSesion, listaAsientos,BloquearAlarma.INTERVAL);
			//bloqueo.startRepeatingTask();
		}else{
			//b.setEnabled(true);
			Toast.makeText(contexto, "Error al bloquear los asientos: " + resultado, Toast.LENGTH_SHORT).show();
			Desbloquear desbloquear = new Desbloquear(contexto, salaCine, codigoCine, codigoSesion, false);
			desbloquear.execute();
		}
    	  
	}	
	private void cambiarBloqueo(){
		Button b = (Button) contexto.findViewById(R.id.buttonBloquear);				
		TextView tvA = (TextView) contexto.findViewById(R.id.textViewAtencion);				
		final ScrollView sv = (ScrollView) contexto.findViewById(R.id.scrollViewSala);				

			
		Toast.makeText(contexto, "Asientos bloqueados", Toast.LENGTH_SHORT).show();
	  	b.setText("Desbloquear");	
	  	tvA.setVisibility(TextView.VISIBLE);
	  	
	  	sv.post(new Runnable() {            
	  	    @Override
	  	    public void run() {
	  	    	sv.fullScroll(View.FOCUS_DOWN);              
	  	    }
	  	});
		
		b.setEnabled(true);
		
		
	}
}
