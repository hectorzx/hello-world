package cinesavip;

import com.cinesavip.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		
		inicializarVideo();
	}
	
	private void inicializarVideo(){
		
		String urlVideo = getIntent().getStringExtra("URL");
		

	    Uri uri = Uri.parse(urlVideo);

	    VideoView videoView = (VideoView)findViewById(R.id.myvideoview);
	    
	    MediaController mc = new MediaController(this);
	    mc.setAnchorView(videoView);
	    mc.setMediaPlayer(videoView);
	    videoView.setMediaController(mc);
	    videoView.setVideoURI(uri);
	    videoView.start();
		
	}
}
