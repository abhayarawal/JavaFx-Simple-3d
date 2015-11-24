/*
 *  Super Sweet Renderer v 0.1
 *  AUTHOR Abhaya S Rawal
 *  
 *  The program is a basic 3d renderer. A 3d object is instantiated with it's vertices and edges. 
 *  It is then loaded in the renderer which draws the 3d object on the viewport in perspective mode. 
 *  Various key bindings allows to rotate, scale, or translate the object in the viewport. 
 *  Each object is dynamically generated using the primitives tool. 
 *  The program uses JavaFx library to render it's user interface.
 *  Any wavefront object file can be imported into the application.
 *  
 *  Scale - Shift (Up, Down)
 *  Rotate - Ctrl + ( Up, Down, Left, Right, Comma, Period )
 *  Translate - F, B, D, U, L, R
 *  
 */

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	// initial viewport size
	int viewportX = 1400;
	int viewportY = 850;
	
	public static void main(String[] args) {
		// launch the app, super method
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		@SuppressWarnings("unused")
		
		// initialize the dispatcher with the stage, and viewport size
		Dispatcher dispatcher = new Dispatcher(stage, viewportX, viewportY);
	}
}
