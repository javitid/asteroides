package com.latarce.games.asteroides;

import android.graphics.drawable.Drawable;
import android.view.View;

public class Misil extends Grafico{

	private boolean misilActivo = false;
	private int tiempoMisil;
	
	public Misil(View view, Drawable drawable) {
		super(view, drawable);
		// TODO Auto-generated constructor stub
	}

	public boolean isMisilActivo() {
		return misilActivo;
	}

	public void setMisilActivo(boolean misilActivo) {
		this.misilActivo = misilActivo;
	}

	public int getTiempoMisil() {
		return tiempoMisil;
	}

	public void setTiempoMisil(int tiempoMisil) {
		this.tiempoMisil = tiempoMisil;
	}

}
