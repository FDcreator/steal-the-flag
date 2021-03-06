package com.damato.brothers.stealtheflag.game.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.damato.brothers.stealtheflag.game.GameMain;
import com.damato.brothers.stealtheflag.game.screens.GameScreen;

public class Player extends Sprite {
    //for animation
    public enum  State {  STANDING, JUMPING,FALLING, WALKING, SHOOTING, DEAD };
    
    public enum Arm {
    	PISTOL(10, 15, 8, 0.5f, 12, 40), SUB(5, 30, 8, 0.25f, 30, 150), BAZOOKA(50, 5, 20, 1.5f, 5, 10);
    	
    	int damage;
    	int speed;
    	int dimension;
    	float timeLife;
    	int countBullet;
    	int countRechargeBullet;
    	Arm(int dam, int spe, int dimen, float timeli, int countB, int countRB) {
    		damage = dam;
    		speed = spe;
    		dimension = dimen;
    		timeLife = timeli;
    		countBullet = countB;
    		countRechargeBullet = countRB;
    	}
    	
    };

    public State currentState;
    public State previousState;

    public World world;
    public Body b2body;

    private float stateTimer;
    private float jumpTime;
    private boolean isDead;
    private boolean destroyed;
    private boolean isHit;
    private boolean shot;
    private int life;
    private Arm arm;
    private int countBullet;
    private int countRechargeBullet;

    private Vector2 position;

    private boolean walkRight;
    private boolean walkLeft;

    private String color;

    private Array<FireBall> fireBalls;
    
    private ShapeRenderer renderer;

    public Player(GameScreen gameScreen){
        world = gameScreen.getWorld();
        this.renderer = gameScreen.getShapeRender();
        life = 100;
        arm = Arm.PISTOL;
        countBullet = arm.countBullet;
        countRechargeBullet = arm.countRechargeBullet;
        walkLeft = false;
        walkRight = false;
        stateTimer = 0;
        jumpTime = 0;
        isDead = false;
        destroyed = false;
        //isHit e shot talvez não sejam utilizados (BR-F)
        isHit = false;
        shot = false;
        position = new Vector2(4016, 256);

        fireBalls = new Array<FireBall>();

        definePlayer();
        setBounds(0,0,60/GameMain.PPM, 60/GameMain.PPM);
        
        Gdx.app.log("PLAYER", "arm: " + getArm());
    }
    public void update(float dt){
        setPosition(b2body.getPosition().x- getWidth() / 2,
                b2body.getPosition().y- getHeight() / 2);
        getRegion(dt);

        for (FireBall fireBall: fireBalls){
            fireBall.update(dt);
            if (fireBall.isDestroy()){
                fireBalls.removeValue(fireBall,true);
            }
        }
        
        if ( Gdx.input.isKeyJustPressed(Input.Keys.C)) {
        	Gdx.app.log("ARM", countBullet + "/" + countRechargeBullet);
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
        	rechargeArm();
        }
    }
    public void dispose(){

    }
    public void jump(float dt){
        jumpTime +=dt;
        if ( currentState != State.JUMPING &&
                currentState != State.FALLING && (b2body.getLinearVelocity().y ==0)) {
            b2body.applyLinearImpulse(new Vector2(0, 6.5f), b2body.getWorldCenter(), true);
            jumpTime = 0;
            // currentState = State.JUMPING;
        }
        if (currentState == State.JUMPING && jumpTime >=0.3f){
            b2body.applyLinearImpulse(new Vector2(0, 0.5f), b2body.getWorldCenter(), true);
            jumpTime = 0;
        }
    }
    public void fire(){
        shot = true;
        if (fireBalls.size < 1 && countBullet > 0) {
            fireBalls.add(new FireBall(this));
            countBullet--;
        }
    }

