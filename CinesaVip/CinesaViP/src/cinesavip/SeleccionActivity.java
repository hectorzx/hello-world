package cinesavip;

import java.io.IOException;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;
import api.Log;
import api.Parser;
import com.cinesavip.R;
public class SeleccionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_seleccion);
		/*
		 * if (savedInstanceState == null) {
		 * getFragmentManager().beginTransaction().add(R.id.container, new
		 * PlaceholderFragment()).commit(); }
		 */

		inicializar();

	}

	private void cargarCache() {

		Parser parser = Parser.getInstance();
		String ciudad = parser.cargarCacheCiudadFavoritos(this);
		String cine = parser.cargarCacheCineFavoritos(this);
		Log.w("cargarCache", "cargando datos");

		if (ciudad == null || cine == null) {
			return;
		}

		Intent intent = new Intent(this, CinesActivity.class);

		intent.putExtra("CIUDAD", ciudad); // Put your id to your next Intent
		intent.putExtra("CINE", cine); // Put your id to your next Intent
		Log.w("cargarCache", "cine y ciudad cargados");
		startActivity(intent);

	}

	private void inicializar() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		final Parser parser = Parser.getInstance();

		if (parser.cargarCache(this) == true) {
			initMenu();
			cargarCache();
			return;
		}
		final Context contesto = this;

		Thread thread = new Thread() {
			@Override
			public void run() {
				cargarCines();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						initMenu();
						parser.guardarCache(contesto);
					}
				});
			}
		};
		thread.start();
	}

	private void cargarCines() {
		final Parser pC = Parser.getInstance();

		try {
			pC.getCiudadesYCines();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initMenu() {

		final Parser pC = Parser.getInstance();

		ProgressBar pb = (ProgressBar) findViewById(R.id.progressBarSel);
		((ViewGroup) pb.getParent()).removeView(pb);

		ScrollView sV = (ScrollView) findViewById(R.id.scrollViewSel);
		sV.setFillViewport(false);

		RadioGroup group = (RadioGroup) findViewById(R.id.groupRadioButtons);

		for (String ciudad : pC.getCiudades()) {
			RadioButton boton = new RadioButton(this);
			boton.setText(ciudad);
			group.addView(boton);
		}
	}

	public void onClickAceptar(View v) {
		RadioGroup group = (RadioGroup) findViewById(R.id.groupRadioButtons);

		int radioButtonID = group.getCheckedRadioButtonId();
		if (radioButtonID == -1) {
			Toast.makeText(this, "Selecciona tu ciudad", Toast.LENGTH_SHORT).show();
			return;
		}

		RadioButton radioButton = (RadioButton) group.findViewById(radioButtonID);

		Intent intent = new Intent(this, CinesActivity.class);
		intent.putExtra("CIUDAD", radioButton.getText().toString()); 
		startActivity(intent);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_seleccion, container, false);
			return rootView;
		}
	}
}
