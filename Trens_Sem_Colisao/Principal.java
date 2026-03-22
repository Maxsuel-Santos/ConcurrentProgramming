/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 13/03/2026
* Ultima alteracao.: 22/03/2026
* Nome.............: Principal.java
* Funcao...........: Projeto da disciplina de Programacao Concorrente com
* o objetivo de simular o percurso de dois trens por um caminho definido.
************************************************************************ */

// Importacoes necessarias
import java.util.ArrayList;
import java.util.Random;

import javafx.animation.Animation; // Esse sera o import usado para as animacoes com JavaFx
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
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
import javafx.scene.shape.VLineTo;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/* ***************************************************************
* Classe: Principal
* Funcao: Classe principal responsavel por iniciar a aplicacao
*************************************************************** */
public class Principal extends Application {

  // Velocidade padrao dos trens
  private static final double DEFAULT_SPEED = 10; 
  
  // Janela principal
  private Stage primaryStage; 
  
  // Janela da simulacao
  private Stage StageNew; 
  
  // Caixa horizontal do painel de controle
  private HBox hbox; 
  
  // Caixa vertical do painel de controle
  private VBox vbox; 
  
  // Caixa para slider e label do trem azul
  private VBox blueSliderVbox; 
  
  // Caixa para slider e label do trem vermelho
  private VBox redSliderVbox; 
  
  // Container de texto
  private VBox textContainer; 
  
  // Botao de redefinicao da simulacao
  private Button resetButton; 
  
  // Slider do trem azul
  private Slider blueSpeedSlider; 
  
  // Slider do trem vermelho
  private Slider redSpeedSlider; 
  
  // Limites da tela
  private Rectangle2D screenBounds; 
  
  // Propriedade booleana para pausar trens 1
  private BooleanProperty pauseTrains1; 
  
  // Propriedade booleana para pausar trens 2
  private BooleanProperty pauseTrains2;
  
  /* *************************************************************** 
  * Enum: Option 
  * Funcao: Representa as opcoes de direcao dos trens 
  *************************************************************** */
  private enum Option {
    OP1("MESMA DIRECAO"),
    OP2("MESMA DIRECAO INVERSA"),
    OP3("DIRECAO OPOSTA"),
    OP4("DIRECAO OPOSTA INVERSA");

    private final String title;

    /* *************************************************************** 
    * Metodo: Option 
    * Funcao: Construtor do enum Option 
    * Parametros: @param title eh o texto da opcao 
    * Retorno: nao possui retorno 
    *************************************************************** */
    Option(String title) {
      this.title = title;
    } // fim do construtor Option

    /* *************************************************************** 
    * Metodo: getTitle 
    * Funcao: Retorna o titulo da opcao 
    * Parametros: nao possui parametros 
    * Retorno: String contendo o titulo da opcao 
    *************************************************************** */
    public String getTitle() {
      return title;
    } // Fim do metodo getTitle
  } // Fim do enum Option

  
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
    primaryStage.setTitle("Simulador de Trem");

    // Obtencao do tamanho da tela do usuario
    this.screenBounds = Screen.getPrimary().getVisualBounds();

    // Criacao do texto de instrucao
    Text directionText = new Text("Selecione uma das direcoes para o trem:");
    directionText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

    // Inicializacao das propriedades de controle dos trens
    this.pauseTrains1 = new SimpleBooleanProperty(false);
    this.pauseTrains2 = new SimpleBooleanProperty(false);

    // Criacao dos botoes de direcao dos trens
    Button button1 = createStyledButton(Option.OP1);
    Button button2 = createStyledButton(Option.OP2);
    Button button3 = createStyledButton(Option.OP3);
    Button button4 = createStyledButton(Option.OP4);

    // Container horizontal para os botoes
    HBox buttonContainer = new HBox(10); 
    buttonContainer.setAlignment(Pos.CENTER); 
    buttonContainer.setPadding(new Insets(0, 20, 20, 20)); 

