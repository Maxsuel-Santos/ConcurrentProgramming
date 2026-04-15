/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 15/04/2026
* Ultima alteracao.: 15/04/2026
* Nome.............: Principal.java
* Funcao...........: Classe principal responsavel por iniciar a aplicacao
************************************************************************ */

import java.util.ArrayList;
import java.util.Random;
import util.Option;
import util.Constantes;

import javafx.animation.Animation; 
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.VLineTo;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Principal extends Application {
  
  private Stage primaryStage; 
  
  private Stage StageNew; 
  
  private HBox hbox; 
  
  private VBox vbox; 
  
  private VBox blueSliderVbox; 
  
  private VBox greenSliderVbox; 
  
  private VBox textContainer; 
  
  private Button resetButton; 
  
  private Slider blueSpeedSlider; 
  
  private Slider greenSpeedSlider; 
  
  private Rectangle2D screenBounds; 
  
  private BooleanProperty pauseTrains1; 
  
  private BooleanProperty pauseTrains2;
  
  /** ********************************************************************
  * Metodo: start
  * Funcao: Metodo de inicializacao da aplicacao JavaFX.
  * Parametros: @param primaryStage eh o Stage principal da aplicacao, 
  * que representa a janela principal.
  * Retorno: @return void
  ******************************************************************** */
  @Override
  public void start(Stage primaryStage) {
    
    this.primaryStage = primaryStage;

    primaryStage.setTitle("MAXTRAIN SIMULATOR");

    this.screenBounds = Screen.getPrimary().getVisualBounds();

    Text directionText = new Text("SELECIONE UMA DAS DIRECOES PARA O TREM:");
    directionText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

    this.pauseTrains1 = new SimpleBooleanProperty(false);
    this.pauseTrains2 = new SimpleBooleanProperty(false);

    Button button1 = createStyledButton(Option.OP1);
    Button button2 = createStyledButton(Option.OP2);
    Button button3 = createStyledButton(Option.OP3);
    Button button4 = createStyledButton(Option.OP4);

    HBox buttonContainer = new HBox(10); 
    buttonContainer.setAlignment(Pos.CENTER); 
    buttonContainer.setPadding(new Insets(0, 20, 20, 20)); 

    VBox textContainer = new VBox(directionText);
    textContainer.setAlignment(Pos.CENTER); 
    textContainer.setPadding(new Insets(10, 0, 0, 0)); 

    this.vbox = new VBox(10); 
    this.vbox.setAlignment(Pos.TOP_CENTER);
    this.vbox.getChildren().addAll(textContainer, buttonContainer);

    vbox.getStyleClass().add("main-container");

    VBox buttonImage1 = new VBox(2);
    buttonImage1.setAlignment(Pos.CENTER);
    buttonImage1.setPadding(new Insets(10, 0, 0, 0));

    VBox buttonImage2 = new VBox(2);
    buttonImage2.setAlignment(Pos.CENTER);
    buttonImage2.setPadding(new Insets(10, 0, 0, 0));

    VBox buttonImage3 = new VBox(2);
    buttonImage3.setAlignment(Pos.CENTER);
    buttonImage3.setPadding(new Insets(10, 0, 0, 0));

    VBox buttonImage4 = new VBox(2);
    buttonImage4.setAlignment(Pos.CENTER);
    buttonImage4.setPadding(new Insets(10, 0, 0, 0));

    Image image1 = new Image("/img/downdown.png");
    ImageView imageView1 = new ImageView(image1);

    Image image2 = new Image("/img/upup.png");
    ImageView imageView2 = new ImageView(image2);

    Image image3 = new Image("/img/downup.png");
    ImageView imageView3 = new ImageView(image3);

    Image image4 = new Image("/img/updown.png");
    ImageView imageView4 = new ImageView(image4);

    buttonImage1.getChildren().add(button1);
    buttonImage1.getChildren().add(imageView1);
    buttonImage2.getChildren().add(button2);
    buttonImage2.getChildren().add(imageView2);
    buttonImage3.getChildren().add(button3);
    buttonImage3.getChildren().add(imageView3);
    buttonImage4.getChildren().add(button4);
    buttonImage4.getChildren().add(imageView4);

    buttonContainer.getChildren().addAll(buttonImage1, buttonImage2, buttonImage3, buttonImage4);

    Scene scene = new Scene(this.vbox);

    scene.getStylesheets().add("css/style.css");

    primaryStage.setScene(scene);
    primaryStage.setResizable(false);

    Stage firstStage = new Stage();
    firstStage.setTitle("MAXTRAIN SIMULATOR");

    Button startButton = new Button("START");  

    startButton.getStyleClass().add("start-button");

    HBox startHbox = new HBox(15);
    startHbox.setAlignment(Pos.BOTTOM_CENTER);
    startHbox.setPadding(new Insets(0, 0, 120, 0));
    startHbox.getChildren().add(startButton);

    Scene firstScene = new Scene(startHbox,800,540);

    firstScene.getStylesheets().add("css/style.css");

    Image background = new Image("/img/startbackground.png");
    BackgroundImage backgroundImg = new BackgroundImage(
        background,
        BackgroundRepeat.NO_REPEAT,
        BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.DEFAULT,
        new BackgroundSize(
            BackgroundSize.AUTO,
            BackgroundSize.AUTO,
            false,
            false,
            true,
            true
        )
    );

    startHbox.setBackground(new Background(backgroundImg));

    firstStage.setScene(firstScene);
    firstStage.setResizable(false);
    firstStage.show();

    double sceneHeight = firstStage.getHeight();

    double centerY = (this.screenBounds.getHeight() - sceneHeight) / 2.05;

    firstStage.setY(centerY / 4);

    startButton.setOnAction(e -> {
        primaryStage.show();
        primaryStage.setX(firstStage.getX());
        primaryStage.setY(centerY / 4);
        firstStage.close();
    }); 

  }

  /** ********************************************************************
  * Metodo: createRectangle
  * Funcao: Cria um retangulo com dimensoes especificadas e aplica uma
  * imagem como preenchimento
  * Parametros: @param x eh a coordenada horizontal do retangulo, @param y eh a 
  * coordenada vertical do retangulo, @param width eh a largura do retangulo,
  * @param height eh a altura do retangulo e @param imagePath eh o caminho da
  * imagem utilizada no preenchimento.
  * Retorno: @return Um objeto Rectangle preenchido com a imagem especificada.
  ********************************************************************* */
  private static Rectangle createRectangle(double x, double y, double width, double height, String imagePath) {

    Rectangle rectangle = new Rectangle(x, y, width, height);

    Image img = new Image(imagePath);

    rectangle.setFill(new ImagePattern(img));

    return rectangle;
    
  } 

  /** ********************************************************************
  * Metodo: createStyledButton
  * Funcao: Cria um botao estilizado associado a uma opcao de direcao.
  * Parametros: @param text representa a opcao de direcao do botao.
  * Retorno: @return Button configurado com estilo e evento.
  ******************************************************************** */
  private Button createStyledButton(Option text) {

    Button button = new Button(text.getTitle());

    button.getStyleClass().add("option-button");

    button.setOnAction(e -> {
      openNewScreen(text);
      this.blueSpeedSlider.setValue(Constantes.DEFAULT_SPEED);
      this.greenSpeedSlider.setValue(Constantes.DEFAULT_SPEED);
      this.pauseTrains1.set(false);
      this.pauseTrains2.set(false);
    }); 
    return button;
    
  } 
  
  /** ********************************************************************
  * Metodo: openNewScreen
  * Funcao: Abre uma nova janela de simulacao e configura os elementos iniciais da interface
  * Parametros: @param message representa a opcao de direcao escolhida
  * Retorno: @return void
  ******************************************************************** */
  private void openNewScreen(Option message) {
    // Se existir uma janela aberta, fecha ela e reseta os elementos da tela
    if (this.StageNew != null) {
      this.StageNew.close();
      this.vbox.getChildren().removeAll(
        this.textContainer,
        this.hbox,
        this.resetButton
      );
    }
    this.StageNew = new Stage();
    this.StageNew.setResizable(false);
    this.StageNew.setY(80);
    this.StageNew.setX(this.screenBounds.getWidth() / 15);

    switch (message) {
      case OP1:
        this.StageNew.setTitle("MESMA DIRECAO");
        break;
      case OP2:
        this.StageNew.setTitle("MESMA DIRECAO INVERSA");
        break;
      case OP3:
        this.StageNew.setTitle("DIRECAO OPOSTA");
        break;
      case OP4:
        this.StageNew.setTitle("DIRECAO OPOSTA INVERSA");
        break;
      default:
        break;
    }

    Pane root = new Pane();
    Scene scene = new Scene(root, 500, 900);

    SVGPath waterArea = new SVGPath();
    
    waterArea.setContent(
      "M250.5 419L199 423.5L210.5 373V346.5L202.5 313.5L185.5 281V213L269.5 218.5L304 216.5L392 210.5L393.5 432.5L348 422L293.5 417L250.5 419Z"
    );

    this.blueSpeedSlider = new Slider(0, 100, Constantes.DEFAULT_SPEED); 
    this.blueSpeedSlider.setMinWidth(300);
    this.blueSpeedSlider.setMaxWidth(300);
    this.blueSpeedSlider.setShowTickMarks(true);
    this.blueSpeedSlider.setShowTickLabels(true);
    blueSpeedSlider.getStyleClass().add("blue-speed-slider");

    this.greenSpeedSlider = new Slider(0, 100, Constantes.DEFAULT_SPEED); 
    this.greenSpeedSlider.setMinWidth(300);
    this.greenSpeedSlider.setMaxWidth(300);
    this.greenSpeedSlider.setShowTickMarks(true);
    this.greenSpeedSlider.setShowTickLabels(true);
    greenSpeedSlider.getStyleClass().add("green-speed-slider");
    
    Image background = new Image("/img/background.png");
    
    BackgroundImage backgroundImg = new BackgroundImage(
        background,
        BackgroundRepeat.NO_REPEAT,
        BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.DEFAULT,
        new BackgroundSize(
            BackgroundSize.AUTO,
            BackgroundSize.AUTO,
            false,
            false,
            true,
            true
        )
    );

    root.setBackground(new Background(backgroundImg));

    this.resetButton = new Button("RESET");
    resetButton.getStyleClass().add("reset-button");
    
    Text painelText = new Text("PAINEL DE CONTROLE");
    painelText.setFont(Font.font(
        "Arial",
        FontWeight.BOLD,
        25
      )
    );

    this.textContainer = new VBox(painelText);
    this.textContainer.setAlignment(Pos.CENTER); 
    this.textContainer.setPadding(new Insets(10, 0, 0, 0)); 

    this.primaryStage.setHeight(440);

    this.vbox.getChildren().add(this.textContainer);

    Text blueTrainLabel = new Text("Velocidade do Trem Azul");
    blueTrainLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
     
    Text greenTrainLabel = new Text("Velocidade do Trem Verde");
    greenTrainLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16)); 

    this.blueSliderVbox = new VBox(10);
    this.blueSliderVbox.setAlignment(Pos.CENTER);
    this.blueSliderVbox.setPadding(new Insets(10, 0, 0, 0));
    this.blueSliderVbox.getChildren().addAll(blueTrainLabel, this.blueSpeedSlider);

    this.greenSliderVbox = new VBox(10);
    this.greenSliderVbox.setAlignment(Pos.CENTER);
    this.greenSliderVbox.setPadding(new Insets(10, 0, 0, 0));
    this.greenSliderVbox.getChildren().addAll(greenTrainLabel, this.greenSpeedSlider);

    this.hbox = new HBox(10);
    this.hbox.setAlignment(Pos.CENTER); 

    this.hbox.getChildren().add(this.blueSliderVbox);
    this.hbox.getChildren().add(this.greenSliderVbox);

    this.vbox.getChildren().add(this.hbox);
    this.vbox.getChildren().add(this.resetButton);

    Rectangle blueTrain = createRectangle(-200, -200, 90, 30, "/img/bluetrain.png");
    Rectangle greenTrain = createRectangle(-200, -200, 90, 30, "/img/greentrain.png");

    Rectangle carro1 = createRectangle(20, 20, 40, 80, "/img/car1.png");
    Rectangle carro2 = createRectangle(20, 20, 40, 80, "/img/car2.png");
    Rectangle carro3 = createRectangle(20, 20, 40, 80, "/img/car3.png");
    Rectangle carro4 = createRectangle(20, 20, 40, 120, "/img/car4.png");
    Rectangle carro5 = createRectangle(20, 20, 40, 80, "/img/car5.png");
    Rectangle carro6 = createRectangle(20, 20, 40, 60, "/img/car6.png");
    Rectangle carro7 = createRectangle(20, 20, 40, 60, "/img/car7.png");
    Rectangle carro8 = createRectangle(20, 20, 40, 80, "/img/car8.png");
    Rectangle carro9 = createRectangle(20, 20, 40, 60, "/img/car9.png");
    Rectangle carro10 = createRectangle(20, 20, 40, 60, "/img/car10.png");
    Rectangle carro11 = createRectangle(20, 20, 40, 80, "/img/car11.png");
    Rectangle carro12 = createRectangle(20, 20, 40, 90, "/img/car12.png");
    Rectangle carro13 = createRectangle(20, 20, 40, 80, "/img/car13.png");
    Rectangle carro14 = createRectangle(20, 20, 40, 90, "/img/car14.png");
    Rectangle carro15 = createRectangle(20, 20, 40, 80, "/img/car15.png");
    Rectangle carro16 = createRectangle(20, 20, 30, 90, "/img/car16.png");
    Rectangle carro17 = createRectangle(20, 20, 40, 80, "/img/car17.png");
    Rectangle carro18 = createRectangle(20, 20, 40, 60, "/img/car18.png");
    Rectangle carro19 = createRectangle(20, 20, 40, 80, "/img/car19.png");
    Rectangle carro20 = createRectangle(20, 20, 40, 80, "/img/car20.png");
    Rectangle carro21 = createRectangle(20, 20, 40, 80, "/img/car21.png");
    Rectangle carro22 = createRectangle(20, 20, 40, 80, "/img/car22.png");
    Rectangle carro23 = createRectangle(20, 20, 40, 80, "/img/car23.png");
    Rectangle carro24 = createRectangle(20, 20, 40, 80, "/img/car24.png");
    Rectangle carro25 = createRectangle(20, 20, 40, 80, "/img/car25.png");
    Rectangle carro26 = createRectangle(20, 20, 40, 80, "/img/car26.png");
    Rectangle carro27 = createRectangle(20, 20, 40, 80, "/img/car27.png");
    Rectangle carro28 = createRectangle(20, 20, 40, 80, "/img/car28.png");
    Rectangle carro29 = createRectangle(20, 20, 40, 80, "/img/car29.png");
    Rectangle carro30 = createRectangle(20, 20, 40, 80, "/img/car30.png");
    Rectangle carro31 = createRectangle(20, 20, 40, 80, "/img/car31.png");
    Rectangle carro32 = createRectangle(20, 20, 40, 80, "/img/car32.png");
    Rectangle carro33 = createRectangle(20, 20, 40, 80, "/img/car33.png");
    Rectangle carro34 = createRectangle(20, 20, 40, 80, "/img/car34.png");
    Rectangle carro35 = createRectangle(20, 20, 40, 80, "/img/car35.png");
    Rectangle carro36 = createRectangle(20, 20, 40, 80, "/img/car36.png");

    ArrayList<Rectangle> carros = new ArrayList<Rectangle>();

    Duration[] durations = new Duration[36];

    Rectangle[] carrosArray = {
        carro1, carro2, carro3, carro4, 
        carro5, carro6, carro7, carro8, 
        carro9, carro10, carro11, carro12, 
        carro13, carro14, carro15, carro16, 
        carro17, carro18, carro19, carro20,
        carro21, carro22, carro23, carro24, 
        carro25, carro26, carro27, carro28, 
        carro29, carro30, carro31, carro32, 
        carro33, carro34, carro35, carro36
    };

    for (int i = 0; i < 36; i++) {
      carros.add(carrosArray[i]);
      durations[i] = Duration.seconds(i % 6 == 0 ? 30 : 29); 
    }

    this.StageNew.setScene(scene);
    this.StageNew.show();

    Path pathIda = new Path(
        new MoveTo(54, -50),
        new VLineTo(20), 
        new CubicCurveTo(50, 120, 58, 100, 100, 165),
        new VLineTo(265), 
        new CubicCurveTo(100, 280, 150, 320, 150, 410),
        new VLineTo(450),
        new CubicCurveTo(150, 530, 100, 560, 100, 600),
        new VLineTo(657), 
        new CubicCurveTo(100, 690, 50, 720, 50, 800),
        new VLineTo(950), 
        new MoveTo(50, 800)
    );

    Path pathVolta = new Path(
        new MoveTo(50, 950),
        new VLineTo(800),
        new CubicCurveTo(50, 720, 100, 690, 100, 657),
        new VLineTo(600),
        new CubicCurveTo(100, 560, 150, 530, 150, 450),
        new VLineTo(410),
        new CubicCurveTo(150, 320, 100, 280, 100, 265),
        new VLineTo(165),
        new CubicCurveTo(58, 100, 50, 120, 54, 20),
        new VLineTo(-50)
    );

    Path pathCarros = new Path(
        new MoveTo(465, 950),
        new VLineTo(-50)
    );

    Random random = new Random();

    Timeline timeline = new Timeline(
        new KeyFrame(Duration.seconds(3.5), event -> {
          int newRandomIndex = random.nextInt(carros.size());
          Rectangle newCar = carros.get(newRandomIndex);

          if (!root.getChildren().contains(newCar)) {
            newCar.setX(465);
            newCar.setY(950);
            root.getChildren().add(newCar);

            PathTransition pathTransitionCarro = new PathTransition(
                durations[newRandomIndex],
                pathCarros,
                newCar
              );
            pathTransitionCarro.setInterpolator(Interpolator.LINEAR);
            pathTransitionCarro.play();

            pathTransitionCarro.setOnFinished(event2 -> {
              root.getChildren().remove(newCar);
            });
          }
        }));

    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();

    Path mirroGreenPathIda = new Path();

    for (PathElement element : pathIda.getElements()) {
      if (element instanceof MoveTo) {
        mirroGreenPathIda.getElements().add(new MoveTo(
          2 * (100 - ((MoveTo) element).getX()) + ((MoveTo) element).getX(),
          ((MoveTo) element).getY()));
      } 
      else if (element instanceof LineTo) {
        mirroGreenPathIda.getElements().add(new LineTo(
          2 * (100 - ((LineTo) element).getX()) + ((LineTo) element).getX(),
          ((LineTo) element).getY()));
      } 
      else if (element instanceof VLineTo) {
        mirroGreenPathIda.getElements().add(new VLineTo(((VLineTo) element).getY()));
      }
      else if (element instanceof CubicCurveTo) {
        CubicCurveTo cubicCurve = (CubicCurveTo) element;
        mirroGreenPathIda.getElements().add(new CubicCurveTo(
          2 * (100 - cubicCurve.getControlX1()) + cubicCurve.getControlX1(),
          cubicCurve.getControlY1(),
          2 * (100 - cubicCurve.getControlX2()) + cubicCurve.getControlX2(),
          cubicCurve.getControlY2(),
          2 * (100 - cubicCurve.getX()) + cubicCurve.getX(),
          cubicCurve.getY()));
      }
    }

    Path mirroGreenPathVolta = new Path();

    for (PathElement element : pathVolta.getElements()) {
      if (element instanceof MoveTo) {
        mirroGreenPathVolta.getElements().add(new MoveTo(
          2 * (100 - ((MoveTo) element).getX()) + ((MoveTo) element).getX(),
          ((MoveTo) element).getY()));
      } 
      else if (element instanceof LineTo) {
        mirroGreenPathVolta.getElements().add(new LineTo(
          2 * (100 - ((LineTo) element).getX()) + ((LineTo) element).getX(),
          ((LineTo) element).getY()));
      } 
      else if (element instanceof VLineTo) {
        mirroGreenPathVolta.getElements().add(new VLineTo(((VLineTo) element).getY()));
      }  
      else if (element instanceof CubicCurveTo) {
        CubicCurveTo cubicCurve = (CubicCurveTo) element;
        mirroGreenPathVolta.getElements().add(new CubicCurveTo(
          2 * (100 - cubicCurve.getControlX1()) + cubicCurve.getControlX1(),
          cubicCurve.getControlY1(),
          2 * (100 - cubicCurve.getControlX2()) + cubicCurve.getControlX2(),
          cubicCurve.getControlY2(),
          2 * (100 - cubicCurve.getX()) + cubicCurve.getX(),
          cubicCurve.getY()));
      }
    } 

    pathIda.setStroke(Color.TRANSPARENT);
    pathIda.setStrokeWidth(0);

    mirroGreenPathIda.setStroke(Color.TRANSPARENT);
    mirroGreenPathIda.setStrokeWidth(0);

    root.getChildren().add(pathIda);
    root.getChildren().add(mirroGreenPathIda);

    root.getChildren().add(blueTrain);
    root.getChildren().add(greenTrain);

    PathTransition pathTransition1 = new PathTransition();
    PathTransition pathTransition2 = new PathTransition();

    this.resetButton.setOnAction(e -> {
      this.blueSpeedSlider.setValue(Constantes.DEFAULT_SPEED);
      this.greenSpeedSlider.setValue(Constantes.DEFAULT_SPEED);

      pathTransition1.stop();
      pathTransition1.play();

      pathTransition2.stop();
      pathTransition2.play();
    }); 

    DoubleProperty dividedRateProperty = new SimpleDoubleProperty();
    dividedRateProperty.bind(Bindings.divide(this.blueSpeedSlider.valueProperty(), 30.0));
    pathTransition1.rateProperty().bind(dividedRateProperty);
    pathTransition1.setNode(blueTrain);
    pathTransition1.setDuration(Duration.seconds(4));
    pathTransition1.setCycleCount(1);
    pathTransition1.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
    pathTransition1.setInterpolator(Interpolator.LINEAR);

    DoubleProperty dividedRateProperty2 = new SimpleDoubleProperty();
    dividedRateProperty2.bind(Bindings.divide(this.greenSpeedSlider.valueProperty(), 30.0));
    pathTransition2.rateProperty().bind(dividedRateProperty2);
    pathTransition2.setNode(greenTrain);
    pathTransition2.setDuration(Duration.seconds(4));
    pathTransition2.setCycleCount(1);
    pathTransition2.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
    pathTransition2.setInterpolator(Interpolator.LINEAR);
    pathTransition1.setOnFinished(event -> {
      pathTransition1.play(); 
    });

    pathTransition2.setOnFinished(event -> {
      pathTransition2.play(); 
    }); 

    switch (message) {
      case OP1:
        pathTransition1.setPath(pathIda);
        pathTransition1.play();

        pathTransition2.setPath(mirroGreenPathIda);
        pathTransition2.play();
        break;
      case OP2:
        pathTransition1.setPath(pathVolta);
        pathTransition1.play();

        pathTransition2.setPath(mirroGreenPathVolta);
        pathTransition2.play();
        break;
      case OP3:
        pathTransition1.setPath(pathIda);
        pathTransition1.play();

        pathTransition2.setPath(mirroGreenPathVolta);
        pathTransition2.play();
        break;
      case OP4:
        pathTransition1.setPath(mirroGreenPathIda);
        pathTransition1.play();

        pathTransition2.setPath(pathVolta);
        pathTransition2.play();
        break;
      default:
        break;
    } 
  } 



  /** ********************************************************************
  * Metodo: main
  * Funcao: Metodo principal por iniciar a execucao do programa
  * Parametros: @param args representa os argumentos passados pela linha de comando.
  * Retorno: @return void
  ******************************************************************** */
  public static void main(String[] args) {
    launch(args);
  } 
}
