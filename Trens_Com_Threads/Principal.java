/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 15/04/2026
* Ultima alteracao.: 26/04/2026
* Nome.............: Principal.java
* Funcao...........: Classe principal responsavel por iniciar a aplicacao
*                    e orquestrar a simulacao dos trens. Apresenta um
*                    menu de direcao (4 opcoes) e um menu de exclusao
*                    mutua (4 opcoes: sem controle, variavel de
*                    travamento, estrita alternancia, Peterson).
*                    Ao trocar de opcao os trens reiniciam na posicao
*                    original conforme exigido pelo professor.
************************************************************************ */

import java.util.ArrayList;
import java.util.Random;
import util.Option;
import util.SyncOption;
import util.Constantes;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
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
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
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
import sync.EstritaAlternancia;
import sync.SolucaoPeterson;
import sync.VariavelDeTravamento;
import trains.TremDaDireita;
import trains.TremDaEsquerda;

public class Principal extends Application {

  private Stage primaryStage;
  private Stage stageNew;
  private HBox hbox;
  private VBox vbox;
  private VBox blueSliderVbox;
  private VBox greenSliderVbox;
  private VBox textContainer;
  private Button resetButton;
  private Slider blueSpeedSlider;
  private Slider greenSpeedSlider;
  private Rectangle2D screenBounds;

  // Threads dos trens
  private TremDaEsquerda Te;
  private TremDaDireita Td;

  // Objetos de exclusao mutua (apenas um ativo por vez)
  private VariavelDeTravamento VT;
  private EstritaAlternancia EA;
  private SolucaoPeterson peterson;

  // Referencia ao container do menu de anti-colisao para remocao correta
  private VBox syncContainer;

  /** ********************************************************************
  * Metodo: start
  * Funcao: Metodo de inicializacao da aplicacao JavaFX.
  * Parametros: @param primaryStage eh o Stage principal da aplicacao,
  *             que representa a janela principal.
  * Retorno: @return void
  ******************************************************************** */
  @Override
  public void start(Stage primaryStage) {

    // Cria um AudioClip carregando o arquivo de audio localizado na pasta /sound.
    AudioClip themeSound = new AudioClip(
      getClass().getResource("/sound/theme.wav").toExternalForm()
    );
    
    themeSound.setCycleCount(AudioClip.INDEFINITE);
    themeSound.play();

    this.primaryStage = primaryStage;
    primaryStage.setTitle("MARLOSUEL SIMULATOR");
    this.screenBounds = Screen.getPrimary().getVisualBounds();

    Text directionText = new Text("SELECIONE UMA DAS DIRECOES PARA O TREM:");
    directionText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

    Button button1 = createStyledButton(Option.OP1);
    Button button2 = createStyledButton(Option.OP2);
    Button button3 = createStyledButton(Option.OP3);
    Button button4 = createStyledButton(Option.OP4);

    HBox buttonContainer = new HBox(10);
    buttonContainer.setAlignment(Pos.CENTER);
    buttonContainer.setPadding(new Insets(0, 20, 20, 20));

    this.textContainer = new VBox(directionText);
    this.textContainer.setAlignment(Pos.CENTER);
    this.textContainer.setPadding(new Insets(10, 0, 0, 0));

    this.vbox = new VBox(10);
    this.vbox.setAlignment(Pos.TOP_CENTER);
    this.vbox.getChildren().addAll(this.textContainer, buttonContainer);
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

    buttonImage1.getChildren().addAll(button1, imageView1);
    buttonImage2.getChildren().addAll(button2, imageView2);
    buttonImage3.getChildren().addAll(button3, imageView3);
    buttonImage4.getChildren().addAll(button4, imageView4);

    buttonContainer.getChildren().addAll(buttonImage1, buttonImage2, buttonImage3, buttonImage4);

    Scene scene = new Scene(this.vbox);
    scene.getStylesheets().add("css/style.css");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);

    // Tela de inicio (splash)
    Stage firstStage = new Stage();
    firstStage.setTitle("MARLOSUEL SIMULATOR");

    Button startButton = new Button("START");
    startButton.getStyleClass().add("start-button");

    HBox startHbox = new HBox(15);
    startHbox.setAlignment(Pos.BOTTOM_CENTER);
    startHbox.setPadding(new Insets(0, 0, 120, 0));
    startHbox.getChildren().add(startButton);

    Scene firstScene = new Scene(startHbox, 800, 540);
    firstScene.getStylesheets().add("css/style.css");

