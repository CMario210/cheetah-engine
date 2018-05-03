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
package game.doors;

import javax.sound.sampled.Clip;

import engine.ResourceLoader;
import engine.audio.AudioUtil;
import engine.core.GameComponent;
import engine.core.Time;
import engine.core.Transform;
import engine.core.Vector2f;
import engine.core.Vector3f;
import engine.rendering.Material;
import engine.rendering.Mesh;
import engine.rendering.Vertex;
import game.Auschwitz;
import game.Level;

/**
 *
 * @author Carlos Rodriguez
 * @version 1.0
 * @since 2018
 */
public class SecretWall implements GameComponent {
	
	private static final float HEIGHT = Level.LEVEL_HEIGHT;
	private static final float LENGTH = Level.SPOT_LENGTH;
	private static final float WIDTH = 0.125f;
	private static final int START = 0;
	
	public static float xLower;
	public static float xHigher;
	public static float yLower;
	public static float yHigher;
	
	private static final String RES_LOC = "secretWall/";

    private static final Clip openNoise = ResourceLoader.loadAudio(RES_LOC + "MEDIA");

    private static Mesh door;

    private Material material;
    private Transform transform;
    private Vector3f closedPos;
    private Vector3f openPos;

    private boolean opening;
    private boolean open;

    private double startTime;
    private double openTime;

    /**
     * Constructor of the secret wall object.
     * @param transform of the door.
     * @param material of the door.
     * @param openPosition of the door.
     */
    public SecretWall(Transform transform, Material material, Vector3f openPosition) {
        this.transform = transform;
        this.openPos = openPosition;
        this.closedPos = transform.getPosition();
        this.material = material;

        opening = false;
        open = false;
        startTime = 0;
        openTime = 0;

        if (door == null) {
            door = new Mesh();

            Vertex[] doorVerts = new Vertex[]{	new Vertex(new Vector3f(START, START, START), new Vector2f(xHigher, yHigher)),
												new Vertex(new Vector3f(START, HEIGHT, START), new Vector2f(xHigher, yLower)),
												new Vertex(new Vector3f(LENGTH, HEIGHT, START), new Vector2f(xLower, yLower)),
												new Vertex(new Vector3f(LENGTH, START, START), new Vector2f(xLower, yHigher)),
												
												new Vertex(new Vector3f(START, START, START), new Vector2f(xHigher, yHigher)),
												new Vertex(new Vector3f(START, HEIGHT, START), new Vector2f(xHigher, yLower)),
												new Vertex(new Vector3f(LENGTH, HEIGHT, START), new Vector2f(xLower, yLower)),
												new Vertex(new Vector3f(LENGTH, START, START), new Vector2f(xLower, yHigher)),
												
												new Vertex(new Vector3f(START, START, START), new Vector2f(xHigher, yHigher)),
												new Vertex(new Vector3f(START, HEIGHT, START), new Vector2f(xHigher, yLower)),
												new Vertex(new Vector3f(LENGTH, HEIGHT, START), new Vector2f(xLower, yLower)),
												new Vertex(new Vector3f(LENGTH, START, START), new Vector2f(xLower, yHigher)),
												
												new Vertex(new Vector3f(START, START, START), new Vector2f(xHigher, yHigher)),
												new Vertex(new Vector3f(START, HEIGHT, START), new Vector2f(xHigher, yLower)),
												new Vertex(new Vector3f(LENGTH, HEIGHT, START), new Vector2f(xLower, yLower)),
												new Vertex(new Vector3f(LENGTH, START, START), new Vector2f(xLower, yHigher))};

            int[] doorIndices = new int[]{0, 1, 2,
            								0, 2, 3,
            								6, 5, 4,
            								7, 6, 4,
            								10, 9, 8,
            								11, 10, 8,
            								12, 13, 14,
            								12, 14, 15};

            door.addVertices(doorVerts, doorIndices, true);
        }
    }

    /**
     * Opening secret wall's method.
     * @param time of opening.
     * @param delay of opening.
     */
    public void open(float time, float delay) {
        if (opening || open) {
            return;
        }

        startTime = (double) Time.getTime() / (double) Time.SECOND;
        openTime = startTime + (double) time;

        opening = true;
        if(opening == true)
        AudioUtil.playAudio(openNoise, transform.getPosition().sub(Transform.getCamera().getPos()).length());
    }

    /**
     * Refresh the secret wall every single frame.
     */
    public void update() {
        if (opening) {
            double time = (double) Time.getTime() / (double) Time.SECOND;

            if (time < openTime) {
                double lerpFactor = (time - startTime) / (openTime - startTime);

                transform.setPosition(openPos.lerp(closedPos, (float) lerpFactor));
            }
        } else {
            transform.setPosition(closedPos);
        }
    }

    /**
     * Renders the secret wall.
     */
    public void render() {
        Auschwitz.updateShader(transform.getTransformation(), transform.getPerspectiveTransformation(), material);
        door.draw();
    }

    /**
     * Checks if the secret wall is open or not.
     * @return Secret wall state.
     */
    public boolean isOpen() {
        return open;
    }
    
    /**
     * Checks if the secret wall tries to open or not.
     * @return Secret wall state.
     */
    public boolean opens() {
        return opening;
    }

    /**
     * Returns the transform of the secret wall.
     * @return Transform.
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * Returns the size of the door in the level.
     * @return Size of the door.
     */
    public Vector2f getSize() {
        if (transform.getRotation().getY() == 0) {
            return new Vector2f(HEIGHT, WIDTH);
        } else {
            return new Vector2f(WIDTH, HEIGHT);
        }
    }
    
    /**
     * Returns the XZ position of the secret wall in transform.
     * @return XZ position.
     */
    public Vector2f getPosXZ() {
        return new Vector2f(transform.getPosition().getX(), transform.getPosition().getZ());
    }
}