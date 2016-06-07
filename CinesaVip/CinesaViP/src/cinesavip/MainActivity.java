package cinesavip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.Toast;
import api.Log;
import api.NetworkCinesa;
import api.Parser;
import api.Preferencias;
import api.Sala;
import asyn.Bloquear;
import asyn.Desbloquear;
import asyn.ParsearSalaCine;
import publicidad.Publicidad;
import servicios.BloquearAlarma;

import com.cinesavip.R;
public class MainActivity extends Activity {

	Sala salaCine;
	String codigoCine = "1033";
	String codigoSesion = "32139";
	int sesion;
	int codCine;
	


	private void prueba() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		sesion = getIntent().getIntExtra("SESION", -1);
		codCine = getIntent().getIntExtra("CINE", -1);

		if (sesion != -1) {
			codigoSesion = String.valueOf(sesion);
			codigoCine = String.valueOf(codCine);
		}
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.rl);
		TableLayout glayout = (TableLayout) findViewById(R.id.tl);
		salaCine = new Sala(layout, glayout, this);

		String titulo = getIntent().getStringExtra("TITULO");
		if (titulo != null) {
			setTitle(titulo);
		}
		
        int estadoAlarma = Preferencias.getEstadoAlarma(this);
		if(estadoAlarma != Preferencias.ALARMA_PARADA){
			cargarPubli();
			recargarContexto();
		}
		
		obtenersala();

	}
	
	private void cargarPubli(){
		Publicidad publi = Publicidad.getInstance(this);
		//publi.requestNewInterstitial();
		publi.mostrarPubli();
	}
	
	private void recargarContexto(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String handle = prefs.getString("s_handler", null);
		String siteID = prefs.getString("s_siteId", null);
		String cookie = prefs.getString("s_cookie", null);

		NetworkCinesa nC = NetworkCinesa.getInstance();
		nC.setCookieActual(cookie);
		nC.setHandleActual(handle);
		nC.setSiteIDActual(siteID);
		
		Log.w("BloquearServicio", "contexto cargado");

	}

	private void obtenersala() {

		ParsearSalaCine thread = new ParsearSalaCine(this, salaCine, codigoCine, codigoSesion);
		thread.execute();
	}

	public void onClickBloquear(View v) {
		// Button bV = (Button) v;
		Button b = (Button) findViewById(R.id.buttonBloquear);
		b.setEnabled(false);
		if (b.getText().equals("Desbloquear")) {
			desbloquear();
		} else {
			if (salaCine.getSeleccionados().size() == 0) {
				Toast.makeText(this, "Debes seleccionar al menos un asiento", Toast.LENGTH_SHORT).show();
				b.setEnabled(true);
			} else {
				//bloquearServicio();
				bloquear();
				//bloquearAlarma();
			}
		}
	}
	
	/*private void bloquearAlarma(){		
		
		BloquearAlarma alarma = new BloquearAlarma();	
		alarma.setAlarm(this, codigoCine, codigoSesion, salaCine.getSeleccionados(),BloquearAlarma.INTERVAL);
	}*/
	
	 

	/*private void bloquearServicio() {
		servicio = BloquearServicio.getInstance();
		servicio.inicializarDatos(codigoCine, codigoSesion, salaCine.getSeleccionados());

		stopService(new Intent(this, BloquearServicio.class));
		startService(new Intent(this, BloquearServicio.class));
		
		
	}*/

	
	private void bloquear() {

		final Bloquear bloquear = new Bloquear(this, salaCine, codigoCine, codigoSesion,
				salaCine.getSeleccionados());

		bloquear.execute();

	}

	private void desbloquear() {
		desbloquear(true);
	}

	private void desbloquear(final boolean msg) {
		
		cancelarAlarma();
		
		Desbloquear desbloquear = new Desbloquear(this, salaCine, codigoCine, codigoSesion, msg);
		desbloquear.execute();

	}

	public void onClickPruebas(View v) {

		Parser pC = Parser.getInstance();
		try {
			pC.getCiudadesYCines();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		desbloquear(false);
		// finalizarTodo();
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		if (salaCine == null) {
			prueba();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void comprar() {
		String urlCompra = "http://entradas.cinesa.es/compra/?s=" + codCine + "&performanceCode=" + sesion;
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlCompra));
		startActivity(browserIntent);
	}

	private void mostrarAyuda() {
		new AlertDialog.Builder(this).setTitle("Ayuda")
				.setMessage("Bloquea los asientos para que nadie pueda comprarlos.\n\nBy Hectorzx").show();

	}
	
	@Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
        //alarma = new BloquearAlarma();
        //desbloquear();

    }
	
	private void cancelarAlarma(){
		BloquearAlarma alarma = new BloquearAlarma();
        alarma.cancelAlarm(this);     
        Log.w("MyApp", "onDestroy");

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_comprar) {
			comprar();
			return true;
		}
		if (id == R.id.action_ayuda) {
			mostrarAyuda();
			return true;
		}
		if (id == android.R.id.home) {

			desbloquear(false);
			// finalizarTodo();
			this.finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}

}
