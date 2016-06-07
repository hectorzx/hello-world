package asyn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import api.NetworkCinesa;
import api.Preferencias;
import api.Sala;
import com.cinesavip.R;

public class RellenarSala extends AsyncTask<String, String, Pair<String,String>>{

	
	private Sala salaCine;
	private Activity contexto;
	private String codigoCine,codigoSesion;
	
	public RellenarSala(Activity contexto, Sala salaCine, String codigoCine, String codigoSesion ){
		this.salaCine = salaCine;
		this.contexto = contexto;
		this.codigoCine = codigoCine;
		this.codigoSesion = codigoSesion;
	}
	
	
	@Override
	protected Pair<String,String> doInBackground(String... arg) {
		
		 final NetworkCinesa nC = NetworkCinesa.getInstance();
			
        Pair<String, String> resultado= null;
        
        int estadoAlarma = Preferencias.getEstadoAlarma(contexto);
		try {
			if(estadoAlarma == Preferencias.ALARMA_PARADA){
				resultado = nC.inicializarSala(codigoCine, codigoSesion);
			}else{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contexto);
				Set<String> asientos = prefs.getStringSet("s_listaAsientos", null);				
				
				List<Integer> listaAsientosCargada = new ArrayList<Integer>();
				for (String asiento : asientos) {
					listaAsientosCargada.add(Integer.valueOf(asiento));
				}
				String resultadoTmp = nC.bloquearAsientos(codigoCine, codigoSesion, listaAsientosCargada);
				
				
				JSONObject json = new JSONObject(resultadoTmp);
				String seatMap = json.getJSONObject("SwapSeatsResponse").getJSONObject("reservedSeats").getString("seatMap");				
				
				
				resultado = new Pair<String, String>(seatMap, android.text.TextUtils.join(":", listaAsientosCargada));


			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	   
	
		return resultado;
	}
	
	@Override
	protected void onPostExecute(Pair<String,String> result) 
	{ 
		
		if(result == null){
			salaCine.errorEscena();
			return;
		}
		if(isCancelled()){return;}
		String seatMap = result.first;	
	    salaCine.resetSeleccionados();
		salaCine.setSeatMap(seatMap);
		//salaCine.setAsientoActivo(resultado.second);
        int estadoAlarma = Preferencias.getEstadoAlarma(contexto);

		if(estadoAlarma == Preferencias.ALARMA_PARADA){
			salaCine.clearAsientoActivo(result.second);  	
		}else{
			salaCine.setAsientosActivos(result.second);
			cambiarBloqueo();
		}
  	  			                    	
		salaCine.prepararEscena();
	    LinearLayout pB = (LinearLayout) contexto.findViewById(R.id.layoutCargarSala);	
	    TextView tv = (TextView) contexto.findViewById(R.id.textViewSelAsi);	
	    tv.setVisibility(TextView.VISIBLE);
	    
	    if(pB!=null){
	    	((ViewGroup)pB.getParent()).removeView(pB);
	    }
		Button b = (Button) contexto.findViewById(R.id.buttonBloquear);				
		b.setEnabled(true);
		
    }
	
	private void cambiarBloqueo(){
		Button b = (Button) contexto.findViewById(R.id.buttonBloquear);				
		TextView tvA = (TextView) contexto.findViewById(R.id.textViewAtencion);				
		final ScrollView sv = (ScrollView) contexto.findViewById(R.id.scrollViewSala);				

			
		//Toast.makeText(contexto, "Asientos bloqueados", Toast.LENGTH_SHORT).show();
	  	b.setText("Desbloquear");	
	  	tvA.setVisibility(TextView.VISIBLE);
	  	
	  	sv.post(new Runnable() {            
	  	    @Override
	  	    public void run() {
	  	    	sv.fullScroll(View.FOCUS_DOWN);              
	  	    }
	  	});
		
		//b.setEnabled(true);
		
		
	}

}
