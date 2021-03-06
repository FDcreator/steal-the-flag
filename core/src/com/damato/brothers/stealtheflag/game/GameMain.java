package com.damato.brothers.stealtheflag.game;

import com.badlogic.gdx.Game;
import com.damato.brothers.stealtheflag.game.connection.ServerManagement;
import com.damato.brothers.stealtheflag.game.screens.MenuScreen;

public class GameMain extends Game {

	//covert meter in centimeter
	public static final float PPM = 100;
	public static final int V_WIDTH = 1024;
	public static final int V_HEIGHT = 512;

	//for fixtures data, help in colision world
	public static final short PLAYER_BIT = 1;
	public static final short GROUND_BIT = 2;
	public static final short WALL_BIT = 4;
	public static final short FIREBALL_BIT = 8;
	public static final short FLAG_BIT = 16;
	public ServerManagement server;

	@Override
	public void create() {	
		
		this.server = new ServerManagement();
		setScreen(new MenuScreen(this));
	}
}
