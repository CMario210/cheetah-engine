/*
 * Copyright 2017 Carlos Rodriguez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package game.enemies;

import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.Clip;

import engine.audio.AudioUtil;
import engine.components.Attenuation;
import engine.components.GameComponent;
import engine.components.MeshRenderer;
import engine.components.SpotLight;
import engine.core.Time;
import engine.core.Transform;
import engine.core.Vector2f;
import engine.core.Vector3f;
import engine.physics.PhysicsUtil;
import engine.rendering.Material;
import engine.rendering.Mesh;
import engine.rendering.RenderingEngine;
import engine.rendering.Shader;
import engine.rendering.Texture;
import engine.rendering.Vertex;
import game.Auschwitz;
import game.Level;
import game.Player;
import game.pickUps.Bullet;

/**
 *
 * @author Carlos Rodriguez
 * @version 1.2
 * @since 2017
 */
public class NaziSoldier extends GameComponent {

    private static final float MAX_HEALTH = 100f;
    private static final float SHOT_ANGLE = 10.0f;
    private static final float DAMAGE_MIN = 18.75f;
    private static final float DAMAGE_RANGE = 22.5f;
    private static final float NAZI_WIDTH = 0.4f;

    private static final int STATE_IDLE = 0;
    private static final int STATE_CHASE = 1;
    private static final int STATE_ATTACK = 2;
    private static final int STATE_DYING = 3;
    private static final int STATE_POST_DEATH = 4;
    private static final int STATE_DONE = 5;
    private static final int STATE_HIT = 6;
    private static final int STATE_DEAD = 7;
    
    private static final String RES_LOC = "naziSoldier/";

    private static final Clip seeNoise = AudioUtil.loadAudio(RES_LOC + "SSSSIT");
    private static final Clip shootNoise = AudioUtil.loadAudio("weapons/pistol/GUN");
    private static final Clip hitNoise = AudioUtil.loadAudio(RES_LOC + "SPOPAIN");
    private static final Clip deathNoise = AudioUtil.loadAudio(RES_LOC + "SSSDTH");

    private static ArrayList<Texture> animation;
    private static Mesh mesh;
    private static Random rand;
    private float sizeX;

    private Transform transform;
    private Material material;
    private MeshRenderer meshRenderer;
    private Bullet bullet;
    private SpotLight light;

    private int state;
    public boolean isQuiet;
    private boolean canAttack;
    private boolean canLook;
    private boolean dead;
    private double deathTime;
    private double health;

    /**
     * Constructor of the actual enemy.
     * @param transform the transform of the data.
     * @param renderingEngine of the enemy.
     */
    public NaziSoldier(Transform transform) {
        rand = new Random();

        animation = new ArrayList<Texture>();

        animation.add(new Texture(RES_LOC + "TRANA0"));
        animation.add(new Texture(RES_LOC + "TRANB0"));
        animation.add(new Texture(RES_LOC + "TRANC0"));
        animation.add(new Texture(RES_LOC + "TRAND0"));

        animation.add(new Texture(RES_LOC + "TRANE0"));
        animation.add(new Texture(RES_LOC + "TRANF0"));
        animation.add(new Texture(RES_LOC + "TRANG0"));

        animation.add(new Texture(RES_LOC + "TRANH0"));
        animation.add(new Texture(RES_LOC + "TRANH1"));
        animation.add(new Texture(RES_LOC + "TRANI0"));
        animation.add(new Texture(RES_LOC + "TRANJ0"));
        animation.add(new Texture(RES_LOC + "TRANK0"));
        animation.add(new Texture(RES_LOC + "TRANL0"));
        
        animation.add(new Texture(RES_LOC + "SSWVN0"));
        animation.add(new Texture(RES_LOC + "SSWVO0"));

        if (mesh == null) {
            final float sizeY = 0.8f;
            sizeX = (float) ((double) sizeY / (sizeY * 2.0));

            final float offsetX = 0.05f;
            final float offsetY = 0.01f;

            final float texMinX = -offsetX;
            final float texMaxX = -1 - offsetX;
            final float texMinY = -offsetY;
            final float texMaxY = 1 - offsetY;

            Vertex[] verts = new Vertex[]{new Vertex(new Vector3f(-sizeX, 0, 0), new Vector2f(texMaxX, texMaxY)),
                new Vertex(new Vector3f(-sizeX, sizeY, 0), new Vector2f(texMaxX, texMinY)),
                new Vertex(new Vector3f(sizeX, sizeY, 0), new Vector2f(texMinX, texMinY)),
                new Vertex(new Vector3f(sizeX, 0, 0), new Vector2f(texMinX, texMaxY))};

            int[] indices = new int[]{0, 1, 2,
                                        0, 2, 3};

            mesh = new Mesh(verts, indices, true);
        }
        
        this.transform = transform;
        this.material = new Material(animation.get(0));
        this.meshRenderer = new MeshRenderer(mesh, getTransform(), material);
        this.light = new SpotLight(new Vector3f(0.5f,0.3f,0.1f), 0.8f, 
        	    	new Attenuation(0.1f,0.1f,0.1f), new Vector3f(-2,0,5f), new Vector3f(1,1,1), 0.7f);   
        this.state = 0;
        this.canAttack = true;
        this.canLook = true;
        this.dead = false;
        this.deathTime = 0.0;
        this.health = MAX_HEALTH;
    }

