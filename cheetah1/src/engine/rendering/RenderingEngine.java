/*
 * Copyright 2018 Carlos Rodriguez.
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
package engine.rendering;

import static engine.components.Constants.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.HashMap;

import engine.components.BaseLight;
import engine.components.Camera;
import engine.components.GameComponent;
import engine.components.PointLight;
import engine.components.SpotLight;
import engine.core.Debug;
import engine.core.Vector3f;
import engine.core.crash.CrashReport;
import engine.rendering.resourceManagement.MappedValues;

/**
 *
 * @author Carlos Rodriguez
 * @version 1.2
 * @since 2018
 */
public class RenderingEngine extends MappedValues {
	
	private Camera 						mainCamera;
	private BaseLight 					activeLight;
	private Shader 						forwardAmbient;
	
	private static ArrayList<BaseLight> lights;
	private HashMap<String, Integer> 	samplerMap;
	
	/**
	 * Constructor for the rendering engine.
	 */
	public RenderingEngine() {
		super();
        lights = new ArrayList<BaseLight>();
        samplerMap = new HashMap<String, Integer>();
		samplerMap.put("diffuse", 0);
		samplerMap.put("normalMap", 1);
		samplerMap.put("dispMap", 2);
        
		forwardAmbient = new Shader("forward-ambient");
		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glFrontFace(GL_CW);
        glCullFace(GL_BACK);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        glEnable(GL_TEXTURE_2D);
	}
	
	/**
     * Renders everything every on screen.
     */
    public void render(GameComponent component) {
    	try {
    		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    		
	        component.render(forwardAmbient, this);
			
			for(BaseLight light : lights) {
				
				glEnable(GL_BLEND);
				glBlendFunc(GL_ONE, GL_ONE);
				glDepthMask(false);
				glDepthFunc(GL_EQUAL);
				
				switch(light.getShader().getName()) {
					case"forward-directional":
						activeLight = light;
						component.render(light.getShader(), this);
					break;
					case"forward-point":
						if(((PointLight) light).getDistance() < LIGHT_POP_IN) {
							activeLight = light;
							float distance = 0;
							if(component.getTransform() != null)
								distance = ((PointLight) light).getPosition().sub(component.getTransform().getPosition()).length();
							if(distance < ((PointLight) light).getRange())
								component.render(light.getShader(), this);
						}
					break;
					case"forward-spot":
						if(((SpotLight) light).getDistance() < LIGHT_POP_IN) {
							activeLight = light;
							float distance = 0;
							if(component.getTransform() != null)
								distance = ((SpotLight) light).getPosition().sub(component.getTransform().getPosition()).length();
							if(distance < ((SpotLight) light).getRange())
								component.render(light.getShader(), this);
						}
					break;
				}
				
				glDepthFunc(GL_LESS);
				glDepthMask(true);
				glDisable(GL_BLEND);
			}
    	} catch(RuntimeException e) {
    		Debug.crash(new CrashReport(e));
    	}
    }
    
    /**
	 * Cleans everything light related.
	 */
    public void clearLights() { lights.clear();}

    /**
     * Sets textures to openGL.
     * @param enabled Able switch.
     */
    public static void setTextures(boolean enabled) {
        if (enabled) {
            glEnable(GL_TEXTURE_2D);
        } else {
            glDisable(GL_TEXTURE_2D);
        }
    }
	
	/**
	 * Adds a new directional light to the rendering engine.
	 * @param light to add.
	 */
	public void addLight(BaseLight light) { lights.add(light); }
	
	/**
	 * Removes a new directional light to the rendering engine.
	 * @param light to remove.
	 */
	public void removeLight(BaseLight light) { lights.remove(light); }
	
	/**
	 * Returns the light that is been used.
	 * @return Active light.
	 */
	public BaseLight getActiveLight() {return activeLight;}

	/**
	 * Sets a new color for the fog.
	 * @param color of the fog.
	 */
	public void setFogColor(Vector3f color) {AddVector3f("fogColor", color); setClearColor(color);}

	/**
	 * Sets a density to the actual fog.
	 * @param fogDensity to set.
	 */
	public void setFogDensity(float fogDensity) {AddFloat("fogDensity", fogDensity);}

	/**
	 * Sets a new gradient for the fog.
	 * @param fogGradient to set.
	 */
	public void setFogGradient(float fogGradient) {AddFloat("fogGradient", fogGradient);}
	
	/**
	 * Returns the sampler's slot of the texture's unit.
	 * @param samplerName of the texture.
	 * @return Texture's slot.
	 */
	public int getSamplerSlot(String samplerName) { return samplerMap.get(samplerName); }

	/**
     * Bind the textures to openGL.
     */
    @SuppressWarnings("unused")
	private static void unbindTextures() {glBindTexture(GL_TEXTURE_2D, 0);}

    /**
     * Cleans all the colors in the environment.
     * @param color to clean.
     */
    public static void setClearColor(Vector3f color) {glClearColor(color.getX(), color.getY(), color.getZ(), 1.0f);}
	
	/**
	 * Sets a new ambient light.
	 * @return ambient to set.
	 */
	public void setAmbientLight(Vector3f ambient) {AddVector3f("ambient", ambient);}
    
    /**
     * Returns the main camera in game.
     * @return camera.
     */
    public Camera getMainCamera() { return mainCamera; }

    /**
     * Sets the main camera of all the game to the rendering
     * engine.
     * @param mainCamera of the game.
     */
	public void setMainCamera(Camera mainCamera) { this.mainCamera = mainCamera; }

}