    public void definePlayer(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(position.x/ GameMain.PPM,position.y/ GameMain.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);
        FixtureDef fixdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(28/ GameMain.PPM,28/ GameMain.PPM);
        fixdef.filter.categoryBits = GameMain.PLAYER_BIT;
        fixdef.filter.maskBits = GameMain.GROUND_BIT | GameMain.WALL_BIT | GameMain.FIREBALL_BIT
        | GameMain.FLAG_BIT;
        fixdef.shape = shape;

        b2body.createFixture(fixdef).setUserData(this);

    }
    public TextureRegion getRegion(float dt){
        //used state here
        //invertendo textura
       /* if (walkLeft && !region.isFlipX()){
            region.flip(true,false);
        }else if (walkRight && region.isFlipX()){
            region.flip(true,false);
        }*/

        /*se statetimet = current timer e for igual a previousState
        faça stateTimer + dt, caso não todos ficam iguais a zero*/
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;

        currentState = getState();
        return null;
    }
    public State getState() {

        if (isDead) {
            return State.DEAD;
        } else if (shot) {
            shot = false;
            return State.SHOOTING;
        } else if ((b2body.getLinearVelocity().y > 0)) {
            return State.JUMPING;
        } else if (b2body.getLinearVelocity().y < 0 ) {
            return State.FALLING;
        } else if (walkLeft || walkRight) {
            return State.WALKING;
        } else {
            return State.STANDING;
        }
    }

    public boolean isDead(){
        return  isDead;
    }
    public World getPlayerWorld(){
        return world;
    }
    public boolean getDirectionR(){
        //retorna verdadeiro se anda para direita e falso se anda  para esquerda
        return walkRight;
    }

    public void setDirectionR(boolean directionR) {
    	this.walkRight = directionR;
    }
    
    public float getStateTimer(){
        return  stateTimer;
    }
    public Array<FireBall> getFireBalls(){
        return fireBalls;
    }
    public void setToDestroyed(boolean destroyed){
        destroyed = this.destroyed;
    }
    public void setWalkDirection(boolean walkR,boolean walkL){
        walkRight = walkR;
        walkLeft = walkL;
    }
    public void hitInBody(int damage){
        //attached
    	if ( getLife() > 0 ) {
    		if ( getLife() - damage <= 0 ) {
        		setLife(0);
        	} else {
        		setLife( getLife() - damage );
        	}
    	}
    }
    
    public void updateMove() {
    	b2body.setTransform(getPosition().x, getPosition().y, 0);
    	//System.out.println("x: " + getX());
    }
    
    public void updateShooting() {
    	if ( currentState == Player.State.SHOOTING ) {
    		fire();
    		//Gdx.app.log("Player", "SHOOTING");
    	}
    }
    
    public void drawLife() {
    	float width = ( getWidth() * 2 ) * (getLife() / 100f);
    	//Gdx.app.log("PLAYER", "life: " + life);
    	float height = 10 / GameMain.PPM;
    	
    	renderer.begin(ShapeType.Line);
    	renderer.setColor(Color.WHITE);
    	renderer.rect(getPosition().x - (getWidth()), getPosition().y + getHeight(),  ( getWidth() * 2 ), height);
    	renderer.end();
    	
    	renderer.begin(ShapeType.Filled);
    	renderer.setColor(Color.RED);
    	renderer.rect(getPosition().x - (getWidth()), getPosition().y + getHeight(), width, height);
    	renderer.end();
    	
    }
    
    private void rechargeArm() {
    	if (  countRechargeBullet > 0 && countBullet < 30) {
    		
    		if ( countRechargeBullet > arm.countBullet - countBullet) {
	    		int count = arm.countBullet - countBullet; // 30 - 20 = 10
	    		countRechargeBullet -= count; // x - 10
	    		countBullet += count; // 20 + 10 = 30
    		} else {
    			countBullet += countRechargeBullet;
    			countRechargeBullet = 0;
    		}
    		
    	}
    }
    
    public void setPosition(Vector2 position) {
		this.position = position;
	}
    
    public Vector2 getPosition() {
		return position;
	}

    public int getLife() {
		return life;
	}
    
    public void setLife(int life) {
		this.life = life;
	}
    
    public Arm getArm() {
		return arm;
	}
    
    public void setArm(Arm arm) {
		this.arm = arm;
	}
    
    public int getCountBullet() {
		return countBullet;
	}
    
    public void setCountBullet(int countBullet) {
		this.countBullet = countBullet;
	}
    
    public int getCountRechargeBullet() {
		return countRechargeBullet;
	}
    
    public void setCountRechargeBullet(int countRechargeBullet) {
		this.countRechargeBullet = countRechargeBullet;
	}
    
    public int getDamage() {
    	return arm.damage;
    }

}