    float offsetX = 0;
    float offsetY = 0;

    /**
     * Updates the enemy every single frame.
     * @param delta of time
     */
    public void update(double delta) {
        //Set Height
        transform.setPosition(transform.getPosition().getX(), 0, transform.getPosition().getZ());
        
        Vector3f playerDistance = transform.getPosition().sub(Level.getPlayer().getCamera().getPos());

        Vector3f orientation = playerDistance.normalized();
        float distance = playerDistance.length();
        setDistance(distance);

        float angle = (float) Math.toDegrees(Math.atan(orientation.getZ() / orientation.getX()));

        if (orientation.getX() > 0)
            angle = 180 + angle;

        transform.setRotation(0, angle + 90, 0);

        //Action/Animation
        double time = Time.getTime();

        if (!dead && health <= 0) {
        	bullet = new Bullet(new Transform(transform.getPosition()), false);
            dead = true;
            deathTime = time;
            state = STATE_DYING;
            seeNoise.stop();
            shootNoise.stop();
            hitNoise.stop();
            AudioUtil.playAudio(deathNoise, distance);
            light.removeToEngine();
        }

        if (!dead) {
            Player player = Level.getPlayer();

            Vector2f playerDirection = transform.getPosition().sub(
                    player.getCamera().getPos().add(
                            new Vector3f(player.getSize().getX(), 0, player.getSize().getY()).mul(0.5f))).getXZ().normalized();

            switch(state) {
            	case STATE_IDLE:
            		isQuiet = true;
                    double timeDecimals = (time - (double) ((int) time));

                    if (timeDecimals >= 0.5) {
                        material.setDiffuse(animation.get(1));
                        canLook = true;
                    } else {
                        material.setDiffuse(animation.get(0));
                        if (canLook) {
                            Vector2f lineStart = transform.getPosition().getXZ();
                            Vector2f lineEnd = lineStart.sub(playerDirection.mul(1000.0f));

                            Vector2f nearestIntersect = Auschwitz.getLevel().checkIntersections(lineStart, lineEnd, false);
                            Vector2f playerIntersect = PhysicsUtil.lineIntersectRect(lineStart, lineEnd, player.getCamera().getPos().getXZ(), player.getSize());

                            if (playerIntersect != null && (nearestIntersect == null
                                    || nearestIntersect.sub(lineStart).length() > playerIntersect.sub(lineStart).length())) {
                                AudioUtil.playAudio(seeNoise, distance);
                                state = STATE_CHASE;
                            }

                            canLook = false;
                        }
                    }
            		break;
            	case STATE_CHASE:
            		light.removeToEngine();
                	isQuiet = false;
                    if (rand.nextDouble() < 0.5f * delta) {
                        state = STATE_ATTACK;
                    }

                    if (distance > 1.0f) {
                        orientation.setY(0);
                        float moveSpeed = 1.55f;

                        Vector3f oldPos = transform.getPosition();
                        Vector3f newPos = transform.getPosition().add(orientation.mul((float) (-moveSpeed * delta)));

                        Vector3f collisionVector = Auschwitz.getLevel().checkCollisions(oldPos, newPos, NAZI_WIDTH, NAZI_WIDTH);

                        Vector3f movementVector = collisionVector.mul(orientation.normalized());

                        if (!movementVector.equals(orientation.normalized())) {
                            Auschwitz.getLevel().openDoors(transform.getPosition(), false);
                        }

                        if (movementVector.length() > 0) {
                            transform.setPosition(transform.getPosition().add(movementVector.mul((float) (-moveSpeed * delta))));
                        }
                    } else {
                        state = STATE_ATTACK;
                    }
                    timeDecimals = (time - (double) ((int) time));

                    while (timeDecimals > 0.5) {
                        timeDecimals -= 0.5;
                    }

                    timeDecimals *= 1.5f;

                    if (timeDecimals <= 0.25f) {
                        material.setDiffuse(animation.get(0));
                    } else if (timeDecimals <= 0.5f) {
                        material.setDiffuse(animation.get(1));
                    } else if (timeDecimals <= 0.75f) {
                        material.setDiffuse(animation.get(2));
                    } else {
                        material.setDiffuse(animation.get(3));
                    }
            		break;
            	case STATE_ATTACK:
            		isQuiet = true;
                    timeDecimals = (time - (double) ((int) time));

                    if (timeDecimals <= 0.25f) {
                        material.setDiffuse(animation.get(4));
                    } else if (timeDecimals <= 0.5f) {
                        material.setDiffuse(animation.get(5));
                    } else if (timeDecimals <= 0.7f) {
                        if (canAttack) {
                        	light.setPosition(transform.getPosition());
                            light.setDirection(orientation.mul(-1));
                            light.addToEngine();
                            Vector2f shootDirection = playerDirection.rotate((rand.nextFloat() - 0.5f) * SHOT_ANGLE);

                            Vector2f lineStart = transform.getPosition().getXZ();
                            Vector2f lineEnd = lineStart.sub(shootDirection.mul(1000.0f));

                            Vector2f nearestIntersect = Auschwitz.getLevel().checkIntersections(lineStart, lineEnd, false);
                            canAttack = false;

                            Vector2f playerIntersect = PhysicsUtil.lineIntersectRect(lineStart, lineEnd, player.getCamera().getPos().getXZ(), player.getSize());

                            if (playerIntersect != null && (nearestIntersect == null
                                    || nearestIntersect.sub(lineStart).length() > playerIntersect.sub(lineStart).length())) {

                            	float damage;
                                if(player.getHealth() <= 0) {
                                	state = STATE_DONE;
                                	light.removeToEngine();
                                } else {
                                	damage = DAMAGE_MIN + rand.nextFloat() * DAMAGE_RANGE;
                                	if(player.isArmor() == false) {
                                		player.addHealth((int) -damage, "German Soldier");
                                	}else {
                                		player.addArmor((int) -damage);
                                	}
                         
                                }
                                
                            }
                            AudioUtil.playAudio(shootNoise, distance);
                        }
                        material.setDiffuse(animation.get(6));
                    } else {
                    	light.removeToEngine();
                        canAttack = true;
                        material.setDiffuse(animation.get(5));
                        state = STATE_CHASE;
                    }
            		break;
            	case STATE_DONE:
            		isQuiet = true;
                	timeDecimals = (time - (double) ((int) time));

                    if (timeDecimals <= 0.75f) {
                        material.setDiffuse(animation.get(13));
                    } else {
                        material.setDiffuse(animation.get(14));
                    }
            		break;
            	case STATE_HIT:
            		isQuiet = true;
                	timeDecimals = (time - (double) ((int) time));
                    if (timeDecimals <= 0.5f) {
                        material.setDiffuse(animation.get(7));
                    } else {
                        state = STATE_CHASE;
                    }
            		break;
            }
            
        }
  
        switch(state) {
	    	case STATE_DYING:
	    		isQuiet = true;
	            dead = true;

	            final float time1 = 0.1f;
	            final float time2 = 0.3f;
	            final float time3 = 0.45f;
	            final float time4 = 0.6f;

	            if (time <= deathTime + 0.2f) {
	                material.setDiffuse(animation.get(9));
	            } else if (time > deathTime + time1 && time <= deathTime + time2) {
	                material.setDiffuse(animation.get(10));
	            } else if (time > deathTime + time2 && time <= deathTime + time3) {
	                material.setDiffuse(animation.get(11));
	            } else if (time > deathTime + time3 && time <= deathTime + time4) {
	                material.setDiffuse(animation.get(12));
	            } else if (time > deathTime + time4) {
	                state = STATE_DEAD;
	            }
	    		break;
	    	case STATE_DEAD:
	    		isQuiet = true;
	        	bullet.update(delta);
	        	material.setDiffuse(animation.get(12));   	
	            dead = true;  
	            if (distance < bullet.PICKUP_THRESHHOLD)
	            	state = STATE_POST_DEATH;
	    		break;
	    	case STATE_POST_DEATH:
	    		isQuiet = false;
	        	material.setDiffuse(animation.get(12));
	    		break;
        }

    }

