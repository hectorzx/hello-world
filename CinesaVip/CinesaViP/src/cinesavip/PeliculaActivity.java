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
import java.util.List;

import com.cinesavip.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import api.NetworkCinesa;
import api.Parser;
import api.Pelicula;
import api.Sesion;
import api.UtilsView;
public class PeliculaActivity extends Activity {

	private String cine;
	private String id ;
	private String fechaActual;
	private String[] fechasTodas;
	private String fechaText;
	private Pelicula peli;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_pelicula);
		/*if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}*/
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		insertarDatos();
	}
	
	private void insertarDatos(){
		
		
		
		cine = getIntent().getStringExtra("CINE");
		
		Parser parser = Parser.getInstance();
		peli = parser.getPeliActual();
		
		id = String.valueOf(peli.getId());
		
		fechaActual = getIntent().getStringExtra("FECHA");
		
		Calendar c = Calendar.getInstance();
	 	c.setTime(new Date());
	 	String fechaA = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
	 	c.add(Calendar.DATE, 1);
	 	String fecha1 = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());	 	
	 	
	 	fechaText = fechaActual;
		if(fechaActual.compareTo(fechaA)==0){
			fechaText="Hoy";
		}else if(fechaActual.compareTo(fecha1)==0){
			fechaText="Mañana";
		}else{
			fechaText = parser.getDiaSemana(fechaActual) + " - " + fechaText;
			
		}
			
		final TextView fechaSel = (TextView) findViewById(R.id.fechaSel);	
		fechaSel.setText(fechaText);

		TextView directoresT = (TextView) findViewById(R.id.directoresT);				
		TextView actoresT = (TextView) findViewById(R.id.actoresT);				
		TextView generoT = (TextView) findViewById(R.id.generoT);				
		TextView duracionT = (TextView) findViewById(R.id.duracionT);	
		TextView tituloPeli = (TextView) findViewById(R.id.tituloPeli);	
		
		TextView num_votos = (TextView) findViewById(R.id.num_valoraciones);	
		RatingBar rating = (RatingBar) findViewById(R.id.ratingBar1);	
		
		TextView sinopsis = (TextView) findViewById(R.id.sinopsisText);	
		TextView observaciones = (TextView) findViewById(R.id.textViewObservaciones);	

		
		
		num_votos.setText("(" + peli.getVotos() + " valoraciones)");
		rating.setRating(peli.getValoracion());
		
		directoresT.setText(peli.getDirectores());
		actoresT.setText(peli.getActores());
		generoT.setText(peli.getGenero());
		duracionT.setText(peli.getDuracion() + " minutos");
		tituloPeli.setText(peli.getNombre());
		sinopsis.setText(peli.getSinopsis());
		if(peli.getObservaciones()!=null){
			observaciones.setVisibility(TextView.VISIBLE);
			observaciones.setText("*" + peli.getObservaciones());
		}
		
		try {
			fechasTodas = parser.ordenarFechas(peli.getFechas());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		final ImageView peliculaImg = (ImageView) findViewById(R.id.peliculaImg);	
		final NetworkCinesa nC = NetworkCinesa.getInstance();		
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		peliculaImg.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
        	Dialog imagenDialog = new Dialog(v.getContext()) {
        		  @Override
        		  public boolean onTouchEvent(MotionEvent event) {
        		    // Tap anywhere to close dialog.
        		    this.dismiss();
        		    return true;
        		  }
        		};	            	
        	imagenDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			final ImageView imagenGrande = new ImageView(v.getContext());
			imagenGrande.setAdjustViewBounds(true);
			imagenGrande.setScaleType(ImageView.ScaleType.FIT_CENTER);

			imagenGrande.setLayoutParams(UtilsView.getInstance().getLpMW());
			
			imagenGrande.setImageDrawable(peliculaImg.getDrawable());
			
			RelativeLayout rl = new RelativeLayout(v.getContext());
			rl.setLayoutParams(UtilsView.getInstance().getLpWW());
			rl.addView(imagenGrande);
			
			imagenDialog.setContentView(rl);
			imagenDialog.show();	
			
			
			final boolean calidad = NetworkCinesa.getInstance().isWifiActivo(v.getContext())
					?true:prefs.getBoolean("descargar_imagen_expandible", true);
			
			descargarImagen(imagenGrande, calidad?NetworkCinesa.CALIDAD_ALTA:NetworkCinesa.CALIDAD_NORMAL);

        }
    });				
		
		
		final int calidad = NetworkCinesa.getInstance().isWifiActivo(this)?NetworkCinesa.CALIDAD_NORMAL:Integer.valueOf(prefs.getString("calidad_grandes", "2"));
		if( calidad > NetworkCinesa.CALIDAD_CERO ){
			descargarImagen(peliculaImg, calidad);
		}	    
		inicializarHorarios(false);
	}		
	
	
	private void inicializarHorarios(final boolean scrollAbajo){
		final NetworkCinesa nC = NetworkCinesa.getInstance();
		final ScrollView scroll = (ScrollView) findViewById(R.id.scrollPelicula);
		
		Thread thread = new Thread() {
	        @Override
	        public void run() {			
				try {
					 final List<Sesion> sesiones = nC.getSesiones(cine, id, fechaActual);
					 
					 runOnUiThread(new Runnable() {
			                @Override
			                public void run() {
			        			mostrarHorarios(sesiones);
			        			if(scrollAbajo){
				        			scroll.post(new Runnable() {            
				        			    @Override
				        			    public void run() {
				        			           scroll.fullScroll(View.FOCUS_DOWN);              
				        			    }
				        			});
			        			}
			                }
			            });
				} catch (Exception  e) {
					e.printStackTrace();
					 runOnUiThread(new Runnable() {
			                @Override
			                public void run() {
			                	errorHorarios();
			                }
			            });
				}	
	        }
		};
	    thread.start();  

	}
	
	
	private void descargarImagen(final ImageView imagenView, final int calidad ){	
		
		Thread thread = new Thread() {
	        @Override
	        public void run() {		        	
	    		final NetworkCinesa nC = NetworkCinesa.getInstance();		

        		final Drawable img;
				try {
					img = nC.cargarImagen(peli.getUrlImagen(), calidad);
					runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                    	imagenView.setImageDrawable(img);		
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
	
	private void errorHorarios(){
		
		LinearLayout salasHorariosL = (LinearLayout) findViewById(R.id.salasHorariosL);	
		salasHorariosL.removeAllViews();
		
		LinearLayout.LayoutParams lpFA = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		LinearLayout.LayoutParams lpSE = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				1);
		
		TextView tT = new TextView(this);
		tT.setText("Horarios");
		tT.setLayoutParams(lpFA);
		tT.setTextAppearance(this, R.style.blanco_text);
		tT.setBackgroundResource(R.drawable.fondoazul);
		salasHorariosL.addView(tT);
		
		tT = new TextView(this);
		tT.setText("Error al cargar los horarios");
		tT.setLayoutParams(lpFA);
		tT.setTextAppearance(this, R.style.texto_error);
		tT.setBackgroundResource(R.drawable.fondo_error);
		salasHorariosL.addView(tT);
		
		View  separador = new View(this);
		separador.setLayoutParams(lpSE);
		separador.setBackgroundColor(Color.GRAY);
		salasHorariosL.addView(separador);
	}
	
	private void mostrarHorarios( List<Sesion> sesiones){
		LinearLayout salasHorariosL = (LinearLayout) findViewById(R.id.salasHorariosL);	
		salasHorariosL.removeAllViews();
		HashMap<String,List<Sesion>> sesionesSala = new HashMap<String, List<Sesion>>();
		
		final Pelicula peli =Parser.getInstance().getPeliActual();
		//Separamos por sala
		for(Sesion s: sesiones){			
			List<Sesion> sGM = sesionesSala.get(s.getSala()+s.getTipo());						
			if((sGM==null) || !(sGM.get(0).getTipo().equals(s.getTipo()))){
				List<Sesion> sSala = new ArrayList<Sesion>();
				sSala.add(s);
				sesionesSala.put(s.getSala()+s.getTipo(), sSala);				
			}else{
				sGM.add(s);				
			}	
		}
		
		LinearLayout.LayoutParams lpFA = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		lp.setMargins(15, 10, 10, 15);
		
		LinearLayout.LayoutParams lbutton = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		for(List<Sesion> lS : sesionesSala.values()){
			
			TextView tT = new TextView(this);
			tT.setText(lS.get(0).getTipo() + " - Sala " + lS.get(0).getSala());
			tT.setLayoutParams(lpFA);
			tT.setTextAppearance(this, R.style.blanco_text);
			tT.setBackgroundResource(R.drawable.fondoazul);
			salasHorariosL.addView(tT);			
			
			LinearLayout ln = new LinearLayout(this);
			ln.setLayoutParams(lp);			
			salasHorariosL.addView(ln);
			
			List<View> vistas = new ArrayList<View>() ;
			
			for(final Sesion s : lS){
				
				TextView tb = new TextView(this);
				tb.setText(s.getHorario());
				tb.setId(s.getId());
				tb.setLayoutParams(lbutton);
				tb.setClickable(true);	
				
				int estilo = R.style.boton_texto;
				int stiloBack = R.drawable.subboton;
				boolean isDisponible = Parser.getInstance().isDisponibleFecha(s.getHorario(),fechaActual);
				if(!isDisponible){
					estilo = R.style.texto_gris;
					stiloBack = R.drawable.subbotonn;
				}
				
				tb.setTextAppearance(this, estilo);				
				
				tb.setBackgroundResource(stiloBack);
				if(isDisponible){
					tb.setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(final View v) {
			            	
			            	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
			        		if(prefs.getBoolean("bloquear_asientos", true)){
			            	
			            	Intent intent = new Intent(v.getContext(), MainActivity.class);
			        		intent.putExtra("SESION", v.getId()); //Put your id to your next Intent
			        		intent.putExtra("CINE", Integer.valueOf(cine)); //Put your id to your next Intent
			        		intent.putExtra("TITULO",  Parser.getInstance().getDia(fechaText) + " - " + s.getHorario() + "  " + peli.getNombre() ); //Put your id to your next Intent

			        		startActivity(intent);
			        		}else{
			        			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s.getCompra()));
			        			startActivity(browserIntent);			        			
			        		}
			            }
			        });
				}
				vistas.add(tb);
				
			}
			View[] simpleArray = vistas.toArray(new View[vistas.size()]);

			UtilsView.getInstance().populateViews(ln,simpleArray,this,null);
		}
	}
	
	 public void onClickHorarios(final View v) {
		 
			final TextView fechaSel = (TextView) findViewById(R.id.fechaSel);	
			final LinearLayout salasHorariosL = (LinearLayout) findViewById(R.id.salasHorariosL);	

		 	Calendar c = Calendar.getInstance();
		 	c.setTime(new Date());
		 	String fechaA = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
		 	c.add(Calendar.DATE, 1);
		 	String fecha1 = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());		 	
		 	
		 	final String[] items =   Arrays.copyOfRange(fechasTodas, 0, fechasTodas.length);			
			
			for(int i=0;i<items.length;i++){				
				if(items[i].compareTo(fechaA)==0){
					items[i]="Hoy";
				}else if(items[i].compareTo(fecha1)==0){
					 items[i]="Mañana";
				}else{
					items[i] = Parser.getInstance().getDiaSemana(items[i]) + " - " + items[i];
				}
			}
			
			final String[] itemsValue = fechasTodas;

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
			builder.setTitle("Selecciona una fecha:");
			builder.setItems(items, new DialogInterface.OnClickListener() {
		
			   public void onClick(DialogInterface dialog, int item) {	
				   
				   salasHorariosL.removeAllViews();
				   fechaActual = itemsValue[item];
				   fechaText = items[item];
				   fechaSel.setText(fechaText);
				   inicializarHorarios(true);
			   }		
			});
			
			AlertDialog alert = builder.create();
			alert.show();
	 }
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pelicula, menu);
		return true;
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
		
		if (id == R.id.action_trailer) {
			Intent intent = new Intent(this, VideoActivity.class);
    		intent.putExtra("URL", peli.getTralier()); //Put your id to your next Intent
    		startActivity(intent);
    		
			return true;
		}
            	        		
            
		
		if(id == android.R.id.home){
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
			View rootView = inflater.inflate(R.layout.fragment_pelicula, container, false);
			return rootView;
		}
	}
}
