package com.latarce.games.asteroides;

import org.example.asteroides.R;

import android.app.Activity;
import android.os.Bundle;

public class Juego extends Activity {

	private VistaJuego vistaJuego;
	
   @Override public void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
       setContentView(R.layout.juego);
       vistaJuego = (VistaJuego) findViewById(R.id.VistaJuego);
       vistaJuego.setPadre(this);
   }
   
   @Override protected void onPause() {
	   super.onPause();
	   vistaJuego.getThread().pausar();
	   vistaJuego.unregisterSensorManager();
   }

	@Override protected void onResume() {
	   super.onResume();
	   vistaJuego.getThread().reanudar();
	   vistaJuego.registerSensorManager();
	}

	@Override protected void onDestroy() {
	   vistaJuego.getThread().detener();
	   super.onDestroy();
	}
}