    /**
     * Method that calculates the damage that the enemy receives.
     * @param amt amount.
     */
    public void damage(int amt) {
        if (state == STATE_IDLE) {
            state = STATE_CHASE;
        }

        health -= amt;

        if (health > 0 && amt > 0) {
        	state = STATE_HIT;
        	AudioUtil.playAudio(hitNoise, transform.getPosition().sub(Level.getPlayer().getCamera().getPos()).length());     	
        }
    }

    /**
     * Method that renders the enemy's mesh.
     * @param shader to render
     * @param renderingEngine to use
     */
    public void render(Shader shader, RenderingEngine renderingEngine) {
        Vector3f prevPosition = transform.getPosition();
        transform.setPosition(new Vector3f(transform.getPosition().getX() + offsetX, transform.getPosition().getY() + offsetY, transform.getPosition().getZ()));
        
        if (state == STATE_DEAD)    	
        	bullet.render(shader, renderingEngine);
        meshRenderer.render(shader, renderingEngine);

        transform.setPosition(prevPosition);
    }
    
    /**
     * Sets the state to start with.
     * @param state to set.
     */
    public void setState(int state) {this.state = state;}

    /**
	 * Gets the enemy's actual transformation.
	 * @return the enemy's transform data.
	 */
    public Transform getTransform() {return transform;}

    /**
	 * Gets if the enemy is dead or not.
	 * @return the enemy's life state.
	 */
    public boolean isAlive() {return !dead;}

    /**
     * Returns the enemy's size depending on the enemy's own width,
     * all of this in a Vector2f.
     * @return vector with the size.
     */
    public Vector2f getSize() {return new Vector2f(NAZI_WIDTH, NAZI_WIDTH);}

    /**
     * Gets the enemy's actual health.
     * @return enemy's health.
     */
	public double getHealth() {return health;}
    
}