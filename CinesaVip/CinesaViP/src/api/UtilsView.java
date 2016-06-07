package api;

import android.app.Activity;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UtilsView {
	private static UtilsView instance = null;
	public final int[] MARGENCERO = {0,0,0,0};	//izquierda,arria,deracha,abajo
	public final int[] MARGEN = {5,5,5,5};	//izquierda,arria,deracha,abajo
	public final int[] MARGENGRANDE = {15,8,15,8};	//izquierda,arria,deracha,abajo
	public final int[] MARGENIMAGENGRANDE = {150,150,150,150};	//izquierda,arria,deracha,abajo

	public final int[] MARGENTITULO = {5,0,5,3};	
	public final int[] MARGENCHICO = {5,1,5,1};	
	
	public final int[] PADDINGCERO = {0,0,0,0};	//izquierda,arria,deracha,abajo
	public final int[] PADDING = {5,5,5,5};	//izquierda,arria,deracha,abajo
	public final int[] PADDINGTITULO = {5,0,5,0};	
	public final int[] PADDINGCHICO = {5,0,5,0};	

	private LinearLayout.LayoutParams lpWW;
	private LinearLayout.LayoutParams lpFW;
	private LinearLayout.LayoutParams lpMW;
	private LinearLayout.LayoutParams lp;
	private LinearLayout.LayoutParams lpFA;
	private LinearLayout.LayoutParams lImagen;
	private LinearLayout.LayoutParams lbutton;
	private LinearLayout.LayoutParams lpSE;
	private LinearLayout.LayoutParams lImagenGrande;

	protected UtilsView() { 
		inicializarLayoutParams();		
	 }
	
	private void inicializarLayoutParams(){		
		lpWW = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		lpMW = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		lpFW = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);		
		
		
		lImagenGrande = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		
		//lImagenGrande.setMargins(MARGENIMAGENGRANDE[0], MARGENIMAGENGRANDE[1], MARGENIMAGENGRANDE[2], MARGENIMAGENGRANDE[3]);

		lp.setMargins(MARGENGRANDE[0], MARGENGRANDE[1], MARGENGRANDE[2], MARGENGRANDE[3]);
		
		lpFA = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		
		
		lpSE = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				1);
		lImagen = new LinearLayout.LayoutParams(
					165,
					240);
			
		/*lImagen = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lImagen.height = 160;
		lImagen.width = 110;*/
		
		
		lbutton = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		lbutton.setMargins(MARGEN[0], MARGEN[1], MARGEN[2], MARGEN[3]);	
	}	
	   
	public static UtilsView getInstance() {
	    if(instance == null) {
	       instance = new UtilsView();
	    }
    return instance;
	}	

	
	/**
	 * Copyright 2011 Sherif 
	 * Updated by Karim Varela to handle LinearLayouts with other views on either side.
	 * @param linearLayout
	 * @param views : The views to wrap within LinearLayout
	 * @param context
	 * @param extraView : An extra view that may be to the right or left of your LinearLayout.
	 * @author Karim Varela
	 **/
	public void populateViews(LinearLayout linearLayout, View[] views, Activity context, View extraView){
			populateViews(linearLayout, views,  context, extraView, 0);
	}
	public void populateViews(LinearLayout linearLayout, View[] views, Activity context, View extraView, float porc)
	{
	    //extraView.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

	    linearLayout.removeAllViews();
	    
	    
	    DisplayMetrics displaymetrics = new DisplayMetrics();
	    context.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
	    int maxWidth = displaymetrics.widthPixels ;
	    if(porc!=0){
	    	maxWidth *= porc;
	    }
	    maxWidth -= 20;
	    		
	    linearLayout.setOrientation(LinearLayout.VERTICAL);

	    LinearLayout.LayoutParams params;
	    LinearLayout newLL = new LinearLayout(context);
	    newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
	    newLL.setGravity(Gravity.LEFT);
	    newLL.setOrientation(LinearLayout.HORIZONTAL);

	    int widthSoFar = 0;

	    for (int i = 0; i < views.length; i++)
	    {
	        LinearLayout LL = new LinearLayout(context);
	        LL.setOrientation(LinearLayout.HORIZONTAL);
	        LL.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
	        LL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

	        views[i].measure(0, 0);
	        params = new LinearLayout.LayoutParams(views[i].getMeasuredWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
	        params.setMargins(MARGEN[0], MARGEN[1], MARGEN[2], MARGEN[3]);	
	        
	        LL.addView(views[i], params);

	        LL.measure(0, 0);
	        widthSoFar += views[i].getMeasuredWidth();
	        if (widthSoFar + views[i].getMeasuredWidth()/2 >= maxWidth)
	        {
	            linearLayout.addView(newLL);

	            newLL = new LinearLayout(context);
	            newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
	            newLL.setOrientation(LinearLayout.HORIZONTAL);
	            newLL.setGravity(Gravity.LEFT);
	            params = new LinearLayout.LayoutParams(LL.getMeasuredWidth(), LL.getMeasuredHeight());
	            newLL.addView(LL, params);
	            widthSoFar = LL.getMeasuredWidth();
	        }
	        else
	        {
	            newLL.addView(LL);
	        }
	    }
	    linearLayout.addView(newLL);
	}	
	
	public LinearLayout crearLinearLayout(Activity context, int orientacion ,LinearLayout.LayoutParams lp, int[] pading){
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(orientacion);
		layout.setBackgroundColor(Color.WHITE);
		layout.setPadding(pading[0], pading[1], pading[2], pading[3]);
		layout.setLayoutParams(lp);		
		return layout;
		
	}
	public TextView crearTextView(Activity context, String texto, LinearLayout.LayoutParams lp, int estilo){			
		return crearTextView(context, texto, lp, estilo, PADDINGCHICO);
	}
	
	public TextView crearTextView(Activity context, String texto, LinearLayout.LayoutParams lp, int estilo, int[] margen){			
		lp.setMargins(margen[0], margen[1], margen[2], margen[3]);
		TextView tv = new TextView(context);
		tv.setText(texto);
		tv.setLayoutParams(lp);		
		tv.setTextAppearance(context, estilo);	
		//tv.setBackgroundResource(R.drawable.fondoazul);	

		return tv;
	}
	public View crearSeparador(Activity context){
		View  separador = new View(context);
		separador.setLayoutParams(lpSE);
		separador.setBackgroundColor(Color.GRAY);
		return separador;
	}

	public LinearLayout.LayoutParams getLp() {
		return lp;
	}

	public LinearLayout.LayoutParams getLpFA() {
		return lpFA;
	}

	public LinearLayout.LayoutParams getlImagen() {
		return lImagen;
	}

	public LinearLayout.LayoutParams getLbutton() {
		return lbutton;
	}

	public LinearLayout.LayoutParams getLpWW() {
		return lpWW;
	}

	public LinearLayout.LayoutParams getLpMW() {
		return lpMW;
	}

	public LinearLayout.LayoutParams getLpSE() {
		return lpSE;
	}

	public LinearLayout.LayoutParams getLpFW() {
		return lpFW;
	}

	public int[] getMARGENIMAGENGRANDE() {
		return MARGENIMAGENGRANDE;
	}

	public LinearLayout.LayoutParams getlImagenGrande() {
		return lImagenGrande;
	}
}
