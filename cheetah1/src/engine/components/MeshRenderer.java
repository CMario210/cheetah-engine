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
package engine.components;

import engine.core.Transform;
import engine.rendering.Material;
import engine.rendering.Mesh;
import engine.rendering.RenderingEngine;
import engine.rendering.Shader;

/**
 *
 * @author Carlos Rodriguez.
 * @version 1.1
 * @since 2018
 */
public class MeshRenderer extends GameComponent {
	
	private Transform 	transform;
	private Mesh 		mesh;
	private Material 	material;
	
	/**
	 * Renderer of a mesh with more than one possible material
	 * To render.
	 * @param mesh to render.
	 * @param transform of the mesh.
	 * @param material of the mesh.
	 */
	public MeshRenderer(Mesh mesh, Transform transform, Material material) {
		this.mesh = mesh;
		this.transform = transform;
		this.material = material;		
	}
	
	/**
	 * Render method of the mesh.
	 * @param shader to render.
	 * @param renderingEngine to call.
	 */
	public void render(Shader shader, RenderingEngine renderingEngine) {
		shader.bind();
        shader.updateUniforms(transform, material, renderingEngine);
        mesh.draw();
	}

}