    // Container do texto
    VBox textContainer = new VBox(directionText);
    textContainer.setAlignment(Pos.CENTER); 
    textContainer.setPadding(new Insets(10, 0, 0, 0)); 

    // Container principal da tela
    this.vbox = new VBox(10); 
    this.vbox.setAlignment(Pos.TOP_CENTER);
    this.vbox.getChildren().addAll(textContainer, buttonContainer);

    // Criacao dos containers de cada botao com imagem: Botao 1
    VBox buttonImage1 = new VBox(2);
    buttonImage1.setAlignment(Pos.CENTER);
    buttonImage1.setPadding(new Insets(10, 0, 0, 0));

    // Criacao dos containers de cada botao com imagem: Botao 2
    VBox buttonImage2 = new VBox(2);
    buttonImage2.setAlignment(Pos.CENTER);
    buttonImage2.setPadding(new Insets(10, 0, 0, 0));

    // Criacao dos containers de cada botao com imagem: Botao 3
    VBox buttonImage3 = new VBox(2);
    buttonImage3.setAlignment(Pos.CENTER);
    buttonImage3.setPadding(new Insets(10, 0, 0, 0));

    // Criacao dos containers de cada botao com imagem: Botao 4
    VBox buttonImage4 = new VBox(2);
    buttonImage4.setAlignment(Pos.CENTER);
    buttonImage4.setPadding(new Insets(10, 0, 0, 0));

    // Carregamento das imagens das direcoes: MESMA DIRECAO
    Image image1 = new Image("/img/downdown.png");
    ImageView imageView1 = new ImageView(image1);

    // Carregamento das imagens das direcoes: MESMA DIRECAO INVERSA
    Image image2 = new Image("/img/upup.png");
    ImageView imageView2 = new ImageView(image2);

    // Carregamento das imagens das direcoes: DIRECAO OPOSTA
    Image image3 = new Image("/img/downup.png");
    ImageView imageView3 = new ImageView(image3);

    // Carregamento das imagens das direcoes: DIRECAO OPOSTA INVERSA
    Image image4 = new Image("/img/updown.png");
    ImageView imageView4 = new ImageView(image4);

    // Associacao dos botoes com suas imagens
    buttonImage1.getChildren().add(button1);
    buttonImage1.getChildren().add(imageView1);
    buttonImage2.getChildren().add(button2);
    buttonImage2.getChildren().add(imageView2);
    buttonImage3.getChildren().add(button3);
    buttonImage3.getChildren().add(imageView3);
    buttonImage4.getChildren().add(button4);
    buttonImage4.getChildren().add(imageView4);

    // Adiciona todos os botoes ao container principal
    buttonContainer.getChildren().addAll(buttonImage1, buttonImage2, buttonImage3, buttonImage4);

    // Criacao da cena principal
    Scene scene = new Scene(this.vbox);

    primaryStage.setScene(scene);
    primaryStage.setResizable(false);

    // Criacoa da janela inicial de start
    Stage firstStage = new Stage();
    firstStage.setTitle("Simulador de Trem");

    // Botao de iniciar
    Button startButton = new Button("Start");  

    // Estilizacao do botao iniciar
    startButton.setStyle(
           "-fx-background-color: #FFFFFF;"
         + "-fx-text-fill: black;"
         + "-fx-font-size: 20px;" 
         + "-fx-padding: 10px 20px;"
         + "-fx-font-weight: bold" 
    ); 

    // Container do botao start
    HBox startHbox = new HBox(10);
    startHbox.setAlignment(Pos.BOTTOM_CENTER);
    startHbox.setPadding(new Insets(0, 0, 20, 0));
    startHbox.getChildren().add(startButton);

    // Criacao da cena inicial
    Scene firstScene = new Scene(startHbox,700,400);

    // Define a imagem de fundo da tela inicial e algumas configuracoes de posicionamento
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

    // Configuracao da janela inicial
    firstStage.setScene(firstScene);
    firstStage.setResizable(false);
    firstStage.show();

