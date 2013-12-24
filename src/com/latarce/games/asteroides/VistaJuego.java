package com.latarce.games.asteroides;

import java.util.List;
import java.util.Vector;

import org.example.asteroides.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class VistaJuego extends View implements SensorEventListener{

	////// MISIL //////
	private Vector<Misil> Misiles; // Vector con los Misiles
	private Vector<Misil> Misiles_copia;
	private static int PASO_VELOCIDAD_MISIL = 12;
	
    ////// ASTEROIDES //////
    private Vector<Grafico> Asteroides; // Vector con los Asteroides
    private Vector<Grafico> Asteroides_copia;
    private int numAsteroides;
    private int numFragmentos;
    private int tipoGraficos;
    
    ////// NAVE //////
    private Grafico nave;// Gráfico de la nave
    private int giroNave; // Incremento de dirección
    private float aceleracionNave; // aumento de velocidad
    // Incremento estándar de giro y aceleración
    private static final int PASO_GIRO_NAVE = 5;
    private static final float PASO_ACELERACION_NAVE = 0.5f;

    ////// THREAD Y TIEMPO //////
    // Thread encargado de procesar el juego
    private ThreadJuego thread = new ThreadJuego();
    // Cada cuanto queremos procesar cambios (ms)
    private static int PERIODO_PROCESO = 50;
    // Cuando se realizó el último proceso
    private long ultimoProceso = 0;
 
    // GRAFICOS
    Drawable drawableNave, drawableAsteroide, drawableAsteroide2, drawableAsteroide3, drawableMisil;
    
    private float mX=0, mY=0;
    private boolean disparo = false;
    
    private boolean hayValorInicial = false;
    private float valorInicial;
    
    // SENSORES
    private SensorManager mSensorManager;
    private List<Sensor> listSensors;
    
    // AUDIO
    private SoundPool soundPool;
    private int idDisparo, idExplosion;
    
    // PUNTUACION
    private int puntuacion = 0;
    private Activity padre;
    
    // PREFERENCIAS
    private SharedPreferences preferences;
    
    public VistaJuego(Context context, AttributeSet attrs) {
          super(context, attrs);
          drawableAsteroide = context.getResources().getDrawable(R.drawable.asteroide1);
          drawableAsteroide2 = context.getResources().getDrawable(R.drawable.asteroide2);
          drawableAsteroide3 = context.getResources().getDrawable(R.drawable.asteroide3);
          drawableNave = context.getResources().getDrawable(R.drawable.nave);
          drawableMisil = context.getResources().getDrawable(R.drawable.misil1);
          Asteroides = new Vector<Grafico>();
          Misiles = new Vector<Misil>();
          
          // Preferencias
          preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
          numAsteroides = Integer.parseInt(preferences.getString("asteroides", "5"));
          numFragmentos = Integer.parseInt(preferences.getString("fragmentos", "3"));
          tipoGraficos = Integer.parseInt(preferences.getString("graficos", "1"));
          
          // Comprueba si los gráficos son vectoriales (en el caso de que sean bitmaps no hace nada más)
          if (tipoGraficos == 0){
	          ShapeDrawable dMisil = new ShapeDrawable(new RectShape());
	          dMisil.getPaint().setColor(Color.WHITE);
	          dMisil.getPaint().setStyle(Style.STROKE);
	          dMisil.setIntrinsicWidth(15);
	          dMisil.setIntrinsicHeight(3);
	          drawableMisil = dMisil;
	          
	          ShapeDrawable dAsteroid = new ShapeDrawable(new OvalShape());
	          dAsteroid.getPaint().setColor(Color.WHITE);
	          dAsteroid.getPaint().setStyle(Style.STROKE);
	          dAsteroid.setIntrinsicWidth(80);
	          dAsteroid.setIntrinsicHeight(60);
	          drawableAsteroide = dAsteroid;
	          
	          ShapeDrawable dNave = new ShapeDrawable(new RectShape());
	          dNave.getPaint().setColor(Color.BLUE);
	          dNave.getPaint().setStyle(Style.STROKE);
	          dNave.getPaint().setAlpha(255);
	          dNave.setIntrinsicWidth(40);
	          dNave.setIntrinsicHeight(20);
	          drawableNave = dNave;
          }

          // Crea el número de asteroides fijado
          for (int i = 0; i < numAsteroides; i++) {
              Grafico asteroide = new Grafico(this, drawableAsteroide);
              asteroide.setIncY(Math.random() * 4 - 2);
              asteroide.setIncX(Math.random() * 4 - 2);
              asteroide.setAngulo((int) (Math.random() * 360));
              asteroide.setRotacion((int) (Math.random() * 8 - 4));
              Asteroides.add(asteroide);
          }
          
          // Crea la nave
          nave = new Grafico(this, drawableNave);
          
          soundPool = new SoundPool( 5, AudioManager.STREAM_MUSIC , 0);
          idDisparo = soundPool.load(context, R.raw.disparo, 0);
          idExplosion = soundPool.load(context, R.raw.explosion, 0);
          
          mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }
    
	public void registerSensorManager() {
		listSensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (!listSensors.isEmpty()) {
            Sensor orientationSensor = listSensors.get(0);
            mSensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
        }
	}
    
	public void unregisterSensorManager() {
		mSensorManager.unregisterListener(this);
	}
    

    class ThreadJuego extends Thread {
    	private boolean pausa,corriendo;

    	public synchronized void pausar() {
    		pausa = true;
    	}   	 

    	public synchronized void reanudar() {
    		pausa = false;
    		notify();
   	   } 	 
    	   
    	public void detener() {
    		corriendo = false;
    		if (pausa) reanudar();
    	} 

    	@Override
    	public void run() {
    		corriendo = true;
    		while (corriendo) {
    			actualizaFisica();
    			synchronized (this) {
    				while (pausa) {
    					try {
    						wait();
                        } 
    					catch (Exception e) {
    					}
    				}
    			}
    		}
    	}
    }
    
    @Override 
    protected void onSizeChanged(int ancho, int alto, int ancho_anter, int alto_anter) {
          super.onSizeChanged(ancho, alto, ancho_anter, alto_anter);
          // Una vez que conocemos nuestro ancho y alto.
          for (Grafico asteroide: Asteroides) {
        	  do{
        	      asteroide.setPosX(Math.random()*(ancho-asteroide.getAncho()));
        	      asteroide.setPosY(Math.random()*(alto-asteroide.getAlto()));
        	  } while(asteroide.distancia(nave) < (ancho+alto)/5);
          }
          nave.setPosX(ancho/2 - nave.getAncho()/2);
          nave.setPosY(alto/2 - nave.getAlto()/2);
          ultimoProceso = System.currentTimeMillis();
          thread.start();
    }

    @Override 
    protected void onDraw(Canvas canvas) {
          super.onDraw(canvas);
          for (Grafico asteroide: Asteroides) {
              asteroide.dibujaGrafico(canvas);
          }
          
          nave.dibujaGrafico(canvas);
          
          for (int m = 0; m < Misiles.size(); m++){
	          if (Misiles.get(m).isMisilActivo()){
	        	  Misiles.get(m).dibujaGrafico(canvas);
	          }
	          else{
	        	  Misiles.remove(m);
	          }
          }
    }
    
    
    protected void actualizaFisica() {
        long ahora = System.currentTimeMillis();

        // No hagas nada si el período de proceso no se ha cumplido.
        if (ultimoProceso + PERIODO_PROCESO > ahora) {
              return;
        }

        // Para una ejecución en tiempo real calculamos retardo           
        double retardo = (ahora - ultimoProceso) / PERIODO_PROCESO;
        ultimoProceso = ahora; // Para la próxima vez

        // Actualizamos velocidad y dirección de la nave a partir de 
        // giroNave y aceleracionNave (según la entrada del jugador)
        nave.setAngulo((int) (nave.getAngulo() + giroNave * retardo));
        double nIncX = nave.getIncX() + aceleracionNave * Math.cos(Math.toRadians(nave.getAngulo())) * retardo;
        double nIncY = nave.getIncY() + aceleracionNave * Math.sin(Math.toRadians(nave.getAngulo())) * retardo;

        // Actualizamos si el módulo de la velocidad no excede el máximo
        if (Math.hypot(nIncX,nIncY) <= Grafico.getMaxVelocidad()){
              nave.setIncX(nIncX);
              nave.setIncY(nIncY);
        }

        // Actualizamos posiciones X e Y
        nave.incrementaPos(retardo);
        for (Grafico asteroide : Asteroides) {
              asteroide.incrementaPos(retardo);
              if (asteroide.verificaColision(nave)) {
                  salir();
              }
        }
        
        // Actualizamos posición de misil
		Asteroides_copia = (Vector<Grafico>) Asteroides.clone();
		Misiles_copia = (Vector<Misil>) Misiles.clone();
        for (int h = 0; h < Misiles.size(); h++){
	        if (Misiles.get(h).isMisilActivo()) {
	        	Misiles.get(h).incrementaPos(retardo);
	        	Misiles.get(h).setTiempoMisil(Misiles.get(h).getTiempoMisil()-(int)retardo);
	            if (Misiles.get(h).getTiempoMisil() < 0) {
	            	Misiles.get(h).setMisilActivo(false);
	            } else {
	            	for (int i = 0; i < Asteroides.size(); i++){
	            		if (Misiles.get(h).verificaColision(Asteroides.elementAt(i))) {
	            			destruyeAsteroide(i, h);
	                        break;
	            		}
	            	}
	            }
	        }
        }
        Asteroides = (Vector<Grafico>) Asteroides_copia.clone();
		Misiles = (Vector<Misil>) Misiles_copia.clone();
    }
    
    @Override
    public boolean onTouchEvent (MotionEvent event) {
       super.onTouchEvent(event);
       float x = event.getX();
       float y = event.getY();
       switch (event.getAction()) {

       case MotionEvent.ACTION_DOWN:
              disparo=true;
              break;
       case MotionEvent.ACTION_MOVE:
              float dx = Math.abs(x - mX);
              float dy = Math.abs(y - mY);
              // Sin freno
              //float dy = mY - y;
              if (dy<10 && dx>10){
                     giroNave = Math.round((x - mX) / 2);
                     disparo = false;
              } else if (dx<10 && dy>10){
                     aceleracionNave = Math.round((mY - y) / 25);         	 
                     disparo = false;
              }
              break;

       case MotionEvent.ACTION_UP:
              giroNave = 0;
              aceleracionNave = 0;
              if (disparo){
            	  ActivaMisil();
              }

              break;

       }

       mX=x; mY=y;       

        return true;

    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float valor = event.values[1];
        if (!hayValorInicial){
        	valorInicial = valor;
        	hayValorInicial = true;
        }
        giroNave=(int) (valor-valorInicial)*2 ;	
	}

	private void ActivaMisil() {
		Misil misil = new Misil(this, drawableMisil);
		misil.setPosX(nave.getPosX()+ nave.getAncho()/2-misil.getAncho()/2);
		misil.setPosY(nave.getPosY()+ nave.getAlto()/2-misil.getAlto()/2);
		misil.setAngulo(nave.getAngulo());
		misil.setIncX(Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL);
		misil.setIncY(Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL);
		misil.setTiempoMisil((int) Math.min(this.getWidth() / Math.abs( misil.getIncX()), this.getHeight() / Math.abs(misil.getIncY())) - 2);
		misil.setMisilActivo(true);
		Misiles.add(misil);
		soundPool.play(idDisparo, 1, 1, 1, 0, 1);
	}
	 
	private void destruyeAsteroide(int ast, int mis) {
		int impactos;
		Misiles_copia.get(mis).setMisilActivo(false);
		soundPool.play(idExplosion, 1, 1, 0, 0, 1);
		puntuacion += 1000;
		
		// Se quita un impacto al asteroide
		impactos = Asteroides_copia.elementAt(ast).getImpactos();
		
		// Se comprueban los impactos que le faltan para destruirse por completo y se crean los fragmentos
		if (impactos == 2){
			// Crea el número de fragmentos fijado
			//Asteroides_copia.elementAt(ast).setDrawable(drawableAsteroide2);
			for (int i = 0; i < numFragmentos; i++) {
			    Grafico asteroide = new Grafico(this, drawableAsteroide2);
			    asteroide.setPosY(Asteroides_copia.elementAt(ast).getPosY());
			    asteroide.setPosX(Asteroides_copia.elementAt(ast).getPosX());
			    asteroide.setIncY(Asteroides_copia.elementAt(ast).getIncY() + (i-1) * (6/numFragmentos));
			    asteroide.setIncX(Asteroides_copia.elementAt(ast).getIncX() + (i-1) * (8/numFragmentos));
			    asteroide.setAngulo(Asteroides_copia.elementAt(ast).getAngulo());
			    asteroide.setRotacion(Asteroides_copia.elementAt(ast).getRotacion() + (int) (Math.random() * 8 - 4));
			    asteroide.setImpactos(1);
			    Asteroides_copia.add(asteroide);
			}
		}
		else if(impactos == 1){
			//Asteroides_copia.elementAt(ast).setDrawable(drawableAsteroide3);
			for (int i = 0; i < numFragmentos; i++) {
			    Grafico asteroide = new Grafico(this, drawableAsteroide3);
			    asteroide.setPosY(Asteroides_copia.elementAt(ast).getPosY());
			    asteroide.setPosX(Asteroides_copia.elementAt(ast).getPosX());
			    asteroide.setIncY(Asteroides_copia.elementAt(ast).getIncY() + (i-1) * (6/numFragmentos));
			    asteroide.setIncX(Asteroides_copia.elementAt(ast).getIncX() + (i-1) * (8/numFragmentos));
			    asteroide.setAngulo(Asteroides_copia.elementAt(ast).getAngulo());
			    asteroide.setRotacion(Asteroides_copia.elementAt(ast).getRotacion() + (int) (Math.random() * 8 - 4));
			    asteroide.setImpactos(2);
			    Asteroides_copia.add(asteroide);
			}
		}
		
		// Se destruye el asteroide impactado
		Asteroides_copia.remove(ast);
	   
		if (Asteroides_copia.isEmpty()) {
			salir();
		}
	}

	public ThreadJuego getThread() {
		return thread;
	}
	
	 public void setPadre(Activity padre) {
		 this.padre = padre;
	 }
	 
	 private void salir() {
		 Bundle bundle = new Bundle();
		 bundle.putInt("puntuacion", puntuacion);
		 Intent intent = new Intent();
		 intent.putExtras(bundle);
		 padre.setResult(Activity.RESULT_OK, intent);
		 padre.finish();
	 }
	
}
