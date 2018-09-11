package Main;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application{
	private long test = System.currentTimeMillis();
	private final int WIDTH = 640, HEIGHT = 480;
	private final int HORIZON = (int) (HEIGHT * 0.3);
	private final int OFFSET = 30;
	private int score = 0;
	private List<Hole> holes;
	private ImageView denkaView, balubaView;
	private Pane pane;
	private Label scoreLabel;
	
	private GraphicsContext gc;

	public static void main(String[] args) {

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Canvas canvas = new Canvas(WIDTH, HEIGHT);

		balubaView = new ImageView(new Image("misc/elda.png", 100, 200, true, true));
		balubaView.setVisible(false);
		
		scoreLabel = new Label("Score: " + score);
		scoreLabel.setFont(Font.font("Bauhaus 93", 18));
		HBox h = new HBox(scoreLabel);
		h.setPrefSize(WIDTH, HEIGHT);
		h.setAlignment(Pos.TOP_RIGHT);
		
		pane = new Pane(canvas, h, balubaView);

		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		scene.setCursor(Cursor.NONE);
		scene.setOnMouseClicked(e -> {
			updateScore(-10);
			System.out.println(e.getSceneX() + " " + e.getSceneY());
		});
		scene.setOnMouseEntered(e -> balubaView.setVisible(true));
		scene.setOnMouseMoved(e -> balubaRelocation(e.getSceneX(), e.getSceneY()));
		scene.setOnMouseExited(e -> balubaView.setVisible(false));

		gc = canvas.getGraphicsContext2D();

		drawSky();
		drawGrass();
		drawHoles();
		drawTerraria();
		denkaComingOut();

		primaryStage.setScene(scene);
		primaryStage.show();
		System.out.println(System.currentTimeMillis() - test);
	}

	private void balubaRelocation(double x, double y) {
		balubaView.setX(x);
		balubaView.setY(y);
		balubaView.toFront();
	}

	private void drawHoles() {
		final double HOLE_WIDTH = 150, HOLE_HEIGHT = 50;//Binding to the frame size required
		final double INNER_OFFSET = 0, SPACING = (WIDTH - 2 * OFFSET - 3 * HOLE_WIDTH) / 2;
		//final int HOLE_QUANT = 9; // Number of holes on the map

		LinearGradient lg1 = new LinearGradient(0, 0, 1, 0,
				true, CycleMethod.NO_CYCLE, 
				new Stop[] { new Stop(0, Color.BLACK), 
						new Stop(1, Color.BROWN)});
		gc.setFill(lg1);

		holes = new ArrayList<Hole>();
		double posX = OFFSET, posY = OFFSET + HORIZON;
		while(posY <= HEIGHT - OFFSET) {
			while(posX + HOLE_WIDTH <= WIDTH - OFFSET) {
				holes.add(new Hole(posX, posY));
				posX += HOLE_WIDTH + INNER_OFFSET + SPACING; 
			}
			posY += HOLE_HEIGHT + INNER_OFFSET + SPACING;
			posX = OFFSET;
		}

		for(Hole h : holes) {
			gc.fillOval(h.x + Math.random() * INNER_OFFSET, h.y + Math.random() * INNER_OFFSET, HOLE_WIDTH, HOLE_HEIGHT);
			pane.getChildren().add(h.denkaView);
		}
		
	}

	private void denkaComingOut() {
		Thread thread = new Thread(() -> {
			long time = System.currentTimeMillis();
			while(true) {
				if(System.currentTimeMillis() - time > 2000 && holes.size() > 0) {
					animatedDenka();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}	
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void animatedDenka() {
		Hole hole = holes.remove((int) (Math.random() * holes.size()));
		hole.denkaView.setVisible(true);
		
		new AnimationTimer() {
			double y = hole.denkaY, x = hole.denkaX , delta = 1;
			@Override
			public void handle(long now) {
				if(y <= hole.denkaY) {
					if(y > hole.denkaY - 30) {
						hole.denkaView.setX(x);
						hole.denkaView.setY(y);
						y -= delta;
					} else { 
						delta = -delta;
						y -= delta;
					}
				}else {
					hole.denkaView.setVisible(false);
					holes.add(hole);
					stop();
				}
			}
		}.start();	
	}

	private void drawTerraria() {
		final double HOLE_WIDTH = 150, HOLE_HEIGHT = 50;
		final int LOCAL_OFFSET = 15, TERRARIA = 5;
		double x = Math.random() > 0.5? 1: -1;
		double y = Math.random() > 0.5? 1: -1;
		
		int flowerC = 6;
		Image[] flowers = new Image[flowerC];
		for(int i = 1; i <= flowerC; i++ )
			flowers[i - 1] = new Image("terraria/flower" + i + ".png", 50, 50, true, true);
		//Reword with COS(A), SIN(A) required
		for(int i = 0; i < TERRARIA; i++) {
			Hole hole = holes.remove((int)Math.random() * holes.size());
			if(x == -1 && y == -1) {
				x = hole.x - LOCAL_OFFSET;
				y = hole.y - LOCAL_OFFSET;
			} else if(x == -1 && y == 1) {
				x = hole.x - LOCAL_OFFSET;
				y = hole.y + HOLE_HEIGHT + LOCAL_OFFSET;
			} else if(x == 1 && y == -1) {
				x = hole.x + HOLE_WIDTH + LOCAL_OFFSET;
				y = hole.y - LOCAL_OFFSET;
			} else {
				x = hole.x + HOLE_WIDTH + LOCAL_OFFSET;
				y = hole.y + HOLE_HEIGHT + LOCAL_OFFSET;
			}
			plantFlower(flowers[(int) (Math.random() * flowers.length)], x, y);
			holes.add(hole);
		}
		
	}
	private void plantFlower(Image flower, double x, double y) {
		gc.drawImage(flower, x, y);
	}
	

	private void drawGrass() {
		LinearGradient lg1 = new LinearGradient(0, 1, 0, 0,
				true, CycleMethod.NO_CYCLE, 
				new Stop[] { new Stop(0, Color.DARKGREEN), 
						new Stop(1, Color.LIGHTGREEN)});

		gc.setFill(lg1);
		gc.fillRect(0, HORIZON, WIDTH, HEIGHT);
	}

	private void drawSky() {
		final double SKY_OBJECT_WIDTH = WIDTH * 0.50, SKY_OBJECT_HEIGHT = HORIZON * 0.5;
		final int CLOUDNESS = 5;

		LinearGradient lg1 = new LinearGradient(0, 0, 0, 1,
				true, CycleMethod.NO_CYCLE, 
				new Stop[] { new Stop(0, Color.CYAN), new Stop(1, Color.WHITE)});

		gc.setFill(lg1);
		gc.fillRect(0, 0, WIDTH, HORIZON);
		gc.drawImage(new Image("sky/sun.png", SKY_OBJECT_WIDTH, SKY_OBJECT_HEIGHT, true, true), 0, 0);

		int cloudN = 5;
		Image[] clouds = new Image[cloudN];
		for(int i = 2; i <= cloudN; i++ )
			clouds[i - 1] = new Image("sky/cd" + i + ".png", SKY_OBJECT_WIDTH, SKY_OBJECT_HEIGHT, true, true);

		for(int i = 0; i < CLOUDNESS; i++) 
			makeCloud(new ImageView(clouds[(int) (Math.random() * 4)]), 
					SKY_OBJECT_WIDTH, SKY_OBJECT_HEIGHT);

	}

	private void makeCloud(ImageView cloud, double width, double height) {
		int DELAY = (int) (Math.random() * 1000);
		int startingPosX = (int) (Math.random() > 0.5? 0 - width: WIDTH);
		int startingPosY = (int) (Math.random() * HORIZON - height);

		cloud.setX(startingPosX);
		cloud.setY(startingPosY);
		pane.getChildren().add(cloud);

		new AnimationTimer() {
			final int ERROR = 10;
			double delta = startingPosX == 0? 1 * Math.random(): -1 * Math.random();
			long time = System.currentTimeMillis();
			@Override
			public void handle(long now) {
				if(System.currentTimeMillis() - time > DELAY) {
					if(cloud.getX() >= 0 - width - ERROR && cloud.getX() <= WIDTH + ERROR) 
						cloud.setX(cloud.getX() + delta);
					else {
						time = System.currentTimeMillis();
						delta *= -1;
						cloud.setX(cloud.getX() + delta);
					}
				}
			}
		}.start();
	}
	private void updateScore(int update) {
		score += update;
		scoreLabel.setText("Score: " + score);
	}
	
	class Hole {
		private final int LOCAL_OFFSETX = 30, LOCAL_OFFSETY = -50;
		private final Image denka = new Image("misc/DENKA.PNG", 200, 100, true, true);
		public ImageView denkaView = new ImageView(denka);
		public double x, y;
		public double denkaX, denkaY;

		Hole(double x, double y){
			this.x = x;
			this.y = y;
			
			denkaX = x + LOCAL_OFFSETX;
			denkaY = y + LOCAL_OFFSETY;
			
			denkaView.setX(denkaX);
			denkaView.setY(denkaY);
			denkaView.setVisible(false);
			
			denkaView.setOnMouseEntered(e -> denkaView.setEffect(new Glow(0.8)));
			denkaView.setOnMouseExited(e -> denkaView.setEffect(null));
			denkaView.setOnMouseClicked(e -> updateScore(20));
		}

		Hole(){

		}
	}
}