    // Obtencao da altura da janela (eixo Y)
    double sceneHeight = firstStage.getHeight();

    // Calculo a posicao vertical (Y)
    double centerY = (this.screenBounds.getHeight() - sceneHeight) / 2.05;

    firstStage.setY(centerY / 4);

    // Evento do botao start
    startButton.setOnAction(e -> {
        primaryStage.show();
        primaryStage.setX(firstStage.getX());
        primaryStage.setY(centerY / 4);
        firstStage.close();
    }); // Fim do metodo startButton.setOnAction

  } // Fim do metodo start

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

    // Cria o retangulo
    Rectangle rectangle = new Rectangle(x, y, width, height);

    // Carrega a imagem
    Image img = new Image(imagePath);

    // Aplica a imagem como preenchimento
    rectangle.setFill(new ImagePattern(img));

    // Retorna o retangulo configurado
    return rectangle;
    
  } // Fim do metodo createRectangle

  /** ********************************************************************
  * Metodo: createStyledButton
  * Funcao: Cria um botao estilizado associado a uma opcao de direcao.
  * Parametros: @param text representa a opcao de direcao do botao.
  * Retorno: @return Button configurado com estilo e evento.
  ******************************************************************** */
  private Button createStyledButton(Option text) {

    // Cria o botao (Button) com o titulo da opcao
    Button button = new Button(text.getTitle());

    // Define o estilo visual do botao
    button.setStyle(
          "-fx-background-color: #000;"
        + "-fx-text-fill: white;"
        + "-fx-font-size: 16px;"
        + "-fx-font-weight: bold;"
      
    );

    // Define o comportamento ao clicar o botao
    button.setOnAction(e -> {
      // Abre a tela de simulacao com base na opcao
      openNewScreen(text);
      // Reseta velocidade do trem azul
      this.blueSpeedSlider.setValue(DEFAULT_SPEED);
      // Reseta velocidade do trem Vermelho
      this.redSpeedSlider.setValue(DEFAULT_SPEED);
      // Remove pausa do trem 1
      this.pauseTrains1.set(false);
      // Remove pausa do trem 2
      this.pauseTrains2.set(false);
    }); // Fim do metodo button.setOnAction
    return button;
    
  } // Fim do metodo createStyledButton
  
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
        this.hbox,
        this.resetButton
      );
    }
    // Criacao de uma nova janela de simulacao
    this.StageNew = new Stage();
    // Impede o redimensionamento
    this.StageNew.setResizable(false);
    // Define posicao vertical
    this.StageNew.setY(80);
    // Define posicao horizontal
    this.StageNew.setX(this.screenBounds.getWidth() / 15);

    // Define o titulo da janela de acordo com a opcao selecionada
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
    } // Fim do switch

    // Criacao do container principal da simulacao
    Pane root = new Pane();
    // Criacao da cena da simulacao
    Scene scene = new Scene(root, 500, 900);

    // Criacao do slider do trem azul
    this.blueSpeedSlider = new Slider(0, 100, DEFAULT_SPEED); 
    this.blueSpeedSlider.setMinWidth(300);
    this.blueSpeedSlider.setMaxWidth(300);
    this.blueSpeedSlider.setShowTickMarks(true);
    this.blueSpeedSlider.setShowTickLabels(true);
    // Definicao do slider do trem azul
    this.blueSpeedSlider.setStyle(
        "-fx-control-inner-background: #2196F3;"
      + "-fx-background-color: transparent;"
    );

    // Criacao do slider do trem verde
    this.redSpeedSlider = new Slider(0, 100, DEFAULT_SPEED); 
    this.redSpeedSlider.setMinWidth(300);
    this.redSpeedSlider.setMaxWidth(300);
    this.redSpeedSlider.setShowTickMarks(true);
    this.redSpeedSlider.setShowTickLabels(true);
    // Definicao do estilo do slider verde
    this.redSpeedSlider.setStyle(
        "-fx-control-inner-background: #58BF3B;"
      + "-fx-background-color: transparent;"
    );

    // Carregamento da imagem de fundo da simulacao
    Image background = new Image("/img/background.png");
    
    // Configuracao da imagem de fundo
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

    // Aplicacao do fundo ao container principal
    root.setBackground(new Background(backgroundImg));

    // Criacao do botao reset
    this.resetButton = new Button("RESET");
    // Definicao do estilo do botao reset
    this.resetButton.setStyle(
          "-fx-background-color: #F44336;"
        + "-fx-text-fill: white;"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 16px;"
    );

    // Criacao do texto do painel de controle
    Text painelText = new Text("PAINEL DE CONTROLE");
    painelText.setFont(Font.font(
        "Arial",
        FontWeight.BOLD,
        25
      )
    );

    // Container do texto do painel
    this.textContainer = new VBox(painelText);
    this.textContainer.setAlignment(Pos.CENTER); 
    this.textContainer.setPadding(new Insets(10, 0, 0, 0)); 

    // Ajuste da altura da janela principal
    this.primaryStage.setHeight(440);

    // Adiciona o painel de texto na interface principal
    this.vbox.getChildren().add(this.textContainer);

    // Criacao dos textos dos sliders
    Text blueTrainLabel = new Text("Velocidade do Trem Azul");
    blueTrainLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
     
    Text redTrainLabel = new Text("Velocidade do Trem Verde");
    redTrainLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16)); 

    // Container do slider azul
    this.blueSliderVbox = new VBox(10);
    this.blueSliderVbox.setAlignment(Pos.CENTER);
    this.blueSliderVbox.setPadding(new Insets(10, 0, 0, 0));
    this.blueSliderVbox.getChildren().addAll(blueTrainLabel, this.blueSpeedSlider);

    // Contrainer do slider verde
    this.redSliderVbox = new VBox(10);
    this.redSliderVbox.setAlignment(Pos.CENTER);
    this.redSliderVbox.setPadding(new Insets(10, 0, 0, 0));
    this.redSliderVbox.getChildren().addAll(redTrainLabel, this.redSpeedSlider);

    // Criacao do container horizontal que ira armazenar os controlles de velocidade
    this.hbox = new HBox(10);
    // Centraliza os elementos dentro do HBox
    this.hbox.setAlignment(Pos.CENTER); 

    // Adiciona os containers dos sliders ao HBox
    this.hbox.getChildren().add(this.blueSliderVbox);
    this.hbox.getChildren().add(this.redSliderVbox);

    // Adiciona o HBox e o botao reset ao painel principal
    this.vbox.getChildren().add(this.hbox);
    this.vbox.getChildren().add(this.resetButton);

    // Criacao dos retangulos que representam os trens 
    Rectangle blueTrain = createRectangle(-200, -200, 90, 30, "/img/bluetrain.png");
    Rectangle redTrain = createRectangle(-200, -200, 90, 30, "/img/redtrain.png");

    // Criacao dos retangulos que representam os carros
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

    // Lista que ira armazenar todos os carros da simulacao
    ArrayList<Rectangle> carros = new ArrayList<Rectangle>();

    // Vetor que armazena as duracoes das animacoes de cada carro
    Duration[] durations = new Duration[20];

    // Vetor auxiliar contendo todos os carros criados
    Rectangle[] carrosArray = {
        carro1, carro2, carro3, carro4, carro5,
        carro6, carro7, carro8, carro9, carro10,
        carro11, carro12, carro13, carro14, carro15,
        carro16, carro17, carro18, carro19, carro20,
        carro21, carro22, carro23, carro24, carro25,
        carro26, carro27, carro28, carro29, carro30,
        carro31, carro32, carro33, carro34, carro35, carro36
    };

    // Loop responsavel por adicionar os carros na lista e definir suas ruracoes
    for (int i = 0; i < 20; i++) {
      // Adiciona o carro na lista
      carros.add(carrosArray[i]);
      // Define a duracao da animacao
      // A cada 6 carros, um deles tera duracao diferente
      durations[i] = Duration.seconds(i % 6 == 0 ? 30 : 29); 
    } // Fim do loop for

    // Define a cena da tela da simulacao
    this.StageNew.setScene(scene);
    // Exibe a janela da simulacao
    this.StageNew.show();

    // Criacao do caminho que representa o trajeto de ida do trem
    Path pathIda = new Path(
        new MoveTo(50, -50),
        new VLineTo(100), 
        new CubicCurveTo(50, 160, 100, 160, 100, 200),
        new VLineTo(280), 
        new CubicCurveTo(100, 340, 150, 320, 150, 400),
        new VLineTo(500),
        new CubicCurveTo(150, 560, 100, 560, 100, 600),
        new VLineTo(680), 
        new CubicCurveTo(100, 740, 50, 720, 50, 800),
        new VLineTo(950), 
        new MoveTo(50, 800)
    ); // Fim do pathIda

    // Criacao do caminho que representa o trajeto de volta do trem
    Path pathVolta = new Path(
        new MoveTo(50, 950),
        new VLineTo(800),
        new CubicCurveTo(50, 720, 100, 740, 100, 680),
        new VLineTo(600),
        new CubicCurveTo(100, 560, 150, 560, 150, 500),
        new VLineTo(400),
        new CubicCurveTo(150, 320, 100, 340, 100, 280),
        new VLineTo(200),
        new CubicCurveTo(100, 160, 50, 160, 50, 100),
        new VLineTo(-50),
        new MoveTo(50, -50)
    ); // Fim do pathVolta

    // Criacao do caminho utilizado para o movimento dos carros
    Path pathCarros = new Path(
        new MoveTo(465, 950),
        new VLineTo(-50)
    ); // Fim do pathCarros

    // Criacao de um objeto Random utilizado para selecionar carros de forma pseudo-aleatoria
    Random random = new Random();

    // Criacao de uma Timeline responsavel por controlar o surgimento dos carros
    Timeline timeline = new Timeline(
        // KeyFrame define uma acao que sera executada a cada intervalo de tempo
        new KeyFrame(Duration.seconds(3.5), event -> {
          // Seleciona um indice aleatorio da lista de carros
          int newRandomIndex = random.nextInt(carros.size());
          // Seleciona o carro correspondente ao indice gerado
          Rectangle newCar = carros.get(newRandomIndex);

          // Verifica se o carro ainda nao esta presente na tela
          if (!root.getChildren().contains(newCar)) {
            // Define a posicao inicial do carro
            newCar.setX(465);
            newCar.setY(950);
            // Adiciona o carro ao container principal da simulacao
            root.getChildren().add(newCar);

            // Cria a animacao de movimento do carro ao longo do caminho
            PathTransition pathTransitionCarro = new PathTransition(
                durations[newRandomIndex],
                pathCarros,
                newCar
              );
            // Define o interpolador linear para movimento constante
            pathTransitionCarro.setInterpolator(Interpolator.LINEAR);
            // Inicia a animacao do carro
            pathTransitionCarro.play();

            // Remove o carro da tela quando a animacao terminar
            pathTransitionCarro.setOnFinished(event2 -> {
              root.getChildren().remove(newCar);
            }); // Fim do metodo pathTransitionCarro.setOnFinished
          } // Fim do if
        })); // Fim do new Timeline

    // Define que a Timeline executara indefinidamente
    timeline.setCycleCount(Animation.INDEFINITE);
    // Inicia a timeline
    timeline.play();

    // Criacao de um novo caminho que sera o espelhamento do trajeto de ida
    Path mirroredPathIda = new Path();

    // Percorre todos os elementos do caminho original
    for (PathElement element : pathIda.getElements()) {
      // Verifica se o elemento eh do tipo MoveTo
      if (element instanceof MoveTo) {
        mirroredPathIda.getElements().add(new MoveTo(
          2 * (100 - ((MoveTo) element).getX()) + ((MoveTo) element).getX(),
          ((MoveTo) element).getY()));
      } // Fim do if 
      // Verifica se o elemento eh do tipo LineTo
      else if (element instanceof LineTo) {
        mirroredPathIda.getElements().add(new LineTo(
          2 * (100 - ((LineTo) element).getX()) + ((LineTo) element).getX(),
          ((LineTo) element).getY()));
      } // Fim do else if 
      // Verifica se o elemento eh do tipo VLineTo
      else if (element instanceof VLineTo) {
        mirroredPathIda.getElements().add(new VLineTo(((VLineTo) element).getY()));
      }// Fim do else if
      // Verifica se o elemento eh do tipo CubicCurveTo
      else if (element instanceof CubicCurveTo) {
        CubicCurveTo cubicCurve = (CubicCurveTo) element;
        mirroredPathIda.getElements().add(new CubicCurveTo(
          2 * (100 - cubicCurve.getControlX1()) + cubicCurve.getControlX1(),
          cubicCurve.getControlY1(),
          2 * (100 - cubicCurve.getControlX2()) + cubicCurve.getControlX2(),
          cubicCurve.getControlY2(),
          2 * (100 - cubicCurve.getX()) + cubicCurve.getX(),
          cubicCurve.getY()));
      } // Fim do else if
    } // Fim do for

    // Criacao do caminho espelhado do trajeto de volta 
    Path mirroredPathVolta = new Path();

    // Percorre os elementos do caminho original de volta
    for (PathElement element : pathVolta.getElements()) {
      // Verifica se o elemento eh do tipo MoveTo
      if (element instanceof MoveTo) {
        mirroredPathVolta.getElements().add(new MoveTo(
          2 * (100 - ((MoveTo) element).getX()) + ((MoveTo) element).getX(),
          ((MoveTo) element).getY()));
      } // Fim do if
      // Verifica se o elemento eh do tipo LineTo
      else if (element instanceof LineTo) {
        mirroredPathVolta.getElements().add(new LineTo(
          2 * (100 - ((LineTo) element).getX()) + ((LineTo) element).getX(),
          ((LineTo) element).getY()));
      } // Fim do else if
      // Verifica se o elemento eh do tipo VLineTo
      else if (element instanceof VLineTo) {
        mirroredPathVolta.getElements().add(new VLineTo(((VLineTo) element).getY()));
      } // Fim do else if 
      // Verifica se o elemento eh do tipo CubicCurveTo
      else if (element instanceof CubicCurveTo) {
        CubicCurveTo cubicCurve = (CubicCurveTo) element;
        mirroredPathVolta.getElements().add(new CubicCurveTo(
          2 * (100 - cubicCurve.getControlX1()) + cubicCurve.getControlX1(),
          cubicCurve.getControlY1(),
          2 * (100 - cubicCurve.getControlX2()) + cubicCurve.getControlX2(),
          cubicCurve.getControlY2(),
          2 * (100 - cubicCurve.getX()) + cubicCurve.getX(),
          cubicCurve.getY()));
      } // Fim do else if
    } // Fim do for

    // Define cor e espessura do caminho de ida
    pathIda.setStroke(Color.BLACK);
    pathIda.setStrokeWidth(2);

    // Define cor e espessura do caminho espelhado
    mirroredPathIda.setStroke(Color.BLACK);
    mirroredPathIda.setStrokeWidth(2);

    // Adiciona os trilhos na tela
    root.getChildren().add(pathIda);
    root.getChildren().add(mirroredPathIda);

    // Adiciona os trens na tela
    root.getChildren().add(blueTrain);
    root.getChildren().add(redTrain);

    // Criacao das animacoes de movimento dos trens
    PathTransition pathTransition1 = new PathTransition();
    PathTransition pathTransition2 = new PathTransition();

    // Define o comportamento do botao reset
    this.resetButton.setOnAction(e -> {
      // Redefine a velocidade dos sliders para o valor padrao
      this.blueSpeedSlider.setValue(DEFAULT_SPEED);
      this.redSpeedSlider.setValue(DEFAULT_SPEED);

      // Reinicia a animacao do trem azul
      pathTransition1.stop();
      pathTransition1.play();

      // Reinicia a animacao do trem verde
      pathTransition2.stop();
      pathTransition2.play();
    }); // Fim do metodo resetButton.setOnAction

    // propriedade utilizada para calcular a taxa de velocidade do trem azul
    DoubleProperty dividedRateProperty = new SimpleDoubleProperty();
    // vincula a velocidade do slider azul dividida por 30
    dividedRateProperty.bind(Bindings.divide(this.blueSpeedSlider.valueProperty(), 30.0));
    // aplica a taxa de velocidade na animacao do trem azul
    pathTransition1.rateProperty().bind(dividedRateProperty);
    // define qual objeto sera animado
    pathTransition1.setNode(blueTrain);
    // define a duracao da animacao
    pathTransition1.setDuration(Duration.seconds(4));
    // define que a animacao executara apenas uma vez
    pathTransition1.setCycleCount(1);
    // faz o trem rotacionar de acordo com a direcao do caminho
    pathTransition1.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
    // define interpolacao linear para movimento constante
    pathTransition1.setInterpolator(Interpolator.LINEAR);

    // propriedade utilizada para calcular a taxa de velocidade do trem vermelho
    DoubleProperty dividedRateProperty2 = new SimpleDoubleProperty();
    // vincula a velocidade do slider vermelho dividida por 30
    dividedRateProperty2.bind(Bindings.divide(this.redSpeedSlider.valueProperty(), 30.0));
    // aplica a taxa de velocidade na animacao do trem vermelho
    pathTransition2.rateProperty().bind(dividedRateProperty2);
    // define qual objeto sera animado
    pathTransition2.setNode(redTrain);
    // define a duracao da animacao
    pathTransition2.setDuration(Duration.seconds(4));
    // define que a animacao executara apenas uma vez
    pathTransition2.setCycleCount(1);
    // faz o trem rotacionar conforme a direcao do caminho
    pathTransition2.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
    // define interpolacao linear
    pathTransition2.setInterpolator(Interpolator.LINEAR);
    // quando a animacao do trem azul terminar ela reinicia
    pathTransition1.setOnFinished(event -> {
      pathTransition1.play(); 
    }); // Fim do metodo pathTransition1.setOnFinished

    // quando a animacao do trem vermelho terminar ela reinicia
    pathTransition2.setOnFinished(event -> {
      pathTransition2.play(); 
    }); // Fim do metodo pathTransition2.setOnFinished

    // define o caminho que cada trem ira seguir de acordo com a opcao selecionada
    switch (message) {
      // trens seguem na MESMA DIRECAO
      case OP1:
        pathTransition1.setPath(pathIda);
        pathTransition1.play();

        pathTransition2.setPath(mirroredPathIda);
        pathTransition2.play();
        break;
      // trens seguem na MESMA DIRECAO INVERSA
      case OP2:
        pathTransition1.setPath(pathVolta);
        pathTransition1.play();

        pathTransition2.setPath(mirroredPathVolta);
        pathTransition2.play();
        break;
      // trens seguem em DIRECOES OPOSTAS
      case OP3:
        pathTransition1.setPath(pathIda);
        pathTransition1.play();

        pathTransition2.setPath(mirroredPathVolta);
        pathTransition2.play();
        break;
      // trens seguem em DIRECOES OPOSTAS INVERSAS
      case OP4:
        pathTransition1.setPath(mirroredPathIda);
        pathTransition1.play();

        pathTransition2.setPath(pathVolta);
        pathTransition2.play();
        break;
      default:
        break;
    } // Fim do switch
  } // Fim do metodo openNewScreen

  /** ********************************************************************
  * Metodo: main
  * Funcao: Metodo principal por iniciar a execucao do programa
  * Parametros: @param args representa os argumentos passados pela linha de comando.
  * Retorno: @return void
  ******************************************************************** */
  public static void main(String[] args) {
    launch(args);
  } // Fim do metodo main
} // Fim da classe MainApp
