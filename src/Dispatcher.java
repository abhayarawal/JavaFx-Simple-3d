/*
 *  Dispatcher renders the user interface and listens for use input. It then calls the renderer to render objects to the canvas. 
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

@SuppressWarnings("deprecation")
public class Dispatcher {
	
	// the main arraylist of objects
	private ArrayList<Object> objects;
	
	// temporary placeholder
	Object object;
	
	// viewport X, Y
	int viewportX, viewportY;
	
	// default selection is -1
	int selected = -1;
	
	// all scene elements are defined globally
	private Stage stage;
	private Canvas canvas;
	private Renderer renderer;
	private Scene scene;
	
	// shift, control key pressed?
	boolean shift, ctrl;
	
	// offsets to create a draggable window
	private double offX = 0, offY = 0;
	
	// main layouts defined globally
	VBox prVBox = new VBox(6);
	VBox stVBox = new VBox(6);
	HBox objHBox = new HBox(4);
	VBox slVBox = new VBox(6);
	HBox end = new HBox();
	StackPane viewport;
	
	
	// gives individual objects unique id
	int objId = 0;
	
	// render the grid?
	private boolean gridRender = true;
	
	// title for the app window, will be modified with vertices
	Label title = new Label("Super Sweet Renderer");
	
	// constructor initializes the stage, object arraylist, and the viewport size
	Dispatcher(Stage st, int vx, int vy) {
		stage = st;
		viewportX = vx;
		viewportY = vy;
		
		objects = new ArrayList<Object>();
		renderUI();		
	}
	
	// renders the user interface
	void renderUI()
	{		
		// the main layout
		BorderPane root = new BorderPane();
		
		// viewport defined as stackpane, houses the canvas
		viewport = new StackPane();
		
		// viewport set at the center of the borderpane
		root.setCenter(viewport);
		
		// initialize individual layout outside the function
		initTop(root);
		initToolbar(root);
		initLToolbar(root);
		
		// the footer of the application window
		root.setBottom(end);
		warning("Action done: application ready");
		
		
		/* initialize the scene */
		scene = new Scene(root, viewportX, viewportY);
		scene.getStylesheets().add("UI.css");
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setTitle("Super Sweet Renderer");
		stage.setScene(scene);
		stage.show();
		
		// set key listeners on scene
		dispatchKeyEvents();
			
		// set the main canvas
		canvas = new Canvas(viewport.getWidth(), viewport.getHeight()-20);		
		viewport.getChildren().add(canvas);
		
		// initialize the renderer
		renderer = new Renderer((int) viewport.getWidth(), (int) viewport.getHeight());
		
		// call the render function
		render();
	}
	
	
	// the render function calls the render function inside renderer
	private void render()
	{
		// pass the canvas, objects arraylist, selected element, and grid render (boolean)
		renderer.render(canvas, objects, selected, gridRender);
		
		// fetch the vertices count and modify title label
		title.setText("Super Sweet Renderer" + ", " + renderer.getVC());
		
		boolean wire = renderer.getWire();
		boolean culling = renderer.getCulling();
		boolean face = renderer.getFace();
		Renderer.Illumination ilm = renderer.ilm;
		Renderer.Shading sd = renderer.shading;
		int r = renderer.clR, g = renderer.clG, b = renderer.clB; 
		renderer = new Renderer((int) viewport.getWidth(), (int) viewport.getHeight());
		renderer.setFace(face);  renderer.setCulling(culling);  renderer.setWire(wire);
		renderer.ilm = ilm;
		renderer.shading = sd;
		renderer.clR = r; renderer.clG = g; renderer.clB = b; 
	}
	
	
	// initialize the main toolbar on the left pane
	private void initLToolbar(BorderPane root) {
		// VBox houses all the individual buttons of toolbar
		VBox ltool = new VBox(5);
		root.setLeft(ltool);
		ltool.setPrefWidth(80);
		ltool.setStyle("-fx-padding: 10;");
		
		
		// the buttons for the left toolbar
		Button newDoc = new Button("New");
		Button importObj = new Button("Import");
		ToggleButton culling = new ToggleButton("Culling");
		ToggleButton wire = new ToggleButton("Wire");
		Button trash = new Button("t");
		ToggleButton grid = new ToggleButton("g");
		ToggleButton face = new ToggleButton("Face");
		
		newDoc.setStyle("-fx-pref-width: 45");
		
		// icomoon font set for icon fonts
		trash.getStyleClass().add("icomoon-btn");
		grid.getStyleClass().add("icomoon-btn");
		grid.setSelected(true);
		wire.setSelected(false);
		face.setSelected(true);
		
		// grid button action
		grid.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				gridRender = !gridRender;
				render();
			}			
		});
		
		// import object function opens the explorer window and calls the importObject function if file is selected
		importObj.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				FileChooser fc = new FileChooser();
				fc.setTitle("Import object file");
				
				// set restriction on file type
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Object Files (*.obj)", "*.obj"));
				File file = fc.showOpenDialog(stage);
				if (file != null)
				{
					importObject(file);
				}
			}
		});
		
		culling.setSelected(true);
		culling.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				renderer.setCulling(!renderer.getCulling());
				render();
			}			
		});
		
		wire.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				renderer.setWire(!renderer.getWire());
				render();
			}			
		});
		
		face.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				renderer.setFace(!renderer.getFace());
				render();
			}			
		});
				
		final ComboBox<String> ilum = new ComboBox<String>();
		ilum.getItems().addAll(
			"Ambient", "Diffuse", "Specular"
		);
		ilum.setValue("Specular");
		ilum.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String l, String n) {
				renderer.ilm = Renderer.Illumination.valueOf(n.toUpperCase());
				render();
			}			
		});
		
		final ComboBox<String> shading = new ComboBox<String>();
		shading.getItems().addAll(
			"Faceted", "Gouroud", "Phong"
		);
		shading.setValue("Faceted");
		shading.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String l, String n) {
				renderer.shading = Renderer.Shading.valueOf(n.toUpperCase());
				render();
			}			
		});
		
			
		// add the buttons to the toolbar
		ltool.getChildren().addAll(newDoc, importObj, trash, grid, culling, wire, face, ilum, shading);
		
		// new doc re-initializes the application
		newDoc.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				warning("Action done: new document");
				selected = -1;
				objId = 0;
				objects.clear();
				initSelection();
				render();
			}
		});
		
		// trash deletes the object from objects arraylist if object is selected
		trash.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (selected == -1)
					warning("Operation canceled: no object selected");
				else
				{
					warning("Primitive deleted");
					objects.remove(selected);
					initSelection();
					render();
				}
			}
		});
	}

	String objN;
	Integer lsI = 0;
	boolean merge;
	
	// importObject convert .obj file into native object and adds to the objects arraylist
	private void importObject(File file) {
		lsI = 0;		
		ArrayList<Integer[]> faces = new ArrayList<Integer[]>();
		Action response = Dialogs.create()
				.owner(stage)
				.title("Object Import Action")
				.message("Do you want to merge all nodes?")
				.actions(Dialog.ACTION_YES, Dialog.ACTION_NO)
				.showConfirm();
		
		if (response == Dialog.ACTION_YES)
			merge = true;
		else
			merge = false;
				
		try {
			ArrayList<Vertex> vertices = new ArrayList<Vertex>();
			ArrayList<Integer> edges = new ArrayList<Integer>();
			
			// lambda used to read individual lines
			Files.lines(Paths.get(file.getCanonicalPath())).forEach(
					(String str) -> {
						if (str.startsWith("#") && !merge)
						{
							if (str.indexOf("object") != -1)
								objN = str.split("\\s+")[2];
							
							if (str.indexOf("polygons") != -1 || str.indexOf("triangles") != -1)
							{
								// initialize new object with the vertices and edges and add it to the objects arraylist
								Object obj = new Object(objN+", "+objId, vertices, edges);
								obj.setFace(faces);
								objId++;
								objects.add(obj);
								
								lsI += vertices.size();
								
								vertices.clear();
								edges.clear();
								faces.clear();
							}
						}
						
						// if the line starts with v, it's the vertices
						if (str.startsWith("v"))
						{
							String split[] = str.split("\\s+");
							Vertex v = new Vertex(
								-1 * Double.parseDouble(split[1]), // flip the x vertex
								Double.parseDouble(split[2]),
								Double.parseDouble(split[3])
							);		
							
							vertices.add(v);
						}
						
						// if the line starts with f, it's a face, change it to edges
						if (str.startsWith("f"))
						{
							String split[] = str.split("\\s+");
							Integer[] fc = new Integer[split.length-1];
							for (int i=1, l=split.length; i<l; i++)
							{
								fc[i-1] = Integer.parseInt(split[i])-lsI;
								if (i == l-1)
								{
									edges.add(Integer.parseInt(split[i])-lsI);
									edges.add(Integer.parseInt(split[1])-lsI);
								}
								else
								{
									edges.add(Integer.parseInt(split[i])-lsI);
									edges.add(Integer.parseInt(split[i+1])-lsI);
								}
							}
							faces.add(fc);
						}
					}
			);
			
			if (merge)
			{	
				Object obj = new Object("Object, "+objId, vertices, edges);
				obj.setFace(faces);
				objId++;
				objects.add(obj);
			}
			
			render();
			warning("Action done: import complete");
			initProperties();
			initSelection();
		} catch (IOException e) {
			warning("Warning: could not complete import");
		}
	}

	// initialize the right pane toolbar
	private void initToolbar(BorderPane root) {
		// each segment is part of the VBox
		VBox toolbar = new VBox(10);
		toolbar.setPrefWidth(220);
		toolbar.setStyle("-fx-padding: 10;");
		root.setRight(toolbar);
		
		// initialize each segment outside the function
		initPrimitives();
		initProperties();
		initSelection();
				
		VBox.setVgrow(slVBox, Priority.ALWAYS);
		
		ScrollPane sp = new ScrollPane();
		sp.setPannable(true);
		sp.setHbarPolicy(ScrollBarPolicy.NEVER);
		sp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		sp.setContent(slVBox);
				
		/* Initialize the main toolbar */
		toolbar.getChildren().addAll(prVBox, stVBox, sp);
	}

	// the selection segment
	private void initSelection() {
		// clear the segment everytime for re-render
		slVBox.getChildren().clear();
		
		// all object seletion button are part of the same togglegroup
		ToggleGroup group = new ToggleGroup();
		Label slLabel = new Label("Objects");	
		
		slVBox.getChildren().addAll(slLabel);
		
		Slider slR = new Slider();
		slR.setMin(0);
		slR.setMax(255);
		slR.setValue(255);
		slR.setMajorTickUnit(255);
		slR.setMinorTickCount(0);
		slR.setBlockIncrement(10);
		
		slR.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
            	renderer.clR = new_val.intValue();
            	render();
            }
        });
		
		Slider slG = new Slider();
		slG.setMin(0);
		slG.setMax(255);
		slG.setValue(255);
		slG.setMajorTickUnit(255);
		slG.setMinorTickCount(0);
		slG.setBlockIncrement(10);
		
		slG.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
            	renderer.clG = new_val.intValue();
            	render();
            }
        });
		
		Slider slB = new Slider();
		slB.setMin(0);
		slB.setMax(255);
		slB.setValue(255);
		slB.setMajorTickUnit(255);
		slB.setMinorTickCount(0);
		slB.setBlockIncrement(10);
		
		slB.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    renderer.clB = new_val.intValue();
                    render();
            }
        });
		
		slVBox.getChildren().addAll(slR, slG, slB);
		
		// if objects size is more than 0, loop through the objects arraylist
		if (objects.size() > 0)
		{
			// solo button
//			Button solo = new Button("Solo");
//			Button all = new Button("All");	
//			objHBox.getChildren().addAll(solo, all);
			
			// add the Hbox to the Vbox
//			slVBox.getChildren().addAll(objHBox);
			
			int t = 0;
			ToggleButton[] primitives = new ToggleButton[objects.size()];
			Iterator<Object> oiterator = objects.iterator();
			
			while (oiterator.hasNext())
			{
				object = oiterator.next();
				primitives[t] = new ToggleButton(object.id());
				primitives[t].setToggleGroup(group);
				primitives[t].setPrefWidth(150);
				primitives[t].setUserData(t);
				
				// each button is assigned to their respective item in arraylist
				slVBox.getChildren().add(primitives[t]);
				t++;
			}
			
			// listen for togglegroup change
			group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			    public void changed(ObservableValue<? extends Toggle> ov,
			        Toggle toggle, Toggle new_toggle) {
			            if (new_toggle == null)
			            	selected = -1;
			            else
			            	selected = Integer.parseInt(group.getSelectedToggle().getUserData().toString());
			            
			            warning("Primitive Selected, " + selected);
			            viewport.requestFocus();
			            render();
			         }
			    	
			});
			
			// make the first object the default selection
			primitives[0].setSelected(true);
		}
		
		// objects arraylist is empty
		else
		{
			selected = -1;
			Label empty = new Label("No objects in viewport");
			slVBox.getChildren().addAll(empty);
		}
	}

	// warning method sets message at the footer of the application window
	void warning(String msg)
	{
		end.setAlignment(Pos.CENTER_RIGHT);
		end.getChildren().clear();
		Label message = new Label(msg);
		end.getStyleClass().add("end");
		message.getStyleClass().add("endLbl");
		end.getChildren().add(message);
	}
	
	// initialize the application init primitive properties (empty)
	private void initProperties() {
		/* Primitive setup */		
		stVBox.getChildren().clear();
		Label stLabel = new Label("Message: No primitive object selected.");
		stLabel.setWrapText(true);
		stVBox.getChildren().addAll(stLabel);
		stVBox.getStyleClass().add("set");
	}

	/* Primitive toolset */
	private void initPrimitives() {
		// flowpane to stack all the primitive buttons right next to each other with automatic line breaks
		FlowPane primitives = new FlowPane();
		primitives.setHgap(4);
		primitives.setVgap(4);
		
		Label prLabel = new Label("Primitives");		
		
		// primitive buttons
		Button box = new Button("Box");
		Button cylinder = new Button("Cylinder");
		Button sphere = new Button("Sphere");
		Button pyramid = new Button("Pyramid");
		Button plane = new Button("Plane");
		
		// add the primitive buttons to the flowpane
		primitives.getChildren().addAll(box, cylinder, sphere, pyramid, plane);
		prVBox.getChildren().addAll(prLabel, primitives);
		prVBox.getStyleClass().add("set");
		
		// each primitive calls it's respective function
		box.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				initBox();
			}
		});
		
		cylinder.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				initCylinder();
			}
		});
		
		sphere.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				initSphere();
			}
		});
		
		pyramid.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				initPyramid();
			}
		});
		
		plane.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				initPlane();
			}
		});
	}

	// initialize the top bar of the application
	private void initTop(BorderPane root) {
		/* Create a stackpane for the top of border pane */
		StackPane top = new StackPane();
		root.setTop(top);
		
		/* UI elements */
		Button min = new Button("-");
		Button tgMax = new Button("m/M");
		Button exit = new Button("Exit");
		
		/* GridPane to split the window into two */
		GridPane gPane = new GridPane();
		
		/* ColumnConstraints instances with 50% width set */
		ColumnConstraints left = new ColumnConstraints();
		ColumnConstraints right = new ColumnConstraints();
		left.setPercentWidth(50);
		right.setPercentWidth(50);
		gPane.getColumnConstraints().addAll(left, right);
		
		/* Add the title bar */
		gPane.add(title, 0, 1);
		
		/* HBox node for the window controls and set alignment adjustment to right */
		HBox tHBox = new HBox(4);
		tHBox.setAlignment(Pos.TOP_RIGHT);
		tHBox.getChildren().addAll(min, tgMax, exit);
		
		/* Add the HBox node into the grid pane */
		gPane.add(tHBox, 1, 1);
		top.setPrefHeight(30);
		top.getStyleClass().add("top");
		top.getChildren().add(gPane);
		
		/* Exit the application */
		exit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			}
		});
		
		/* Toggle window maximize/restore */
		tgMax.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				stage.setFullScreen(!stage.isFullScreen());
			}
		});
		
		/* Minimize the window */
		min.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				stage.setIconified(true);
			}
		});
		
		// create the offset of the window
		top.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				offX = stage.getX() - event.getScreenX();
                offY = stage.getY() - event.getScreenY();
			}
		});
		
		// use the offset to move the window
		top.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.setX(event.getScreenX() + offX);
				stage.setY(event.getScreenY() + offY);
			}
		});
	}
	
	// initialize the selection label for properties segment
	private void initSelection(String str)
	{
		Label stLabel = new Label(str);
		stLabel.setWrapText(true);
		stVBox.getChildren().clear();
		stVBox.getChildren().addAll(stLabel);
		stVBox.getStyleClass().add("set");
	}
	
	
	// initializes the sphere primitive, THE PATTERN IS THE SAME FOR ALL PRIMITIVES
	private void initSphere() {
		
		// title
		initSelection("Primitive Type: Sphere");
		
		// use the static properties inside primitive to create UI
		int l=Sphere.getLabels().length;
		Label[] lbls = new Label[l];
		TextField[] tf = new TextField[l];
		HBox[] hb = new HBox[l];
		for(int i=0; i < l; i++)
		{
			lbls[i] = new Label(Sphere.getLabels()[i]+":");
			tf[i] = new TextField();
			tf[i].setMaxWidth(50);
			hb[i] = new HBox(5);
			hb[i].getChildren().addAll(lbls[i], tf[i]);
			stVBox.getChildren().add(hb[i]);
		}
		
		Button add = new Button("Initialize");
		Button cancel = new Button("Cancel");
		HBox ctl = new HBox(4);
		ctl.getChildren().addAll(add, cancel);
		stVBox.getChildren().add(ctl);
		
		// initialize new primitive
		add.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				// try/catch used to catch parsing errors
				try
				{
					float r = Float.parseFloat(tf[0].getText());
					int h = Integer.parseInt(tf[1].getText());
					int s = Integer.parseInt(tf[2].getText());
					
					// object added directly to the objects arraylist
					objects.add(Sphere.instance("Sphere "+objId, r, h, s));
					render();
					warning("Action done: created new primitive");
					initProperties();
					initSelection();
					objId++;
				} catch(Exception e1) { warning(e1.getMessage());} // if parsing error occurs, relay the message
			}
		});
		
		// cancel the primitive creation
		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				warning("Action done: canceled primitive init");
				initProperties();
			}
		});
	}
	
	boolean isolatedSmooth = true;
	
	// initializes the pyramid primitive, same pattern as sphere
	private void initPyramid() {
		initSelection("Primitive Type: Pyramid");
		
		int l=Pyramid.getLabels().length;
		Label[] lbls = new Label[l];
		TextField[] tf = new TextField[l];
		HBox[] hb = new HBox[l];
		for(int i=0; i < l; i++)
		{
			lbls[i] = new Label(Pyramid.getLabels()[i]+":");
			tf[i] = new TextField();
			tf[i].setMaxWidth(50);
			hb[i] = new HBox(5);
			hb[i].getChildren().addAll(lbls[i], tf[i]);
			stVBox.getChildren().add(hb[i]);
		}
		
		HBox smHB = new HBox();
		ToggleButton smth = new ToggleButton("Isolate Smooth");
		smth.setSelected(true);
		smHB.getChildren().add(smth);
		stVBox.getChildren().add(smHB);
		
		smth.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				isolatedSmooth = !isolatedSmooth;
			}			
		});
		
		Button add = new Button("Initialize");
		Button cancel = new Button("Cancel");
		HBox ctl = new HBox(4);
		ctl.getChildren().addAll(add, cancel);
		stVBox.getChildren().add(ctl);
		
		add.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try
				{
					float l = Float.parseFloat(tf[0].getText());
					float w = Integer.parseInt(tf[1].getText());
					float h = Integer.parseInt(tf[2].getText());
					Object pyTmp = Pyramid.instance("Pyramid "+objId, l, w, h);
					pyTmp.smoothGr = !isolatedSmooth;
					objects.add(pyTmp);
					render();
					warning("Action done: created new primitive");
					initProperties();
					initSelection();
					objId++;
				} catch(Exception e1) { warning(e1.getMessage());}
			}
		});
		
		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				warning("Action done: canceled primitive init");
				initProperties();
			}
		});
	}
	
	// initializes the plane primitive, same pattern as sphere
	private void initPlane() {
		initSelection("Primitive Type: Plane");
		
		int l=Plane.getLabels().length;
		Label[] lbls = new Label[l];
		TextField[] tf = new TextField[l];
		HBox[] hb = new HBox[l];
		for(int i=0; i < l; i++)
		{
			lbls[i] = new Label(Plane.getLabels()[i]+":");
			tf[i] = new TextField();
			tf[i].setMaxWidth(50);
			hb[i] = new HBox(5);
			hb[i].getChildren().addAll(lbls[i], tf[i]);
			stVBox.getChildren().add(hb[i]);
		}
		
		Button add = new Button("Initialize");
		Button cancel = new Button("Cancel");
		HBox ctl = new HBox(4);
		ctl.getChildren().addAll(add, cancel);
		stVBox.getChildren().add(ctl);
		
		add.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try
				{
					float l = Float.parseFloat(tf[0].getText());
					float w = Integer.parseInt(tf[1].getText());
					int segX = Integer.parseInt(tf[2].getText());
					int segY = Integer.parseInt(tf[3].getText());
					objects.add(Plane.instance("Plane "+objId, l, w, segX, segY));
					render();
					warning("Action done: created new primitive");
					initProperties();
					initSelection();
					objId++;
				} catch(Exception e1) { warning(e1.getMessage());}
			}
		});
		
		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				warning("Action done: canceled primitive init");
				initProperties();
			}
		});
	}
	
	// initializes the box primitive, same pattern as sphere
	private void initBox() {
		initSelection("Primitive Type: Box");
		
		int l=Box.getLabels().length;
		Label[] lbls = new Label[l];
		TextField[] tf = new TextField[l];
		HBox[] hb = new HBox[l];
		for(int i=0; i < l; i++)
		{
			lbls[i] = new Label(Box.getLabels()[i]+":");
			tf[i] = new TextField();
			tf[i].setMaxWidth(50);
			hb[i] = new HBox(5);
			hb[i].getChildren().addAll(lbls[i], tf[i]);
			stVBox.getChildren().add(hb[i]);
		}
		
		Button add = new Button("Initialize");
		Button cancel = new Button("Cancel");
		HBox ctl = new HBox(4);
		ctl.getChildren().addAll(add, cancel);
		stVBox.getChildren().add(ctl);
		
		add.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try
				{
					float l = Float.parseFloat(tf[0].getText());
					float w = Integer.parseInt(tf[1].getText());
					float h = Integer.parseInt(tf[2].getText());
					objects.add(Box.instance("Box "+objId, l, w, h));
					render();
					warning("Action done: created new primitive");
					initProperties();
					initSelection();
					objId++;
				} catch(Exception e1) { warning(e1.getMessage());}
			}
		});
		
		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				warning("Action done: canceled primitive init");
				initProperties();
			}
		});
	}

	// initializes the cylinder primitive, same pattern as sphere
	private void initCylinder() {
		initSelection("Primitive Type: Cylinder");
		
		int l=Cylinder.getLabels().length;
		Label[] lbls = new Label[l];
		TextField[] tf = new TextField[l];
		HBox[] hb = new HBox[l];
		for(int i=0; i < l; i++)
		{
			lbls[i] = new Label(Cylinder.getLabels()[i]+":");
			tf[i] = new TextField();
			tf[i].setMaxWidth(50);
			hb[i] = new HBox(5);
			hb[i].getChildren().addAll(lbls[i], tf[i]);
			stVBox.getChildren().add(hb[i]);
		}
		
		Button add = new Button("Initialize");
		Button cancel = new Button("Cancel");
		HBox ctl = new HBox(4);
		ctl.getChildren().addAll(add, cancel);
		stVBox.getChildren().add(ctl);
		
		add.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try
				{
					float r = Float.parseFloat(tf[0].getText());
					int h = Integer.parseInt(tf[1].getText());
					int s = Integer.parseInt(tf[2].getText());
					objects.add(Cylinder.instance("Cylinder "+objId, r, h, s));
					render();
					warning("Action done: created new primitive");
					initProperties();
					initSelection();
					objId++;
				} catch(Exception e1) { warning(e1.getMessage());}
			}
		});
		
		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				warning("Action done: canceled primitive init");
				initProperties();
			}
		});
	}

	// bind key events to the scene
	private void dispatchKeyEvents() {
		// if shift/ctrl are released, set the values
		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode() ==KeyCode.SHIFT) shift = false;
				if (e.getCode() ==KeyCode.CONTROL) ctrl = false;
			}
		});
		
		// listen for key press, will also check for ctrl, and shift
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				warning("Object operation, " + selected);
				if (e.getCode() == KeyCode.F) translate(Object.Translate.FORWARD);
				if (e.getCode() == KeyCode.B) translate(Object.Translate.BACKWARD);
				if (e.getCode() == KeyCode.L) translate(Object.Translate.LEFT);
				if (e.getCode() == KeyCode.R) translate(Object.Translate.RIGHT);
				if (e.getCode() == KeyCode.U) translate(Object.Translate.UP);
				if (e.getCode() == KeyCode.D) translate(Object.Translate.DOWN);
				if (e.getCode() == KeyCode.UP)
				{
					if (shift) scale(Object.Scale.UP);
					else if (ctrl) rotate(Object.Rotate.UP); 
				}
				if (e.getCode() == KeyCode.DOWN) 
				{
					if (shift) scale(Object.Scale.DOWN);
					else if (ctrl) rotate(Object.Rotate.DOWN);
				}
				if (e.getCode() == KeyCode.LEFT) 
					if (ctrl) rotate(Object.Rotate.LEFT);
				if (e.getCode() == KeyCode.RIGHT) 
					if (ctrl) rotate(Object.Rotate.RIGHT);
				if (e.getCode() == KeyCode.COMMA) 
					if (ctrl) rotate(Object.Rotate.ZLEFT);
				if (e.getCode() == KeyCode.PERIOD) 
					if (ctrl) rotate(Object.Rotate.ZRIGHT);
				if (e.getCode() == KeyCode.SHIFT) shift = true;
				if (e.getCode() == KeyCode.CONTROL) ctrl = true;
			}			
		});
	}

	/* 
	 * Translates the model and repaints the viewport
	 * Object.Translate enum
	 * 
	 */
	public void translate(Object.Translate t)
	{
		if (selected != -1)
		{
			objects.get(selected).translate(t);
			render();
		}
	}
	
	/* 
	 * Rotates the model and repaints the viewport
	 * Object.Rotate enum
	 *  
	 * */
	public void rotate(Object.Rotate r)
	{
		if (selected != -1)
		{
			objects.get(selected).rotate(r);
			render();
		}
	}
	
	/* 
	 * Scales the model and repaints the viewport
	 * Object.Scale enum
	 *  
	 * */
	public void scale(Object.Scale s)
	{
		if (selected != -1)
		{
			objects.get(selected).scale(s);
			render();
		}
	}
}