    Image background = new Image("/img/startbackground.png");
    BackgroundImage backgroundImg = new BackgroundImage(background,
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.DEFAULT,
        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
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

  } // Fim do metodo start

  /** ********************************************************************
  * Metodo: createRectangle
  * Funcao: Cria um retangulo com dimensoes especificadas e aplica uma
  * imagem como preenchimento.
  * Parametros: @param x coordenada x, @param y coordenada y,
  *             @param width largura, @param height altura,
  *             @param imagePath caminho da imagem.
  * Retorno: @return Rectangle preenchido com a imagem.
  ********************************************************************* */
  private static Rectangle createRectangle(double x, double y, double width, double height, String imagePath) {
    Rectangle rectangle = new Rectangle(x, y, width, height);
    rectangle.setFill(new ImagePattern(new Image(imagePath)));
    return rectangle;
  } // Fim do metodo createRectangle

  /** ********************************************************************
  * Metodo: createStyledButton
  * Funcao: Cria um botao estilizado para o menu de direcao dos trilhos.
  * Parametros: @param text opcao de direcao associada ao botao.
  * Retorno: @return Button configurado com classe CSS e acao.
  ******************************************************************** */
  private Button createStyledButton(Option text) {
    Button button = new Button(text.getTitle());
    button.getStyleClass().add("option-button");
    button.setOnAction(e -> {
      if (Te != null && Te.isAlive()) 
        Te.interrupt();
      if (Td != null && Td.isAlive()) 
        Td.interrupt();
      openNewScreen(text);
      this.blueSpeedSlider.setValue(Constantes.DEFAULT_SPEED);
      this.greenSpeedSlider.setValue(Constantes.DEFAULT_SPEED);
    });
    return button;
  } // Fim do metodo createStyledButton

  /** ********************************************************************
  * Metodo: createSyncButton
  * Funcao: Cria um botao estilizado para o menu de exclusao mutua.
  *         Usa a classe CSS "sync-button" (fundo preto, letra branca).
  * Parametros: @param opt opcao de sincronizacao associada ao botao.
  * Retorno: @return Button configurado com classe CSS e acao.
  ******************************************************************** */
  private Button createSyncButton(SyncOption opt) {
    Button button = new Button(opt.getTitle());
    button.getStyleClass().add("sync-button");
    button.setOnAction(e -> aplicarSincronizacao(opt));
    return button;
  } // Fim do metodo createSyncButton

  /** ********************************************************************
  * Metodo: aplicarSincronizacao
  * Funcao: Encerra o algoritmo de exclusao mutua anterior, limpa as
  *         referencias nas threads, instancia o novo algoritmo escolhido
  *         e reinicia os trens na posicao original.
  * Parametros: @param opt opcao de sincronizacao escolhida pelo usuario.
  * Retorno: @return void
  ******************************************************************** */
  private void aplicarSincronizacao(SyncOption opt) {

    // Encerra algoritmos anteriores com seguranca
    if (VT != null) { 
      VT.encerrarExclusaoMutua(); 
      VT = null; 
    }
    if (EA != null) { 
      EA.encerrarExclusaoMutua(); 
      EA = null; 
    }
    if (peterson != null) {
      peterson.sairRegiaoCritica(0);  
      peterson.sairRegiaoCritica(1);
      peterson.sairRegiaoCritica2(0); 
      peterson.sairRegiaoCritica2(1);
      peterson.encerrarExclusaoMutua();
      peterson = null;
    }

    // Remove referencias nas threads para parar o busy-wait.
    // Guarda de null necessaria: na primeira execucao os trens ainda nao existem.
    if (Te != null) {
      Te.setExclusaoMutua(null);
      Te.setEstritaAlternancia(null);
      Te.setSolucaoPeterson(null);
    }
    if (Td != null) {
      Td.setExclusaoMutua(null);
      Td.setEstritaAlternancia(null);
      Td.setSolucaoPeterson(null);
    }

    switch (opt) {
      case OP1:
        // Sem controle: trens colidem livremente
        resetButtonAction();
        break;

      case OP2:
        // Variavel de Travamento 
        VT = new VariavelDeTravamento();
        resetButtonAction();
        Te.setExclusaoMutua(VT);
        Td.setExclusaoMutua(VT);
        break;

      case OP3:
        // Estrita Alternancia
        EA = new EstritaAlternancia();
        resetButtonAction();
        Te.setEstritaAlternancia(EA);
        Td.setEstritaAlternancia(EA);
        break;

      case OP4:
        // Solucao de Peterson
        peterson = new SolucaoPeterson();
        resetButtonAction();
        Te.setSolucaoPeterson(peterson);
        Td.setSolucaoPeterson(peterson);
        break;

      default:
        break;
    }
  } // Fim do metodo aplicarSincronizacao

