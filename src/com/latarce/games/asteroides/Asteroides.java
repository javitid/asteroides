package com.latarce.games.asteroides;

import org.example.asteroides.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class Asteroides extends Activity implements OnClickListener{

	public static AlmacenPuntuaciones almacen;
	//private MediaPlayer mp;
	private TextView texto;
	private Animation animacion;
	private SharedPreferences preferences;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//Preferencias
		//preferences = getBaseContext().getSharedPreferences("preferencias_principal", MODE_PRIVATE);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		//Anim
		texto = (TextView) findViewById(R.id.textView);
	    animacion = AnimationUtils.loadAnimation(this, R.anim.giro_con_zoom);
	    if (animacion != null && texto != null){
	    	texto.startAnimation(animacion);
	    }
		
		//Audio
		//mp = MediaPlayer.create(this, R.raw.audio);
		//mp.start();
	    //startService(new Intent(Asteroides.this, ServicioMusica.class));
			    
		// Set up click listeners for all the buttons
		View button1 = findViewById(R.id.button1);
		button1.setOnClickListener(this);
		View button2 = findViewById(R.id.button2);
		button2.setOnClickListener(this);
		View button3 = findViewById(R.id.button3);
		button3.setOnClickListener(this);
		View button4 = findViewById(R.id.button4);
		button4.setOnClickListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle estadoGuardado){
		super.onSaveInstanceState(estadoGuardado);
		//if (mp != null) {
		//	int pos = mp.getCurrentPosition();
		//	estadoGuardado.putInt("posicion", pos);
		//}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle estadoGuardado){
		super.onRestoreInstanceState(estadoGuardado);
		//if (estadoGuardado != null && mp != null) {
		//	int pos = estadoGuardado.getInt("posicion");
		//	mp.seekTo(pos);
		//}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//mp.pause();
		texto.clearAnimation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Música
		//mp.start();
	    if(preferences.getBoolean("musica", false)){
	    	startService(new Intent(Asteroides.this, ServicioMusica.class));
	    }
	    else{
	    	stopService(new Intent(Asteroides.this, ServicioMusica.class));
	    }
		
	    // Almacén de puntuaciones
	    if(preferences.getString("almacenamiento", "0").equals("0")){
	    	almacen = new AlmacenPuntuacionesArray();
	    }
	    else if(preferences.getString("almacenamiento", "0").equals("1")){
	    	almacen = new AlmacenPuntuacionesPreferencias(this);
	    }
	    else if(preferences.getString("almacenamiento", "0").equals("2")){
	    	almacen = new AlmacenPuntuacionesFicheroInterno(this);
	    }
	    else if(preferences.getString("almacenamiento", "0").equals("3")){
	    	almacen = new AlmacenPuntuacionesFicheroExterno(this);
	    }
	    else if(preferences.getString("almacenamiento", "0").equals("4")){
	    	almacen = new AlmacenPuntuacionesFicheroRecursos(this);
	    }
	    else if(preferences.getString("almacenamiento", "0").equals("5")){
	    	almacen = new AlmacenPuntuacionesXML_SAX(this);
	    }
	    else if(preferences.getString("almacenamiento", "0").equals("6")){
	    	almacen = new AlmacenPuntuacionesSQLite(this);
	    }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(Asteroides.this, ServicioMusica.class));
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.config:
				lanzarPreferencias(null);
            break;
	        case R.id.acercaDe:
	               lanzarAcercaDe(null);
	               break;
        }
        return true; /** true -> consumimos el item, no se propaga*/
    }
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button1:
				lanzarJuego(null);
			break;
			case R.id.button2:
				lanzarPreferencias(null);
			break;
			case R.id.button3:
				lanzarAcercaDe(v);
				break;
			case R.id.button4:
				lanzarPuntuaciones(v);
				break;
		}
	}
	
    public void lanzarJuego(View view){
    	Intent h = new Intent(this, Juego.class);
    	//startActivity(h);
        startActivityForResult(h, 1234);
    }
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==1234 & resultCode==RESULT_OK & data!=null) {
			int puntuacion = data.getExtras().getInt("puntuacion");
            String nombre = "Yo";
            // Mejor leerlo desde un Dialog o una nueva actividad                           
            //AlertDialog.Builder
            almacen.guardarPuntuacion(puntuacion, nombre, System.currentTimeMillis());
            lanzarPuntuaciones();
		}
	}

	public void lanzarPreferencias(View view){
    	Intent i = new Intent(this, Preferencias.class);
    	startActivity(i);
    }
    
    public void lanzarAcercaDe(View view){
    	Intent j = new Intent(this, AcercaDe.class);
    	startActivity(j);
    }
    
    public void lanzarPuntuaciones(View view){
    	lanzarPuntuaciones();
    }
    
    public void lanzarPuntuaciones(){
    	Intent k = new Intent(this, Puntuaciones.class);
    	startActivity(k);
    }
    
}
