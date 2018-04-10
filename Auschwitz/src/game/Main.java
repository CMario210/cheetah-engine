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
package game;

import engine.core.CoreDisplay;

/**
*
* @author Carlos Rodriguez
* @version 1.3
* @since 2017
*/
public class Main {
	
	/**
	 * The main method of the program, takes everything to show and put it
	 * To work like it should.
	 * @param args arguments.
	 */
	public static void main(String[] args) {
		
		CoreDisplay display = new CoreDisplay(1280, 720, 250);
		display.createWindow("Auschwitz", false);
		display.run();
	}

}