  /** ********************************************************************
  * Metodo: openNewScreen
  * Funcao: Abre (ou reutiliza) a janela de simulacao para a direcao
  *         escolhida. Remove corretamente os elementos do painel
  *         anterior antes de adicionar os novos, evitando duplicacoes.
  * Parametros: @param message opcao de direcao escolhida.
  * Retorno: @return void
  ******************************************************************** */
  private void openNewScreen(Option message) {

    // Remove todos os elementos dinamicos adicionados na chamada anterior.
    // O syncContainer eh guardado como campo para que possa ser removido aqui.
    if (this.stageNew != null) {
      this.vbox.getChildren().removeAll(
        this.textContainer,
        this.hbox,
        this.syncContainer,
        this.resetButton);
    } else {
      this.stageNew = new Stage();
      this.stageNew.setResizable(false);
      this.stageNew.setY(80);
      this.stageNew.setX(this.screenBounds.getWidth() / 15);
    }

    switch (message) {
      case OP1: 
        this.stageNew.setTitle("MESMA DIRECAO");          
        break;
      case OP2: 
        this.stageNew.setTitle("MESMA DIRECAO INVERSA");  
        break;
      case OP3: 
        this.stageNew.setTitle("DIRECAO OPOSTA");         
        break;
      case OP4: 
        this.stageNew.setTitle("DIRECAO OPOSTA INVERSA"); 
        break;
      default: break;
    }

    Pane root = new Pane();
    Scene scene = new Scene(root, 500, 900);

    SVGPath waterArea = new SVGPath();
    // Caminho SVG dos peixes na imagem
    waterArea.setContent(
        "M250.5 419L199 423.5L210.5 373V346.5L202.5 313.5L185.5 281V213L269.5 218.5L304 216.5L392 210.5L393.5 432.5L348 422L293.5 417L250.5 419Z"
    );

    // Define a cor de preenchimento da area de agua
    startFishSpawner(root, waterArea);
    
    // Adiciona a area de agua ao container principal da simulacao
    startBubbleSpawner(root, waterArea);
    
    // Cria o container da animacao do aviao na tela
    startAirplaneTraffic(root);

    // Sliders de velocidade
    this.blueSpeedSlider = new Slider(0, 100, Constantes.DEFAULT_SPEED);
    this.blueSpeedSlider.setMinWidth(300);
    this.blueSpeedSlider.setMaxWidth(300);
    this.blueSpeedSlider.setShowTickMarks(true);
    this.blueSpeedSlider.setShowTickLabels(true);
    this.blueSpeedSlider.getStyleClass().add("blue-speed-slider");

    this.greenSpeedSlider = new Slider(0, 100, Constantes.DEFAULT_SPEED);
    this.greenSpeedSlider.setMinWidth(300);
    this.greenSpeedSlider.setMaxWidth(300);
    this.greenSpeedSlider.setShowTickMarks(true);
    this.greenSpeedSlider.setShowTickLabels(true);
    this.greenSpeedSlider.getStyleClass().add("green-speed-slider");

    // Fundo da janela de simulacao 
    Image bg = new Image("/img/background.png");
    BackgroundImage backgroundImg = new BackgroundImage(bg,
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.DEFAULT,
        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
    root.setBackground(new Background(backgroundImg));

    // Botao RESET
    this.resetButton = new Button("RESET");
    this.resetButton.getStyleClass().add("reset-button");

    // Titulo do painel de controle 
    Text painelText = new Text("PAINEL DE CONTROLE");
    painelText.setFont(Font.font("Arial", FontWeight.BOLD, 25));

    this.textContainer = new VBox(painelText);
    this.textContainer.setAlignment(Pos.CENTER);
    this.textContainer.setPadding(new Insets(10, 0, 0, 0));

    this.primaryStage.setHeight(550);
    this.vbox.getChildren().add(this.textContainer);

    Text blueTrainLabel = new Text("VELOCIDADE DO TREM AZUL");
    blueTrainLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    Text greenTrainLabel = new Text("VELOCIDADE DO TREM VERDE");
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
    this.hbox.getChildren().addAll(this.blueSliderVbox, this.greenSliderVbox);

    // Menu de exclusao mutua: 4 botoes sync-button
    Text syncLabel = new Text("MODO DE ANTI-COLISAO:");
    syncLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    Button syncBtn1 = createSyncButton(SyncOption.OP1);
    Button syncBtn2 = createSyncButton(SyncOption.OP2);
    Button syncBtn3 = createSyncButton(SyncOption.OP3);
    Button syncBtn4 = createSyncButton(SyncOption.OP4);

    HBox syncButtonsBox = new HBox(8);
    syncButtonsBox.setAlignment(Pos.CENTER);
    syncButtonsBox.setPadding(new Insets(4, 10, 4, 10));
    syncButtonsBox.getChildren().addAll(syncBtn1, syncBtn2, syncBtn3, syncBtn4);

    // Guarda o container inteiro como campo para que o proximo
    // openNewScreen consiga remove-lo corretamente do vbox.
    this.syncContainer = new VBox(6, syncLabel, syncButtonsBox);
    this.syncContainer.setAlignment(Pos.CENTER);
    this.syncContainer.setPadding(new Insets(4, 0, 0, 0));

    // Adiciona os elementos na ordem correta — apenas UMA vez cada
    this.vbox.getChildren().add(this.hbox);
    this.vbox.getChildren().add(this.syncContainer);
    this.vbox.getChildren().add(this.resetButton);

    // Trens
    Rectangle blueTrain  = createRectangle(-200, -200, 90, 30, "/img/bluetrain.png");
    Rectangle greenTrain = createRectangle(-200, -200, 90, 30, "/img/greentrain.png");

    // Carros de rua
    Rectangle[] carrosArray = {
        createRectangle(20, 20, 40, 80,  "/img/car1.png"),
        createRectangle(20, 20, 40, 80,  "/img/car2.png"),
        createRectangle(20, 20, 40, 80,  "/img/car3.png"),
        createRectangle(20, 20, 40, 120, "/img/car4.png"),
        createRectangle(20, 20, 40, 80,  "/img/car5.png"),
        createRectangle(20, 20, 40, 60,  "/img/car6.png"),
        createRectangle(20, 20, 40, 60,  "/img/car7.png"),
        createRectangle(20, 20, 40, 80,  "/img/car8.png"),
        createRectangle(20, 20, 40, 60,  "/img/car9.png"),
        createRectangle(20, 20, 40, 60,  "/img/car10.png"),
        createRectangle(20, 20, 40, 80,  "/img/car11.png"),
        createRectangle(20, 20, 40, 90,  "/img/car12.png"),
        createRectangle(20, 20, 40, 80,  "/img/car13.png"),
        createRectangle(20, 20, 40, 90,  "/img/car14.png"),
        createRectangle(20, 20, 40, 80,  "/img/car15.png"),
        createRectangle(20, 20, 30, 90,  "/img/car16.png"),
        createRectangle(20, 20, 40, 80,  "/img/car17.png"),
        createRectangle(20, 20, 40, 60,  "/img/car18.png"),
        createRectangle(20, 20, 40, 80,  "/img/car19.png"),
        createRectangle(20, 20, 40, 80,  "/img/car20.png"),
        createRectangle(20, 20, 40, 80,  "/img/car21.png"),
        createRectangle(20, 20, 40, 80,  "/img/car22.png"),
        createRectangle(20, 20, 40, 80,  "/img/car23.png"),
        createRectangle(20, 20, 40, 80,  "/img/car24.png"),
        createRectangle(20, 20, 40, 80,  "/img/car25.png"),
        createRectangle(20, 20, 40, 80,  "/img/car26.png"),
        createRectangle(20, 20, 40, 80,  "/img/car27.png"),
        createRectangle(20, 20, 40, 80,  "/img/car28.png"),
        createRectangle(20, 20, 40, 80,  "/img/car29.png"),
        createRectangle(20, 20, 40, 80,  "/img/car30.png"),
        createRectangle(20, 20, 40, 80,  "/img/car31.png"),
        createRectangle(20, 20, 40, 80,  "/img/car32.png"),
        createRectangle(20, 20, 40, 80,  "/img/car33.png"),
        createRectangle(20, 20, 40, 80,  "/img/car34.png"),
        createRectangle(20, 20, 40, 80,  "/img/car35.png"),
        createRectangle(20, 20, 40, 80,  "/img/car36.png")
    };

    ArrayList<Rectangle> carros = new ArrayList<>();
    Duration[] durations = new Duration[36];
    for (int i = 0; i < 36; i++) {
      carros.add(carrosArray[i]);
      durations[i] = Duration.seconds(i % 6 == 0 ? 30 : 29);
    }

    this.stageNew.setScene(scene);
    this.stageNew.show();

    // Caminhos dos trens com Path
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
        new MoveTo(50, 800));

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
        new VLineTo(-50));

