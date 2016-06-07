package api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cinesavip.R;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class Sala {
	private Butaca[][] butacas;
	private static final int size = 40;
	private int maxcol;
	private int maxrow;
	private List<Integer> seleccionados;
	private HashMap<Integer, Butaca> mapaButacas;
	//private HashMap<Integer, TextView> mapaButacasView;

	private TableLayout glayout;
	private Activity contesto;
	
	private final int maximosAsientosBloqueados = 6;
	
	public Sala(RelativeLayout layout,TableLayout glayout, Activity contesto){
		butacas = new Butaca[size][size];
		this.glayout = glayout;
		this.contesto = contesto;
		
		seleccionados = new ArrayList<Integer>();
		
	}
	
	
	
	public void parsear(String datos){
		
		mapaButacas = new HashMap<Integer, Butaca>();

		String[] preproc = datos.split("\\~");		
		maxcol = 0;
		maxrow = 0;
		
		for( String butaca : preproc){
			String[] butacaA = butaca.split("\\|");
			
			Butaca b = new Butaca();		
			
			int row = Integer.parseInt(butacaA[3]);
			int col = Integer.parseInt(butacaA[4]);
			
			b.setCol(row);	//codigo
			b.setRow(col);	//codigo			
			
					
			b.setIdbutaca( Integer.parseInt(butacaA[0]));
			b.setVip((Integer.parseInt(butacaA[1])==4)?true:false);
			b.setCodigoseccion( Integer.parseInt(butacaA[2]));
			b.setFila( Integer.parseInt(butacaA[5]));	//sala errorrrrrrrrrrrrrrr
			b.setColumna( Integer.parseInt(butacaA[6]));	//sala
			b.setAtributos( Integer.parseInt(butacaA[7]));
			
			butacas[row][col] = b;
		
			if(row>maxrow){
				maxrow = row;
			}
			if(col>maxcol){
				maxcol = col;
			}			
			mapaButacas.put(Integer.valueOf(b.getIdbutaca()), b);

		}
		
		
	}
	
	
	public void resetSeleccionados(){
		seleccionados = new ArrayList<Integer>();

	}
	
	public void setAsientoActivo(String asiento){		
		Butaca b = mapaButacas.get(Integer.valueOf(asiento));
		b.setEstado(EstadoButaca.SELECCIONADA);
		seleccionados.add(b.getIdbutaca());		
	
	}
	
	public void setAsientosActivos(String asiento){		
		String[] asientos = asiento.split(":");
		
		for(String asientoSel : asientos){
			setAsientoActivo(asientoSel);			
		}
	}
	
	public void clearAsientoActivo(String asiento){		
		if(asiento == null){return;}
		Butaca b = mapaButacas.get(Integer.valueOf(asiento));
		b.setEstado(EstadoButaca.LIBRE);
	
	}
	public void setSeatMap(String seatMap){			
		
		
		if(seatMap==null){
			return;
		}
		
		int x=0;
		int y=0;
		for (int i = 0;i < seatMap.length(); i++){			
		    String estado = String.valueOf(seatMap.charAt(i));
		   
		    Butaca b = butacas[x][y];

		   while(b==null){
			  x++;
			  if(x>maxrow){
				  x=0;
				  y++;
			  }
			  b = butacas[x][y];
		   }
		    

		   if(y > maxcol){
			   break;
		   }
			   
		    int estadoValue = Integer.valueOf(estado);
		    switch (estadoValue) {
			case 0:
				b.setEstado(EstadoButaca.LIBRE);
				break;
			case 1:
			case 2:
				b.setEstado(EstadoButaca.OCUPADA);
				break;
			case 3:
			case 4:
			case 5:
			default:
				b.setEstado(EstadoButaca.NODISPONIBLE);
				break;			
			}
		    
		    x++;
		}	
		
	}
	
	public void errorEscena(){		
		limpiarEscena();
		resetSeleccionados();
	}
	public void limpiarEscena(){		
		glayout.removeAllViews();
	}
	
	public TextView prepararEscena(){
		TextView devolver = null;
		limpiarEscena();
		glayout.setPadding(0,0,0,0);		
		
	
		
		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
		        ViewGroup.LayoutParams.MATCH_PARENT,
		        ViewGroup.LayoutParams.MATCH_PARENT,
		        1.0f);
		rowLp.setMargins(1, 1, 1, 1);		
		
		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
				TableRow.LayoutParams.MATCH_PARENT,
				TableRow.LayoutParams.WRAP_CONTENT,
		        1.0f);		
		cellLp.width= 0;		
		cellLp.setMargins(1, 1, 1, 1);		

		 for (int f=0; f<=maxcol; f++) {
             TableRow tr = new TableRow(contesto);
             tr.setLayoutParams(rowLp);
             tr.setPadding(0, 0, 0, 0);
             for (int c=-1; c<=maxrow+1; c++) { 
            	 
                 TextView textView = new TextView(contesto);
                 textView.setLayoutParams(cellLp);

                 if(c==-1 || c == maxrow+1){
                     textView.setBackgroundColor(Butaca.VACIOCOLOR);
                     textView.setText(String.valueOf(maxcol-f+1));
                 }else if( butacas[c][f] == null){
                     textView.setBackgroundColor(Butaca.VACIOCOLOR);
                 }else{

                     if(butacas[c][f].getEstado()!=null){
	                       // textView.setText("a");
                    	 if(butacas[c][f].getAtributos()!=0){ //Minusvalidos y otros tipos
 	                        textView.setBackgroundColor(Butaca.NODISPONIBLECOLOR);
                    	 }else{                   	 
		                     switch (butacas[c][f].getEstado()) {
		         			case LIBRE:
		         				if(butacas[c][f].isVip()){
			 	                     textView.setBackgroundColor(Butaca.VIPCOLOR);                    		 
			                     }else{
			                    	 textView.setBackgroundColor(Butaca.DISPONIBLECOLOR);
			                     }
		         				break;
		         			case NODISPONIBLE:
		                        textView.setBackgroundColor(Butaca.NODISPONIBLECOLOR);
		         				break;
		         			case OCUPADA:
		                        textView.setBackgroundColor(Butaca.OCUPADOCOLOR);		                        
		         				break;
		         			case SELECCIONADA:
		                        textView.setBackgroundColor(Butaca.SELECCIONADOCOLOR);
		                        textView.setText(String.valueOf(butacas[c][f].getColumna()));
		                        devolver = textView;
		         				break;
		         			default:
		         				break;
		         			}
                      }
                     	 
                     }else{                    	 
                         textView.setBackgroundColor(Butaca.ERRORCOLOR);
                     } 
                     textView.setId(butacas[c][f].getIdbutaca());
                 }
                 textView.setTextSize(5.0f);                
                 textView.setHeight(30);
                 textView.setGravity(Gravity.CENTER);
                 tr.addView(textView,cellLp);
         		final Button b = (Button) contesto.findViewById(R.id.buttonBloquear);				
         		
                 textView.setOnClickListener(new View.OnClickListener() {
                	    @Override
                	    public void onClick(View v) {
                     		final boolean bloqueado = b.getText().equals("Desbloquear");

                	    	if(bloqueado){
            	    			Toast.makeText(contesto, "Los asientos estan bloqueados", Toast.LENGTH_SHORT).show();
                	    		return;
                	    	}
                	    	
                	    	TextView tv = (TextView)v;
                	    	ColorDrawable cd = (ColorDrawable) v.getBackground();
                	    	
                	    	Butaca b = mapaButacas.get(Integer.valueOf(v.getId()));
                	    	
                	    	if(cd.getColor() == Butaca.SELECCIONADOCOLOR){
                                seleccionados.remove(Integer.valueOf(v.getId()));                     	    	
                                tv.setText("");
                                if(b.isVip()){
                                	v.setBackgroundColor(Butaca.VIPCOLOR);
                                }else{
                                	v.setBackgroundColor(Butaca.DISPONIBLECOLOR);
                                }
                	    	}else if(cd.getColor() == Butaca.VIPCOLOR || cd.getColor() == Butaca.DISPONIBLECOLOR) {
                	    		if(seleccionados.size()>= maximosAsientosBloqueados){
                	    			Toast.makeText(contesto, "No puedes seleccionar mas de " + maximosAsientosBloqueados + " asientos", Toast.LENGTH_SHORT).show();
                	    			return;
                	    		}
                	    		seleccionados.add(v.getId());                	    		
                	    		v.setBackgroundColor(Butaca.SELECCIONADOCOLOR); 
                                tv.setText(String.valueOf(b.getColumna()));
                    	    }
                	       
                	    }
                	});
                 

             } // for
             glayout.addView(tr,rowLp);
         } // for   
		 return devolver;
	}
	
	
	
	
	public Butaca[][] getButacas() {
		return butacas;
	}

	public void setButacas(Butaca[][] butacas) {
		this.butacas = butacas;
	}


	public List<Integer> getSeleccionados() {
		return seleccionados;
	}


	public void setSeleccionados(List<Integer> seleccionados) {
		this.seleccionados = seleccionados;
	}

}
