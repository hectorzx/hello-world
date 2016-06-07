package publicidad;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import android.content.Context;

public class Publicidad {

	private static Publicidad instance = null;
	private InterstitialAd mInterstitialAd;
	
	protected Publicidad(Context contexto) {
		// Exists only to defeat instantiation.
		mInterstitialAd = new InterstitialAd(contexto);
		mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
		
		 mInterstitialAd.setAdListener(new AdListener(){
	          public void onAdLoaded(){
	        	  mInterstitialAd.show();
	          }
		 });	
		/*mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });*/
	}

	public static Publicidad getInstance(Context contexto) {
		if (instance == null) {
			instance = new Publicidad(contexto);
		}
		return instance;
	}
	
	 private void requestNewInterstitial() {
	        AdRequest adRequest = new AdRequest.Builder()
	                  .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
	                  .build();

	        mInterstitialAd.loadAd(adRequest);
	    }
	 
	 public void mostrarPubli(){
			requestNewInterstitial();
	 }
}