    Path pathCarros = new Path(new MoveTo(465, 950), new VLineTo(-50));

    Random random = new Random();
    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3.5), event -> {
      int idx = random.nextInt(carros.size());
      Rectangle newCar = carros.get(idx);
      if (!root.getChildren().contains(newCar)) {
        newCar.setX(465);
        newCar.setY(950);
        root.getChildren().add(newCar);
        PathTransition ptCar = new PathTransition(durations[idx], pathCarros, newCar);
        ptCar.setInterpolator(Interpolator.LINEAR);
        ptCar.play();
        ptCar.setOnFinished(e -> root.getChildren().remove(newCar));
      }
    }));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();

    // Espelha o caminho para o trem verde
    Path mirroGreenPathIda   = espelharCaminho(pathIda, 100);
    Path mirroGreenPathVolta = espelharCaminho(pathVolta, 100);

    pathIda.setStroke(Color.TRANSPARENT);
    pathIda.setStrokeWidth(0);
    mirroGreenPathIda.setStroke(Color.TRANSPARENT);
    mirroGreenPathIda.setStrokeWidth(0);

    root.getChildren().addAll(pathIda, mirroGreenPathIda, blueTrain, greenTrain);

    // Cria as threads dos trens
    Te = new TremDaEsquerda(blueTrain, blueSpeedSlider);
    Td = new TremDaDireita(greenTrain, greenSpeedSlider);

    // Define caminho e inicia de acordo com a direcao
    switch (message) {
      case OP1:
        Te.setPath(pathIda);            
        Te.start();
        Td.setPath(mirroGreenPathIda);  
        Td.start();
        break;
      case OP2:
        Te.setPath(pathVolta);           
        Te.start();
        Td.setPath(mirroGreenPathVolta); 
        Td.start();
        break;
      case OP3:
        Te.setPath(pathIda);             
        Te.start();
        Td.setPath(mirroGreenPathVolta); 
        Td.start();
        break;
      case OP4:
        Te.setPath(mirroGreenPathIda);  
        Te.start();
        Td.setPath(pathVolta);          
        Td.start();
        break;
      default:
        break;
    }

    this.resetButton.setOnAction(e -> resetButtonAction());

  } // Fim do metodo openNewScreen

  /** ********************************************************************
  * Metodo: espelharCaminho
  * Funcao: Cria um novo Path espelhado horizontalmente em relacao ao
  *         eixo x = eixoX, para gerar o trilho do segundo trem.
  * Parametros: @param original caminho original a espelhar
  *             @param eixoX    valor do eixo de espelhamento
  * Retorno: @return Path espelhado
  ******************************************************************** */
  private Path espelharCaminho(Path original, double eixoX) {
    Path espelho = new Path();
    for (PathElement el : original.getElements()) {
      if (el instanceof MoveTo) {
        MoveTo m = (MoveTo) el;
        espelho.getElements().add(new MoveTo(2 * eixoX - m.getX(), m.getY()));
      } else if (el instanceof LineTo) {
        LineTo l = (LineTo) el;
        espelho.getElements().add(new LineTo(2 * eixoX - l.getX(), l.getY()));
      } else if (el instanceof VLineTo) {
        espelho.getElements().add(new VLineTo(((VLineTo) el).getY()));
      } else if (el instanceof CubicCurveTo) {
        CubicCurveTo c = (CubicCurveTo) el;
        espelho.getElements().add(new CubicCurveTo(
            2 * eixoX - c.getControlX1(), c.getControlY1(),
            2 * eixoX - c.getControlX2(), c.getControlY2(),
            2 * eixoX - c.getX(),          c.getY()));
      }
    }
    return espelho;
  } // Fim do metodo espelharCaminho

  /** ********************************************************************
  * Metodo: resetButtonAction
  * Funcao: Para e reinicia as animacoes dos dois trens, redefinindo
  * as velocidades para o valor padrao.
  * Parametros: nao possui
  * Retorno: @return void
  ******************************************************************** */
  private void resetButtonAction() {
    this.blueSpeedSlider.setValue(Constantes.DEFAULT_SPEED);
    this.greenSpeedSlider.setValue(Constantes.DEFAULT_SPEED);
    // Para e reinicia somente a animacao; o algoritmo de exclusao mutua
    // continua ativo sem alteracao de estado.
    Te.stoptrain();
    Td.stoptrain();
    Te.play();
    Td.play();
  } // Fim do metodo resetButtonAction

  /** ********************************************************************
  * Metodo: randomPointInWater
  * Funcao: Gera um ponto aleatorio dentro da area de agua
  * Parametros: @param waterArea eh o caminho SVG que representa a area de agua
  * Retorno: @return um array com as coordenadas (x, y) do ponto aleatorio dentro da area de agua
  ********************************************************************* */
  private double[] randomPointInWater(SVGPath waterArea) {

    Random random = new Random();

    while (true) {

      // Gera coordenadas x e y aleatorias dentro dos limites aproximados da area de agua
      double x = 150 + random.nextDouble() * 260;
      double y = 200 + random.nextDouble() * 240;

      // Verifica se o ponto gerado esta dentro da area de agua usando o metodo contains do SVGPath
      if (waterArea.contains(x, y)) {
        return new double[] {x, y};
      }

    }
  
  } // Fim do metodo randomPointInWater

  /** ********************************************************************
  * Metodo: spawnJumpingFish
  * Funcao: Cria um peixe que salta da agua em uma posicao aleatoria dentro 
  * da area de agua e executa uma animacao de salto, criando um efeito visual 
  * de respingo e ondulacao na agua.
  * Parametros: @param root e o container principal onde o peixe sera adicionado, 
  *             @param waterArea e o caminho SVG que representa a area de agua
  * Retorno: @return void
  ********************************************************************* */
  private void spawnJumpingFish(Pane root, SVGPath waterArea) {

    Random random = new Random();

    // Array de strings contendo os caminhos para as imagens dos peixes
    String[] fishImages = {
      "/img/fish1.png",
      "/img/fish2.png",
      "/img/fish3.png",
      "/img/fish4.png",
      "/img/fish5.png",
      "/img/fish6.png",
      "/img/fish7.png",
      "/img/fish8.png",
      "/img/fish9.png",
      "/img/fish10.png",
      "/img/fish11.png",
      "/img/fish12.png",
      "/img/fish13.png",
      "/img/fish14.png",
      "/img/fish15.png",
    };

    ImageView fish = new ImageView(
      new Image(fishImages[random.nextInt(fishImages.length)])
    );

    fish.setFitWidth(28);
    fish.setPreserveRatio(true);

    double[] pos = randomPointInWater(waterArea);

    fish.setLayoutX(pos[0]);
    fish.setLayoutY(pos[1]);

    root.getChildren().add(fish);

    animateJumpFish(root, fish);
  
  } // Fim do metodo spawnJumpingFish

  /** ********************************************************************
  * Metodo: animateJumpFish
  * Funcao: Executa a animacao de salto do peixe, que inclui um movimento
  * de subida e descida, uma rotacao para simular o movimento do peixe no ar, 
  * e efeitos visuais de respingo e ondulacao na agua quando o peixe retorna para a agua.
  * Parametros: @param root eh o container principal onde os efeitos visuais serao adicionados,
  *             @param fish e o ImageView do peixe que sera animado
  * Retorno: @return void
  ********************************************************************* */
  private void animateJumpFish(Pane root, ImageView fish) {

    // Cria uma animacao de translacao para simular o movimento de salto do peixe
    TranslateTransition jump = new TranslateTransition(
      Duration.seconds(0.8),
      fish
    ); 

    jump.setByY(-40);
    jump.setAutoReverse(true);
    jump.setCycleCount(2);

    RotateTransition wiggle = new RotateTransition(
      Duration.seconds(0.2),
      fish
    );

    wiggle.setByAngle(25);
    wiggle.setAutoReverse(true);
    wiggle.setCycleCount(6);

    ParallelTransition animation = new ParallelTransition(
      fish,
      jump,
      wiggle
    );

    animation.setOnFinished(e -> {

      createSplash(root, fish.getLayoutX(), fish.getLayoutY());
      createRipple(root, fish.getLayoutX(), fish.getLayoutY());

      root.getChildren().remove(fish);
    });

    animation.play();
  
  } // Fim do metodo animateJumpFish

  /** ********************************************************************
  * Metodo: createSplash
  * Funcao: Cria um efeito visual de respingo na agua quando o peixe retorna
  * para a agua apos o salto, simulando o impacto do peixe na agua com pequenos
  * circulos que se movem para fora do ponto de impacto e desaparecem gradualmente
  * Parametros: @param root eh o container principal onde os elementos do respingo serao adicionados,
  *             @param x e a coordenada x do ponto de impacto do peixe retornando para a agua, 
  *             @param y e a coordenada y do ponto de impacto do peixe retornando para a agua
  * Retorno: @return void
  ********************************************************************* */
  private void createSplash(Pane root, double x, double y) {

    for (int i = 0; i < 4; i++) {

      // Cria um circulo para representar uma gota de agua do respingo, onde cada circulo tem um tamanho pequeno e uma cor branca para simular a aparência de gotas de agua sendo lancadas para fora do ponto de impacto
      Circle drop = new Circle(2, Color.WHITE);

      drop.setLayoutX(x);
      drop.setLayoutY(y);

      root.getChildren().add(drop);

      TranslateTransition move = new TranslateTransition(
        Duration.seconds(0.6),
        drop
      );

      move.setByX((Math.random() - 0.5) * 30);
      move.setByY(-10 - Math.random() * 20);

      FadeTransition fade = new FadeTransition(
        Duration.seconds(0.6),
        drop
      );

      fade.setFromValue(1);
      fade.setToValue(0);

      ParallelTransition splash = new ParallelTransition(
        drop,
        move,
        fade
      );

      splash.setOnFinished(e -> root.getChildren().remove(drop));

      splash.play();
    } // Fim do loop for
  
  } // Fim do metodo createSplash

  /** ********************************************************************
  * Metodo: createRipple
  * Funcao: Cria um efeito visual de ondulacao na agua quando o peixe retorna
  * para a agua apos o salto, simulando a ondulacao causada pelo impacto do peixe
  * na agua com um circulo que se expande e desaparece gradualmente, criando 
  * um efeito visual de ondulacao na agua
  * Parametros: @param root eh o container principal onde o elemento da ondulacao sera adicionado,
  *             @param x e a coordenada x do ponto de impacto do peixe retornando para a agua, 
  *             @param y e a coordenada y do ponto de impacto do peixe retornando para a agua
  * Retorno: @return void
  ********************************************************************* */
  private void createRipple(Pane root, double x, double y) {

    Circle ripple = new Circle(3);

    ripple.setLayoutX(x);
    ripple.setLayoutY(y);

    ripple.setStroke(Color.WHITE);
    ripple.setFill(Color.TRANSPARENT);
    ripple.setStrokeWidth(2);

    root.getChildren().add(ripple);

    ScaleTransition expand = new ScaleTransition(
      Duration.seconds(1.2),
      ripple
    );

    expand.setToX(6);
    expand.setToY(6);

    FadeTransition fade = new FadeTransition(
      Duration.seconds(1.2),
      ripple
    );

    fade.setFromValue(0.8);
    fade.setToValue(0);

    ParallelTransition rippleAnim = new ParallelTransition(
      ripple,
      expand,
      fade
    );

    rippleAnim.setOnFinished(e -> root.getChildren().remove(ripple));

    rippleAnim.play();
  
  } // Fim do metodo createRipple

  /** ********************************************************************
  * Metodo: spawnBubble
  * Funcao: Cria uma bolha que surge da agua em uma posicao aleatoria dentro da 
  * area de agua e executa uma animacao de subida e desaparecimento, simulando 
  * o movimento de bolhas subindo na agua e desaparecendo gradualmente, 
  * criando um efeito visual de bolhas subindo na agua
  * Parametros: @param root e o container principal onde a bolha sera adicionada, 
  *             @param waterArea e o caminho SVG que representa a area de agua
  * Retorno: @return void
  ********************************************************************* */ 
  private void spawnBubble(Pane root, SVGPath waterArea) {

    double[] pos = randomPointInWater(waterArea);

    Circle bubble = new Circle(2);
    bubble.setLayoutX(pos[0]);
    bubble.setLayoutY(pos[1]);
    bubble.setFill(Color.WHITE);

    root.getChildren().add(bubble);

    TranslateTransition rise = new TranslateTransition(
      Duration.seconds(3),
      bubble
    );

    rise.setByY(-30);

    FadeTransition fade = new FadeTransition(
      Duration.seconds(3),
      bubble
    );

    fade.setFromValue(1);
    fade.setToValue(0);

    ParallelTransition animation = new ParallelTransition(
      bubble,
      rise,
      fade
    );

    animation.setOnFinished(e -> root.getChildren().remove(bubble));

    animation.play();
  
  } // Fim do metodo spawnBubble

  /** ********************************************************************
  * Metodo: startFishSpawner
  * Funcao: Inicia o sistema de surgimento de peixes na simulacao. O metodo
  * cria uma Timeline que dispara periodicamente a criacao de um novo peixe
  * na tela a cada 4 segundos, onde cada peixe surge em uma posicao aleatoria 
  * dentro da area de agua e executa uma animacao de salto, criando um 
  * efeito visual de peixes saltando da agua
  * Parametros: @param root eh o container principal onde os peixes serao adicionados e animados durante a simulacao,
  *             @param waterArea eh o caminho SVG que representa a area de agua, utilizado para gerar posicoes aleatorias para os peixes surgirem dentro da agua
  * Retorno: @return void
  ********************************************************************* */
  private void startFishSpawner(Pane root, SVGPath waterArea) {

    Timeline fishSpawner = new Timeline(
      new KeyFrame(Duration.seconds(4), e -> spawnJumpingFish(root, waterArea))
    );

    fishSpawner.setCycleCount(Timeline.INDEFINITE);
    fishSpawner.play();

  } // Fim do metodo startFishSpawner

  /** ********************************************************************
  * Metodo: startBubbleSpawner
  * Funcao: Inicia o sistema de surgimento de bolhas na simulacao. O metodo
  * cria uma Timeline que dispara periodicamente a criacao de uma nova bolha
  * na tela a cada 1.5 segundos, onde cada bolha surge em uma posicao aleatoria 
  * dentro da area de agua e executa uma animacao de subida e desaparecimento, 
  * criando um efeito visual de bolhas subindo na agua de forma continua e dinâmica
  * Parametros: @param root eh o container principal onde as bolhas serao adicionadas e animadas durante a simulacao,
  *             @param waterArea eh o caminho SVG que representa a area de agua, utilizado para gerar posicoes aleatorias para as bolhas surgirem dentro da agua
  * Retorno: @return void
  ********************************************************************* */
  private void startBubbleSpawner(Pane root, SVGPath waterArea) {

    Timeline bubbleSpawner = new Timeline(
      new KeyFrame(Duration.seconds(1.5), e -> spawnBubble(root, waterArea))
    );

    bubbleSpawner.setCycleCount(Timeline.INDEFINITE);
    bubbleSpawner.play();

  } // Fim do metodo startBubbleSpawner

  /** ********************************************************************
  * Metodo: spawnAirplane
  * Funcao: Cria um aviao na parte inferior da tela e executa uma animacao
  * de subida ate sair completamente da tela, simulando trafego aereo.
  * Parametros: @param root eh o Pane principal onde o aviao sera adicionado.
  * Retorno: @return void
  ********************************************************************* */
  private void spawnAirplane(Pane root) {

    // Cria o aviao fora da tela
    Rectangle airplane = createRectangle(250, 1000, 80, 80, "/img/airplane.png");

    root.getChildren().add(airplane);

    // Garante que o aviao fique na frente de outros elementos
    airplane.toFront();

    TranslateTransition fly = new TranslateTransition(Duration.seconds(8), airplane);
    fly.setFromY(0);
    fly.setToY(-1200);
    fly.setInterpolator(Interpolator.LINEAR);

    // Remove o aviao quando terminar
    fly.setOnFinished(e -> root.getChildren().remove(airplane));

    fly.play();
  
  } // Fim do metodo spawnAirplane

  /** ********************************************************************
  * Metodo: startAirplaneTraffic
  * Funcao: Inicia o sistema de trafego de avioes na simulacao. O metodo
  * cria uma Timeline que dispara periodicamente a criacao de um novo
  * aviao na tela a cada 15 segundos.
  * Parametros: @param root eh o Pane principal onde os avioes serao adicionados e animados durante a simulacao.
  * Retorno: @return void
  ********************************************************************* */
  private void startAirplaneTraffic(Pane root) {

    Timeline airplaneSpawner = new Timeline(
        new KeyFrame(Duration.seconds(15), e -> spawnAirplane(root))
    );

    airplaneSpawner.setCycleCount(Timeline.INDEFINITE);
    airplaneSpawner.play();
    
  } // Fim do metodo startAirplaneTraffic

  /** ********************************************************************
  * Metodo: main
  * Funcao: Metodo principal que inicia a execucao do programa.
  * Parametros: @param args argumentos da linha de comando.
  * Retorno: @return void
  ******************************************************************** */
  public static void main(String[] args) {
    launch(args);
  } // Fim do metodo main

} // Fim da classe Principal
