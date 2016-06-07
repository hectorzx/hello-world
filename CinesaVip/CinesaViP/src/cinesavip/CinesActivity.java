package cinesavip;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.cinesavip.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import api.Cine;
import api.Log;
import api.NetworkCinesa;
import api.Parser;
import api.Pelicula;
import api.Preferencias;
import api.Sesion;
import api.UtilsView;
import publicidad.Publicidad;
import servicios.BloquearAlarma;

public class CinesActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	private String ciudadActual;
	private List<Cine> cinesActuales;
	private String[] nombreCines;
	private Cine cineActual;
	private Parser pC;
	private String fechaActual;
	private String fechaText;
	private LinkedHashMap<String, List<Pelicula>> mapaPeliculas;
	private String[] fechasTodas;
	private Integer indiceCineSel;
	
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cines);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));		
		cancelarAlarma();		
		
		
		
		inicializar();
		
		inicializarPubli();

	}
	
	private void cancelarAlarma(){	
		BloquearAlarma alarma = new BloquearAlarma();
        alarma.cancelAlarm(this);    
        Log.w("MyApp", "onDestroy");

	}
	
	private void inicializarPubli(){
		AdView mAdView = (AdView) findViewById(R.id.adView);
		mAdView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest); 
	}

	private void inicializar() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		pC = Parser.getInstance();

		String value = getIntent().getStringExtra("CIUDAD");
		ciudadActual = value;
		cinesActuales = pC.getCines().get(ciudadActual);
		cineActual = cinesActuales.get(0);
		List<String> listaString = new ArrayList<String>();

		for (Cine c : cinesActuales) {

			listaString.add(c.getNombre());
		}

		nombreCines = listaString.toArray(new String[listaString.size()]);
		mNavigationDrawerFragment.actualizar(nombreCines);
		fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
		fechaText = "Hoy";

		String cineSel = getIntent().getStringExtra("CINE");
		if (cineSel != null) {
			mNavigationDrawerFragment.selectDefault(Integer.valueOf(cineSel) - 1);
		} else {
			mNavigationDrawerFragment.selectDefault();
		}
	}

	private void rellenarPeliculas(final Cine cineActual) {
		rellenarPeliculasComplejaFinal(cineActual);
	}

	private void rellenarPeliculasComplejaFinal(final Cine cineActual) {
		LinearLayout layout = (LinearLayout) findViewById(R.id.peliculasL);

		layout.removeAllViews();

		ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(View.VISIBLE);

		Thread thread = new Thread() {
			@Override
			public void run() {

				NetworkCinesa nC = NetworkCinesa.getInstance();
				try {
					mapaPeliculas = nC.getCarteleraComplejaFinal(String.valueOf(cineActual.getId()));
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								fechasTodas = Parser.getInstance().ordenarFechas(mapaPeliculas.keySet());
							} catch (ParseException e) {
								e.printStackTrace();
							}
							crearEscena();

						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();

	}

	private void crearEscena() {
		crearInterfacePeliculasCompleja(mapaPeliculas.get(fechaActual));

	}

	private void crearInterfaceHorarios(List<Sesion> sesiones, LinearLayout vista, final Pelicula peli) {

		UtilsView uV = UtilsView.getInstance();

		HashMap<String, List<Sesion>> sesionesSala = new HashMap<String, List<Sesion>>();

		// Separamos por tipo
		for (Sesion s : sesiones) {
			List<Sesion> sGM = sesionesSala.get(s.getTipo());
			if (sGM == null) {
				List<Sesion> sSala = new ArrayList<Sesion>();
				sSala.add(s);
				sesionesSala.put(s.getTipo(), sSala);
			} else {
				sGM.add(s);
			}
		}

		vista.addView(uV.crearSeparador(this));

		for (String tipo : sesionesSala.keySet()) {
			List<Sesion> ls = sesionesSala.get(tipo);

			LinearLayout lv = uV.crearLinearLayout(this, LinearLayout.HORIZONTAL, uV.getLpMW(), uV.MARGENCERO);
			vista.addView(lv);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.weight = 0.80f;
			params.gravity = Gravity.CENTER_VERTICAL;

			TextView tipoSala = uV.crearTextView(this, tipo, params, R.style.texto_normal, uV.MARGENCHICO);
			tipoSala.setGravity(Gravity.CENTER_VERTICAL);
			lv.addView(tipoSala);

			params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.weight = 0.20f;
			params.gravity = Gravity.CENTER_VERTICAL;

			LinearLayout ly = uV.crearLinearLayout(this, LinearLayout.HORIZONTAL, params, uV.MARGEN);
			lv.addView(ly);
			List<View> sesionesV = new ArrayList<View>();
			for (final Sesion s : ls) {

				int estilo = R.style.texto_normal;
				int stiloBack = R.drawable.subbotonamarillo;
				boolean isDisponible = Parser.getInstance().isDisponibleFecha(s.getHorario(), fechaActual);
				if (!isDisponible) {
					estilo = R.style.texto_gris;
					stiloBack = R.drawable.subbotonamarillon;
				}
				TextView sesionV = uV.crearTextView(this, s.getHorario(), uV.getLpWW(), estilo, uV.MARGENCHICO);
				sesionV.setBackgroundResource(stiloBack);
				if (isDisponible) {
					sesionV.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(final View v) {

							/*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
							// then you use
							if (prefs.getBoolean("bloquear_asientos", true)) {

								Intent intent = new Intent(v.getContext(), MainActivity.class);
								intent.putExtra("SESION", s.getId());
								intent.putExtra("CINE", cineActual.getId());
								intent.putExtra("TITULO", Parser.getInstance().getDia(fechaText) + " - "
										+ s.getHorario() + "  " + peli.getNombre());

								startActivity(intent);
							} else {
								Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s.getCompra()));
								startActivity(browserIntent);

							}*/
							
							abrirPelicula(peli, s);
						}
					});
				}
				sesionesV.add(sesionV);
			}
			View[] simpleArray = sesionesV.toArray(new View[sesionesV.size()]);
			uV.populateViews(ly, simpleArray, this, null, 0.80f);

			vista.addView(uV.crearSeparador(this));

		}
	}
	
	
	
	
	private void abrirPelicula(Pelicula peli, Sesion s){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// then you use
		if (prefs.getBoolean("bloquear_asientos", true)) {

			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("SESION", s.getId());
			intent.putExtra("CINE", cineActual.getId());
			intent.putExtra("TITULO", Parser.getInstance().getDia(fechaText) + " - "
					+ s.getHorario() + "  " + peli.getNombre());

			startActivity(intent);
		} else {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s.getCompra()));
			startActivity(browserIntent);

		}
	}

	public void onClickHorarios(final View v) {

		final TextView fechaSel = (TextView) findViewById(R.id.fechaSel);

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		String fechaA = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
		c.add(Calendar.DATE, 1);
		String fecha1 = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());

		final String[] items = Arrays.copyOfRange(fechasTodas, 0, fechasTodas.length);
		for (int i = 0; i < items.length; i++) {
			if (items[i].compareTo(fechaA) == 0) {
				items[i] = "Hoy";
			} else if (items[i].compareTo(fecha1) == 0) {
				items[i] = "Mañana";
			} else {
				items[i] = Parser.getInstance().getDiaSemana(items[i]) + " - " + items[i];
			}
		}

		final String[] itemsValue = fechasTodas;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Selecciona una fecha:");
		builder.setItems(items, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int item) {

				fechaActual = itemsValue[item];
				fechaText = items[item];
				fechaSel.setText(items[item]);
				crearEscena();

			}

		});

		AlertDialog alert = builder.create();

		alert.show();
	}

	private void crearInterfacePeliculasCompleja(List<Pelicula> peliculas) {

		ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(View.INVISIBLE);

		UtilsView uV = UtilsView.getInstance();

		LinearLayout layout = (LinearLayout) findViewById(R.id.peliculasL);

		layout.removeAllViews();
		if (peliculas == null) {
			return;
		}

		boolean wifiactivo = NetworkCinesa.getInstance().isWifiActivo(this);

		for (final Pelicula p : peliculas) {

			LinearLayout peliLV = uV.crearLinearLayout(this, LinearLayout.VERTICAL, uV.getLp(), uV.MARGENCERO);

			LinearLayout peliL = uV.crearLinearLayout(this, LinearLayout.HORIZONTAL, uV.getLpMW(), uV.MARGENCERO);

			layout.addView(peliLV);
			peliLV.addView(peliL);

			peliL.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					iniciarActividadPelicula(v.getContext(), p);
				}
			});

			final ImageView imagen = new ImageView(this);
			imagen.setLayoutParams(uV.getlImagen());
			imagen.setScaleType(ScaleType.FIT_XY);
			imagen.setImageResource(R.drawable.nodisponible);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			// then you use
			if (wifiactivo || prefs.getBoolean("descargar_imagenes", true)) {
				Thread thread = new Thread() {
					@Override
					public void run() {
						// final Drawable img = p.getImagen();
						final Drawable img;
						try {
							img = NetworkCinesa.getInstance().cargarImagen(p.getUrlImagen());

							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									imagen.setImageDrawable(img);

								}
							});
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				thread.start();
			}

			peliL.addView(imagen);

			LinearLayout datosL = uV.crearLinearLayout(this, LinearLayout.VERTICAL, uV.getLpMW(), uV.MARGENTITULO);

			TextView titulo = uV.crearTextView(this, p.getNombre(), uV.getLpWW(), R.style.bold);

			TextView director = uV.crearTextView(this, getString(R.string.director), uV.getLpWW(), R.style.bold);
			TextView genero = uV.crearTextView(this, getString(R.string.genero), uV.getLpWW(), R.style.bold);
			TextView duracion = uV.crearTextView(this, getString(R.string.duracion), uV.getLpWW(), R.style.bold);

			TextView directorT = uV.crearTextView(this, p.getDirectores(), uV.getLpWW(), R.style.texto_normal);
			TextView generoT = uV.crearTextView(this, p.getGenero(), uV.getLpWW(), R.style.texto_normal);
			TextView duracionT = uV.crearTextView(this, p.getDuracion() + " minutos", uV.getLpWW(),
					R.style.texto_normal);

			LinearLayout tituloL = uV.crearLinearLayout(this, LinearLayout.HORIZONTAL, uV.getLpWW(), uV.MARGENTITULO);
			LinearLayout directorL = uV.crearLinearLayout(this, LinearLayout.HORIZONTAL, uV.getLpWW(), uV.MARGENCHICO);
			LinearLayout generoL = uV.crearLinearLayout(this, LinearLayout.HORIZONTAL, uV.getLpWW(), uV.MARGENCHICO);
			LinearLayout duracionL = uV.crearLinearLayout(this, LinearLayout.HORIZONTAL, uV.getLpWW(), uV.MARGENCHICO);

			tituloL.addView(titulo);

			directorL.addView(director);
			directorL.addView(directorT);

			generoL.addView(genero);
			generoL.addView(generoT);

			duracionL.addView(duracion);
			duracionL.addView(duracionT);

			datosL.addView(tituloL);
			datosL.addView(directorL);
			datosL.addView(generoL);
			datosL.addView(duracionL);

			peliL.addView(datosL);

			LinearLayout horariosL = uV.crearLinearLayout(this, LinearLayout.VERTICAL, uV.getLpMW(), uV.MARGENTITULO);
			peliLV.addView(horariosL);

			cargarHorariosComplejos(horariosL, p);

		}
	}

	private void cargarHorariosComplejos(final LinearLayout vista, final Pelicula peli) {
		final NetworkCinesa nC = NetworkCinesa.getInstance();

		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					final List<Sesion> sesiones = nC.getSesiones(String.valueOf(cineActual.getId()),
							String.valueOf(peli.getId()), fechaActual);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {							
							crearInterfaceHorarios(sesiones, vista, peli);
								
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		
	}

	private void iniciarActividadPelicula(Context contesto, Pelicula p) {

		Parser.getInstance().setPeliActual(p);
		Intent intent = new Intent(contesto, PeliculaActivity.class);
		intent.putExtra("CINE", String.valueOf(cineActual.getId()));
		intent.putExtra("FECHA", fechaActual);
		startActivity(intent);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
				.commit();
	}

	public void onSectionAttached(int number) {
		cambiarCine(number);
		guardarFavoritos();
	}

	private void cambiarCine(int indice) {
		if (nombreCines != null) {
			indiceCineSel = indice;
			mTitle = nombreCines[indice - 1];
			cineActual = cinesActuales.get(indice - 1);
			rellenarPeliculas(cineActual);
		}
	}

	private void guardarFavoritos() {
		if (indiceCineSel == null || ciudadActual == null) {
			return;
		}
		Parser.getInstance().guardarCacheFavoritos(this, ciudadActual, String.valueOf(indiceCineSel));
		Log.w("guardarCache", "cine y ciudad guardados");

	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.cines, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {

			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_cines, container, false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((CinesActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
		}
	}

}
