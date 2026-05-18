/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 13/03/2026
* Ultima alteracao.: 26/03/2026
* Nome.............: Principal.java
* Funcao...........: Classe principal responsavel por iniciar a aplicacao
************************************************************************ */

// Importacoes necessarias
import java.util.ArrayList;
import java.util.Random;
import util.Option;
import util.Constantes;

import javafx.animation.Animation; // Esse sera o import usado para as animacoes com JavaFx
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
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
import javafx.animation.TranslateTransition;
import javafx.scene.media.AudioClip;

/* ***************************************************************
* Classe: Principal
* Funcao: Classe principal responsavel por iniciar a aplicacao
*************************************************************** */
public class Principal extends Application {
  
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
  private VBox greenSliderVbox; 
  
  // Container de texto
  private VBox textContainer; 
  
  // Botao de redefinicao da simulacao
  private Button resetButton; 
  
  // Slider do trem azul
  private Slider blueSpeedSlider; 
  
  // Slider do trem vermelho
  private Slider greenSpeedSlider; 
  
  // Limites da tela
  private Rectangle2D screenBounds; 
  
  // Propriedade booleana para pausar trens 1
  private BooleanProperty pauseTrains1; 
  
  // Propriedade booleana para pausar trens 2
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

    // Cria um AudioClip carregando o arquivo de audio localizado na pasta /sound.
    AudioClip themeSound = new AudioClip(
      // getResource() localiza o arquivo dentro do projeto 
      // toExternalForm() converte o caminho para um formato que o JavaFX consegue usar.
      getClass().getResource("/sound/theme.wav").toExternalForm()
    );
    
    // Define que o audio deve repetir infinitamente.
    // AudioClip.INDEFINITE faz com que a musica toque em loop continuo.
    themeSound.setCycleCount(AudioClip.INDEFINITE);
    // Inicia a reproducao da musica tema do simulador.
    themeSound.play();
    
    // Armazena a referência do Stage principal da aplicacao.
    // Isso permite acessar ou modificar a janela principal em outros metodos da classe.
    this.primaryStage = primaryStage;

    // Define o titulo que aparecera na barra superior da janela da aplicacao.
    primaryStage.setTitle("MAXTRAIN SIMULATOR");

    // Obtencao do tamanho da tela do usuario
    this.screenBounds = Screen.getPrimary().getVisualBounds();

    // Criacao do texto de instrucao
    Text directionText = new Text("SELECIONE UMA DAS DIRECOES PARA O TREM:");
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

    // Estilizacao do container principal da tela
    vbox.getStyleClass().add("main-container");

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

    scene.getStylesheets().add("css/style.css");

    // Configuracao da janela principal da aplicacao
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);

    // Criacoa da janela inicial de start
    Stage firstStage = new Stage();
    firstStage.setTitle("MAXTRAIN SIMULATOR");

    // Botao de iniciar
    Button startButton = new Button("START");  

    // Estilizacao do botao iniciar
    startButton.getStyleClass().add("start-button");

    // Container do botao start
    HBox startHbox = new HBox(15);
    startHbox.setAlignment(Pos.BOTTOM_CENTER);
    startHbox.setPadding(new Insets(0, 0, 120, 0));
    startHbox.getChildren().add(startButton);

    // Criacao da cena inicial
    Scene firstScene = new Scene(startHbox,800,540);

    // Adiciona o estilo css externo na cena
    firstScene.getStylesheets().add("css/style.css");

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
    button.getStyleClass().add("option-button");

    // Define o comportamento ao clicar o botao
    button.setOnAction(e -> {
      // Abre a tela de simulacao com base na opcao
      openNewScreen(text);
      // Reseta velocidade do trem azul
      this.blueSpeedSlider.setValue(Constantes.DEFAULT_SPEED);
      // Reseta velocidade do trem Vermelho
      this.greenSpeedSlider.setValue(Constantes.DEFAULT_SPEED);
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
        this.textContainer,
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

    // Cria a area de agua utilizando um SVGPath para definir a forma da area de agua
    SVGPath waterArea = new SVGPath();
    
    // Define o caminho SVG que representa a forma da area de agua. O comando "M" move o cursor para a posicao inicial, "L" desenha linhas, "V" desenha linhas verticais, "C" desenha curvas cubicas e "Z" fecha o caminho.
    waterArea.setContent(
      "M250.5 419L199 423.5L210.5 373V346.5L202.5 313.5L185.5 281V213L269.5 218.5L304 216.5L392 210.5L393.5 432.5L348 422L293.5 417L250.5 419Z"
    );
    
    // Define a cor de preenchimento da area de agua
    startFishSpawner(root, waterArea);
    
    // Adiciona a area de agua ao container principal da simulacao
    startBubbleSpawner(root, waterArea);
    
    // Cria o container da animacao do aviao na tela
    startAirplaneTraffic(root);

    // Criacao do slider do trem azul
    this.blueSpeedSlider = new Slider(0, 100, Constantes.DEFAULT_SPEED); 
    this.blueSpeedSlider.setMinWidth(300);
    this.blueSpeedSlider.setMaxWidth(300);
    this.blueSpeedSlider.setShowTickMarks(true);
    this.blueSpeedSlider.setShowTickLabels(true);
    // Definicao do slider do trem azul
    blueSpeedSlider.getStyleClass().add("blue-speed-slider");

    // Criacao do slider do trem verde
    this.greenSpeedSlider = new Slider(0, 100, Constantes.DEFAULT_SPEED); 
    this.greenSpeedSlider.setMinWidth(300);
    this.greenSpeedSlider.setMaxWidth(300);
    this.greenSpeedSlider.setShowTickMarks(true);
    this.greenSpeedSlider.setShowTickLabels(true);
    // Definicao do estilo do slider verde
    greenSpeedSlider.getStyleClass().add("green-speed-slider");
    
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
    resetButton.getStyleClass().add("reset-button");
    
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
     
    Text greenTrainLabel = new Text("Velocidade do Trem Verde");
    greenTrainLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16)); 

    // Container do slider azul
    this.blueSliderVbox = new VBox(10);
    this.blueSliderVbox.setAlignment(Pos.CENTER);
    this.blueSliderVbox.setPadding(new Insets(10, 0, 0, 0));
    this.blueSliderVbox.getChildren().addAll(blueTrainLabel, this.blueSpeedSlider);

    // Contrainer do slider verde
    this.greenSliderVbox = new VBox(10);
    this.greenSliderVbox.setAlignment(Pos.CENTER);
    this.greenSliderVbox.setPadding(new Insets(10, 0, 0, 0));
    this.greenSliderVbox.getChildren().addAll(greenTrainLabel, this.greenSpeedSlider);

    // Criacao do container horizontal que ira armazenar os controlles de velocidade
    this.hbox = new HBox(10);
    // Centraliza os elementos dentro do HBox
    this.hbox.setAlignment(Pos.CENTER); 

    // Adiciona os containers dos sliders ao HBox
    this.hbox.getChildren().add(this.blueSliderVbox);
    this.hbox.getChildren().add(this.greenSliderVbox);

    // Adiciona o HBox e o botao reset ao painel principal
    this.vbox.getChildren().add(this.hbox);
    this.vbox.getChildren().add(this.resetButton);

    // Criacao dos retangulos que representam os trens 
    Rectangle blueTrain = createRectangle(-200, -200, 90, 30, "/img/bluetrain.png");
    Rectangle greenTrain = createRectangle(-200, -200, 90, 30, "/img/greentrain.png");

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
    Duration[] durations = new Duration[36];

    // Vetor auxiliar contendo todos os carros criados
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

    // Loop responsavel por adicionar os carros na lista e definir suas ruracoes
    for (int i = 0; i < 36; i++) {
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
    ); // Fim do pathIda

    // Criacao do caminho que representa o trajeto de volta do trem
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
    Path mirroGreenPathIda = new Path();

    // Percorre todos os elementos do caminho original
    for (PathElement element : pathIda.getElements()) {
      // Verifica se o elemento eh do tipo MoveTo
      if (element instanceof MoveTo) {
        // Calcula a coordenada X espelhada e adiciona um novo MoveTo ao caminho espelhado
        mirroGreenPathIda.getElements().add(new MoveTo(
          2 * (100 - ((MoveTo) element).getX()) + ((MoveTo) element).getX(),
          ((MoveTo) element).getY()));
      } // Fim do if 
      // Verifica se o elemento eh do tipo LineTo
      else if (element instanceof LineTo) {
        mirroGreenPathIda.getElements().add(new LineTo(
          2 * (100 - ((LineTo) element).getX()) + ((LineTo) element).getX(),
          ((LineTo) element).getY()));
      } // Fim do else if 
      // Verifica se o elemento eh do tipo VLineTo
      else if (element instanceof VLineTo) {
        mirroGreenPathIda.getElements().add(new VLineTo(((VLineTo) element).getY()));
      }// Fim do else if
      // Verifica se o elemento eh do tipo CubicCurveTo
      else if (element instanceof CubicCurveTo) {
        CubicCurveTo cubicCurve = (CubicCurveTo) element;
        mirroGreenPathIda.getElements().add(new CubicCurveTo(
          2 * (100 - cubicCurve.getControlX1()) + cubicCurve.getControlX1(),
          cubicCurve.getControlY1(),
          2 * (100 - cubicCurve.getControlX2()) + cubicCurve.getControlX2(),
          cubicCurve.getControlY2(),
          2 * (100 - cubicCurve.getX()) + cubicCurve.getX(),
          cubicCurve.getY()));
      } // Fim do else if
    } // Fim do for

    // Criacao do caminho espelhado do trajeto de volta 
    Path mirroGreenPathVolta = new Path();

    // Percorre os elementos do caminho original de volta
    for (PathElement element : pathVolta.getElements()) {
      // Verifica se o elemento eh do tipo MoveTo
      if (element instanceof MoveTo) {
        mirroGreenPathVolta.getElements().add(new MoveTo(
          2 * (100 - ((MoveTo) element).getX()) + ((MoveTo) element).getX(),
          ((MoveTo) element).getY()));
      } // Fim do if
      // Verifica se o elemento eh do tipo LineTo
      else if (element instanceof LineTo) {
        mirroGreenPathVolta.getElements().add(new LineTo(
          2 * (100 - ((LineTo) element).getX()) + ((LineTo) element).getX(),
          ((LineTo) element).getY()));
      } // Fim do else if
      // Verifica se o elemento eh do tipo VLineTo
      else if (element instanceof VLineTo) {
        mirroGreenPathVolta.getElements().add(new VLineTo(((VLineTo) element).getY()));
      } // Fim do else if 
      // Verifica se o elemento eh do tipo CubicCurveTo
      else if (element instanceof CubicCurveTo) {
        CubicCurveTo cubicCurve = (CubicCurveTo) element;
        mirroGreenPathVolta.getElements().add(new CubicCurveTo(
          2 * (100 - cubicCurve.getControlX1()) + cubicCurve.getControlX1(),
          cubicCurve.getControlY1(),
          2 * (100 - cubicCurve.getControlX2()) + cubicCurve.getControlX2(),
          cubicCurve.getControlY2(),
          2 * (100 - cubicCurve.getX()) + cubicCurve.getX(),
          cubicCurve.getY()));
      } // Fim do else if
    } // Fim do for

    // Define cor e espessura do caminho de ida
    pathIda.setStroke(Color.TRANSPARENT);
    pathIda.setStrokeWidth(0);

    // Define cor e espessura do caminho espelhado
    mirroGreenPathIda.setStroke(Color.TRANSPARENT);
    mirroGreenPathIda.setStrokeWidth(0);

    // Adiciona os trilhos na tela
    root.getChildren().add(pathIda);
    root.getChildren().add(mirroGreenPathIda);

    // Adiciona os trens na tela
    root.getChildren().add(blueTrain);
    root.getChildren().add(greenTrain);

    // Criacao das animacoes de movimento dos trens
    PathTransition pathTransition1 = new PathTransition();
    PathTransition pathTransition2 = new PathTransition();

    // Define o comportamento do botao reset
    this.resetButton.setOnAction(e -> {
      // Redefine a velocidade dos sliders para o valor padrao
      this.blueSpeedSlider.setValue(Constantes.DEFAULT_SPEED);
      this.greenSpeedSlider.setValue(Constantes.DEFAULT_SPEED);

      // Reinicia a animacao do trem azul
      pathTransition1.stop();
      pathTransition1.play();

      // Reinicia a animacao do trem verde
      pathTransition2.stop();
      pathTransition2.play();
    }); // Fim do metodo resetButton.setOnAction

    // Propriedade utilizada para calcular a taxa de velocidade do trem azul
    DoubleProperty dividedRateProperty = new SimpleDoubleProperty();
    // Vincula a velocidade do slider azul dividida por 30
    dividedRateProperty.bind(Bindings.divide(this.blueSpeedSlider.valueProperty(), 30.0));
    // Aplica a taxa de velocidade na animacao do trem azul
    pathTransition1.rateProperty().bind(dividedRateProperty);
    // Define qual objeto sera animado
    pathTransition1.setNode(blueTrain);
    // Define a duracao da animacao
    pathTransition1.setDuration(Duration.seconds(4));
    // Define que a animacao executara apenas uma vez
    pathTransition1.setCycleCount(1);
    // Faz o trem rotacionar de acordo com a direcao do caminho
    pathTransition1.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
    // Define interpolacao linear para movimento constante
    pathTransition1.setInterpolator(Interpolator.LINEAR);

    // Propriedade utilizada para calcular a taxa de velocidade do trem vermelho
    DoubleProperty dividedRateProperty2 = new SimpleDoubleProperty();
    // Vincula a velocidade do slider vermelho dividida por 30
    dividedRateProperty2.bind(Bindings.divide(this.greenSpeedSlider.valueProperty(), 30.0));
    // Aplica a taxa de velocidade na animacao do trem vermelho
    pathTransition2.rateProperty().bind(dividedRateProperty2);
    // Define qual objeto sera animado
    pathTransition2.setNode(greenTrain);
    // Define a duracao da animacao
    pathTransition2.setDuration(Duration.seconds(4));
    // Define que a animacao executara apenas uma vez
    pathTransition2.setCycleCount(1);
    // Faz o trem rotacionar conforme a direcao do caminho
    pathTransition2.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
    // Define interpolacao linear
    pathTransition2.setInterpolator(Interpolator.LINEAR);
    // Quando a animacao do trem azul terminar ela reinicia
    pathTransition1.setOnFinished(event -> {
      pathTransition1.play(); 
    }); // Fim do metodo pathTransition1.setOnFinished

    // Quando a animacao do trem vermelho terminar ela reinicia
    pathTransition2.setOnFinished(event -> {
      pathTransition2.play(); 
    }); // Fim do metodo pathTransition2.setOnFinished

    // Define o caminho que cada trem ira seguir de acordo com a opcao selecionada
    switch (message) {
      // Trens seguem na MESMA DIRECAO
      case OP1:
        pathTransition1.setPath(pathIda);
        pathTransition1.play();

        pathTransition2.setPath(mirroGreenPathIda);
        pathTransition2.play();
        break;
      // Trens seguem na MESMA DIRECAO INVERSA
      case OP2:
        pathTransition1.setPath(pathVolta);
        pathTransition1.play();

        pathTransition2.setPath(mirroGreenPathVolta);
        pathTransition2.play();
        break;
      // Trens seguem em DIRECOES OPOSTAS
      case OP3:
        pathTransition1.setPath(pathIda);
        pathTransition1.play();

        pathTransition2.setPath(mirroGreenPathVolta);
        pathTransition2.play();
        break;
      // Trens seguem em DIRECOES OPOSTAS INVERSAS
      case OP4:
        pathTransition1.setPath(mirroGreenPathIda);
        pathTransition1.play();

        pathTransition2.setPath(pathVolta);
        pathTransition2.play();
        break;
      default:
        break;
    } // Fim do switch
  } // Fim do metodo openNewScreen

  /** ********************************************************************
  * Metodo: randomPointInWater
  * Funcao: Gera um ponto aleatorio dentro da area de agua
  * Parametros: @param waterArea eh o caminho SVG que representa a area de agua
  * Retorno: @return um array com as coordenadas (x, y) do ponto aleatorio dentro da area de agua
  ********************************************************************* */
  private double[] randomPointInWater(SVGPath waterArea) {

    // Cria um objeto Random para gerar numeros aleatorios
    Random random = new Random();

    // Loop infinito que continua ate encontrar um ponto dentro da area de agua
    while (true) {

      // Gera coordenadas x e y aleatorias dentro dos limites aproximados da area de agua
      double x = 150 + random.nextDouble() * 260;
      double y = 200 + random.nextDouble() * 240;

      // Verifica se o ponto gerado esta dentro da area de agua usando o metodo contains do SVGPath
      if (waterArea.contains(x, y)) {
        return new double[] {x, y};
      } // Fim do if

    } // Fim do while
  
  } // Fim do metodo randomPointInWater

  /** ********************************************************************
  * Metodo: spawnJumpingFish
  * Funcao: Cria um peixe que salta da agua em uma posicao aleatoria dentro 
  * da area de agua e executa uma animacao de salto, criando um efeito visual 
  * de respingo e ondulacao na agua.
  * Parametros: @param root e o container principal onde o peixe sera adicionado, 
  * @param waterArea e o caminho SVG que representa a area de agua
  * Retorno: @return void
  ********************************************************************* */
  private void spawnJumpingFish(Pane root, SVGPath waterArea) {

    // Cria um objeto Random para selecionar uma imagem de peixe aleatoria
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

    // Cria um ImageView para o peixe usando uma imagem aleatoria do array
    ImageView fish = new ImageView(
      // Seleciona uma imagem de peixe aleatoria usando o indice gerado pelo Random
      new Image(fishImages[random.nextInt(fishImages.length)])
    );

    // Configura o tamanho do peixe para que ele se encaixe visualmente na cena
    fish.setFitWidth(28);
    // Configura a altura do peixe para manter a proporcao da imagem
    fish.setPreserveRatio(true);

    // Gera uma posicao aleatoria dentro da area de agua para o peixe usando o metodo randomPointInWater
    double[] pos = randomPointInWater(waterArea);

    // Define a posicao do peixe na cena usando as coordenadas geradas
    fish.setLayoutX(pos[0]);
    fish.setLayoutY(pos[1]);

    // Adiciona o peixe ao container principal da cena para que ele seja exibido
    root.getChildren().add(fish);

    // Inicia a animacao de salto do peixe, que inclui um movimento de subida e descida, uma rotacao para simular o movimento do peixe no ar, e efeitos visuais de respingo e ondulacao na agua quando o peixe retorna para a agua
    animateJumpFish(root, fish);
  
  } // Fim do metodo spawnJumpingFish

  /** ********************************************************************
  * Metodo: animateJumpFish
  * Funcao: Executa a animacao de salto do peixe, que inclui um movimento
  * de subida e descida, uma rotacao para simular o movimento do peixe no ar, 
  * e efeitos visuais de respingo e ondulacao na agua quando o peixe retorna para a agua.
  * Parametros: @param root eh o container principal onde os efeitos visuais serao adicionados,
  * @param fish e o ImageView do peixe que sera animado
  * Retorno: @return void
  ********************************************************************* */
  private void animateJumpFish(Pane root, ImageView fish) {

    // Cria uma animacao de translacao para simular o movimento de salto do peixe
    TranslateTransition jump = new TranslateTransition(
      // Define a duracao da animacao de salto, que determina a velocidade do movimento do peixe
      Duration.seconds(0.8),
      // Define o objeto que sera animado, que neste caso eh o ImageView do peixe
      fish
    ); 

    // Define o movimento vertical do salto, onde o peixe sobe 40 pixels e depois retorna para a posicao original, criando um efeito de subida e descida
    jump.setByY(-40);
    // Configura a animacao para que ela se inverta automaticamente, fazendo com que o peixe retorne para a posicao original apos subir, simulando o movimento de salto completo
    jump.setAutoReverse(true);
    // Define que a animacao de salto sera executada duas vezes, o que significa que o peixe subira e descera uma vez, criando um efeito visual mais fluido e natural
    jump.setCycleCount(2);

    // Cria uma animacao de rotacao para simular o movimento do peixe no ar durante o salto, onde o peixe gira levemente para um lado e depois para o outro, criando um efeito visual mais dinâmico e realista
    RotateTransition wiggle = new RotateTransition(
      Duration.seconds(0.2),
      fish
    );

    // Define o angulo de rotacao para criar o efeito de movimento do peixe no ar, onde o peixe gira 25 graus para um lado e depois para o outro, simulando o movimento de oscilacao que os peixes fazem quando saltam da agua
    wiggle.setByAngle(25);
    // Configura a animacao de rotacao para que ela se inverta automaticamente, fazendo com que o peixe gire para um lado e depois para o outro, criando um efeito de oscilacao completa durante o salto
    wiggle.setAutoReverse(true);
    // Define que a animacao de rotacao sera executada 6 vezes, o que significa que o peixe fara 3 oscilacoes completas durante o salto, criando um efeito visual mais fluido e natural
    wiggle.setCycleCount(6);

    // Cria uma animacao paralela para combinar a animacao de salto e a animacao de rotacao, fazendo com que ambas sejam executadas ao mesmo tempo, criando um efeito visual mais complexo e realista do peixe saltando da agua
    ParallelTransition animation = new ParallelTransition(
      fish,
      jump,
      wiggle
    );

    // Define um evento que sera executado quando a animacao de salto e rotacao terminar, onde serao criados efeitos visuais de respingo e ondulacao na agua para simular o impacto do peixe retornando para a agua, e o peixe sera removido da cena para que ele nao fique visivel apos o salto
    animation.setOnFinished(e -> {

      // Cria os efeitos visuais de respingo e ondulacao na agua usando os metodos createSplash e createRipple, que adicionam elementos visuais ao container principal da cena para simular o impacto do peixe retornando para a agua, criando um efeito visual mais imersivo e realista
      createSplash(root, fish.getLayoutX(), fish.getLayoutY());
      // Cria o efeito de ondulacao na agua usando o metodo createRipple, que adiciona um circulo que se expande e desaparece para simular a ondulacao causada pelo impacto do peixe retornando para a agua
      createRipple(root, fish.getLayoutX(), fish.getLayoutY());

      // Remove o peixe da cena apos a animacao de salto e os efeitos visuais serem executados, garantindo que o peixe nao fique visivel na cena apos completar o salto
      root.getChildren().remove(fish);
    });

    // Inicia a animacao de salto e rotacao do peixe
    animation.play();
  
  } // Fim do metodo animateJumpFish

  /** ********************************************************************
  * Metodo: createSplash
  * Funcao: Cria um efeito visual de respingo na agua quando o peixe retorna
  * para a agua apos o salto, simulando o impacto do peixe na agua com pequenos
  * circulos que se movem para fora do ponto de impacto e desaparecem gradualmente
  * Parametros: @param root eh o container principal onde os elementos do respingo serao adicionados,
  * @param x e a coordenada x do ponto de impacto do peixe retornando para a agua, @param y e a 
  * coordenada y do ponto de impacto do peixe retornando para a agua
  * Retorno: @return void
  ********************************************************************* */
  private void createSplash(Pane root, double x, double y) {

    // Loop para criar multiplos circulos de respingo, onde cada circulo representa uma gota de agua que se move para fora do ponto de impacto, criando um efeito visual mais complexo e realista de respingo na agua
    for (int i = 0; i < 4; i++) {

      // Cria um circulo para representar uma gota de agua do respingo, onde cada circulo tem um tamanho pequeno e uma cor branca para simular a aparência de gotas de agua sendo lancadas para fora do ponto de impacto
      Circle drop = new Circle(2, Color.WHITE);

      // Define a posicao inicial do circulo de respingo no ponto de impacto do peixe retornando para a agua, garantindo que os circulos sejam criados exatamente onde o peixe toca a agua, criando um efeito visual mais preciso e realista
      drop.setLayoutX(x);
      drop.setLayoutY(y);

      // Adiciona o circulo de respingo ao container principal da cena para que ele seja exibido, permitindo que os efeitos visuais do respingo sejam visiveis na cena quando o peixe retorna para a agua
      root.getChildren().add(drop);

      // Cria uma animacao de translacao para o circulo de respingo, onde cada circulo se move para fora do ponto de impacto em uma direcao aleatoria, simulando o movimento das gotas de agua sendo lancadas para fora do ponto de impacto do peixe retornando para a agua
      TranslateTransition move = new TranslateTransition(
        Duration.seconds(0.6),
        drop
      );

      // Define o movimento do circulo de respingo para que ele se mova para fora do ponto de impacto em uma direcao aleatoria, onde a coordenada x e deslocada aleatoriamente para a esquerda ou direita, e a coordenada y e deslocada para cima, criando um efeito visual de gotas de agua sendo lancadas para fora do ponto de impacto do peixe retornando para a agua
      move.setByX((Math.random() - 0.5) * 30);
      move.setByY(-10 - Math.random() * 20);

      // Cria uma animacao de fade para o circulo de respingo, onde cada circulo desaparece gradualmente enquanto se move para fora do ponto de impacto, simulando a dissipacao das gotas de agua no ar apos serem lancadas para fora do ponto de impacto do peixe retornando para a agua
      FadeTransition fade = new FadeTransition(
        Duration.seconds(0.6),
        drop
      );

      // Define a animacao de fade para que o circulo de respingo comece com uma opacidade de 1 (totalmente visivel) e desapareca gradualmente para uma opacidade de 0 (totalmente invisivel), criando um efeito visual de dissipacao das gotas de agua no ar apos serem lancadas para fora do ponto de impacto do peixe retornando para a agua
      fade.setFromValue(1);
      fade.setToValue(0);

      // Cria uma animacao paralela para combinar a animacao de movimento e a animacao de fade do circulo de respingo, fazendo com que ambas sejam executadas ao mesmo tempo, criando um efeito visual mais complexo e realista de gotas de agua sendo lancadas para fora do ponto de impacto do peixe retornando para a agua e dissipando no ar
      ParallelTransition splash = new ParallelTransition(
        drop,
        move,
        fade
      );

      // Define um evento que sera executado quando a animacao de movimento e fade do circulo de respingo terminar, onde o circulo de respingo sera removido da cena para garantir que ele nao fique visivel apos completar a animacao, mantendo a cena limpa e evitando que elementos visuais desnecessarios permanecam na cena
      splash.setOnFinished(e -> root.getChildren().remove(drop));

      // Inicia a animacao de movimento e fade do circulo de respingo
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
  * @param x e a coordenada x do ponto de impacto do peixe retornando para a agua, @param y e a 
  * coordenada y do ponto de impacto do peixe retornando para a agua
  * Retorno: @return void
  ********************************************************************* */
  private void createRipple(Pane root, double x, double y) {

    // Cria um circulo para representar a ondulacao na agua, onde o circulo comeca com um tamanho pequeno e uma borda branca para simular a aparência de uma onda se formando na agua apos o impacto do peixe retornando para a agua
    Circle ripple = new Circle(3);

    // Define a posicao inicial do circulo de ondulacao no ponto de impacto do peixe retornando para a agua, garantindo que o circulo seja criado exatamente onde o peixe toca a agua, criando um efeito visual mais preciso e realista de uma onda se formando na agua apos o impacto do peixe retornando para a agua
    ripple.setLayoutX(x);
    ripple.setLayoutY(y);

    // Configura a aparência do circulo de ondulacao para que ele tenha uma borda branca e um preenchimento transparente, criando um efeito visual de uma onda se formando na agua apos o impacto do peixe retornando para a agua
    ripple.setStroke(Color.WHITE);
    ripple.setFill(Color.TRANSPARENT);
    ripple.setStrokeWidth(2);

    // Adiciona o circulo de ondulacao ao container principal da cena para que ele seja exibido, permitindo que os efeitos visuais da onda se formando na agua sejam visiveis na cena quando o peixe retorna para a agua
    root.getChildren().add(ripple);

    // Cria uma animacao de escala para o circulo de ondulacao, onde o circulo se expande gradualmente a partir do ponto de impacto, simulando a formacao de uma onda na agua apos o impacto do peixe retornando para a agua
    ScaleTransition expand = new ScaleTransition(
      Duration.seconds(1.2),
      ripple
    );

    // Define a animacao de escala para que o circulo de ondulacao comece com um tamanho original (1) e se expanda para um tamanho maior (6), criando um efeito visual de uma onda se formando na agua apos o impacto do peixe retornando para a agua
    expand.setToX(6);
    expand.setToY(6);

    // Cria uma animacao de fade para o circulo de ondulacao, onde o circulo desaparece gradualmente enquanto se expande, simulando a dissipacao da onda na agua apos ser formada pelo impacto do peixe retornando para a agua
    FadeTransition fade = new FadeTransition(
      Duration.seconds(1.2),
      ripple
    );

    // Define a animacao de fade para que o circulo de ondulacao comece com uma opacidade de 0.8 (quase totalmente visivel) e desapareca gradualmente para uma opacidade de 0 (totalmente invisivel), criando um efeito visual de dissipacao da onda na agua apos ser formada pelo impacto do peixe retornando para a agua
    fade.setFromValue(0.8);
    fade.setToValue(0);

    // Cria uma animacao paralela para combinar a animacao de escala e a animacao de fade do circulo de ondulacao, fazendo com que ambas sejam executadas ao mesmo tempo, criando um efeito visual mais complexo e realista de uma onda se formando e dissipando na agua apos o impacto do peixe retornando para a agua
    ParallelTransition rippleAnim = new ParallelTransition(
      ripple,
      expand,
      fade
    );

    // Define um evento que sera executado quando a animacao de escala e fade do circulo de ondulacao terminar, onde o circulo de ondulacao sera removido da cena para garantir que ele nao fique visivel apos completar a animacao, mantendo a cena limpa e evitando que elementos visuais desnecessarios permanecam na cena
    rippleAnim.setOnFinished(e -> root.getChildren().remove(ripple));

    // Inicia a animacao de escala e fade do circulo de ondulacao
    rippleAnim.play();
  
  } // Fim do metodo createRipple

  /** ********************************************************************
  * Metodo: spawnBubble
  * Funcao: Cria uma bolha que surge da agua em uma posicao aleatoria dentro da 
  * area de agua e executa uma animacao de subida e desaparecimento, simulando 
  * o movimento de bolhas subindo na agua e desaparecendo gradualmente, 
  * criando um efeito visual de bolhas subindo na agua
  * Parametros: @param root e o container principal onde a bolha sera adicionada, 
  * @param waterArea e o caminho SVG que representa a area de agua
  * Retorno: @return void
  ********************************************************************* */ 
  private void spawnBubble(Pane root, SVGPath waterArea) {

    // Gera uma posicao aleatoria dentro da area de agua para a bolha usando o metodo randomPointInWater, garantindo que as bolhas surjam em locais variados dentro da agua, criando um efeito visual mais dinâmico e realista de bolhas subindo na agua
    double[] pos = randomPointInWater(waterArea);

    // Cria um circulo para representar a bolha, onde a bolha tem um tamanho pequeno e uma cor branca para simular a aparência de uma bolha subindo na agua
    Circle bubble = new Circle(2);
    bubble.setLayoutX(pos[0]);
    bubble.setLayoutY(pos[1]);
    bubble.setFill(Color.WHITE);

    // Adiciona a bolha ao container principal da cena para que ela seja exibida, permitindo que os efeitos visuais das bolhas subindo na agua sejam visiveis na cena
    root.getChildren().add(bubble);

    // Cria uma animacao de translacao para a bolha, onde a bolha se move para cima, simulando o movimento de bolhas subindo na agua
    TranslateTransition rise = new TranslateTransition(
      Duration.seconds(3),
      bubble
    );

    // Define o movimento vertical da bolha para que ela suba 30 pixels, criando um efeito visual de bolhas subindo na agua
    rise.setByY(-30);

    // Cria uma animacao de fade para a bolha, onde a bolha desaparece gradualmente enquanto sobe, simulando o desaparecimento das bolhas na agua à medida que elas sobem e se dissipam
    FadeTransition fade = new FadeTransition(
      Duration.seconds(3),
      bubble
    );

    // Define a animacao de fade para que a bolha comece com uma opacidade de 1 (totalmente visivel) e desapareca gradualmente para uma opacidade de 0 (totalmente invisivel), criando um efeito visual de bolhas desaparecendo na agua à medida que sobem e se dissipam
    fade.setFromValue(1);
    fade.setToValue(0);

    // Cria uma animacao paralela para combinar a animacao de subida e a animacao de fade da bolha, fazendo com que ambas sejam executadas ao mesmo tempo, criando um efeito visual mais complexo e realista de bolhas subindo na agua e desaparecendo gradualmente
    ParallelTransition animation = new ParallelTransition(
      bubble,
      rise,
      fade
    );

    // Define um evento que sera executado quando a animacao de subida e fade da bolha terminar, onde a bolha sera removida da cena para garantir que ela nao fique visivel apos completar a animacao, mantendo a cena limpa e evitando que elementos visuais desnecessarios permanecam na cena
    animation.setOnFinished(e -> root.getChildren().remove(bubble));

    // Inicia a animacao de subida e fade da bolha
    animation.play();
  
  } // Fim do metodo spawnBubble

  /** ********************************************************************
  * Metodo: startFishSpawner
  * Funcao: Inicia o sistema de surgimento de peixes na simulacao. O metodo
  * cria uma Timeline que dispara periodicamente a criacao de um novo peixe
  * na tela a cada 4 segundos, onde cada peixe surge em uma posicao aleatoria 
  * dentro da area de agua e executa uma animacao de salto, criando um 
  * efeito visual de peixes saltando da agua
  * Parametros: @param root eh o container principal onde os peixes 
  * serao adicionados e animados durante a simulacao,
  * @param waterArea eh o caminho SVG que representa a area de agua, 
  * utilizado para gerar posicoes aleatorias para os peixes surgirem dentro da agua
  * Retorno: @return void
  ********************************************************************* */
  private void startFishSpawner(Pane root, SVGPath waterArea) {

    // Cria uma Timeline para controlar o surgimento dos peixes, onde a Timeline dispara um evento a cada 4 segundos para criar um novo peixe na tela, garantindo que os peixes surjam periodicamente durante a simulacao, criando um efeito visual de peixes saltando da agua de forma continua
    Timeline fishSpawner = new Timeline(
      new KeyFrame(Duration.seconds(4), e -> spawnJumpingFish(root, waterArea))
    );

    // Define que a Timeline executara indefinidamente, fazendo com que os peixes continuem surgindo na tela a cada 4 segundos durante toda a simulacao, criando um efeito visual de peixes saltando da agua de forma continua e dinâmica
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
  * Parametros: @param root eh o container principal onde as bolhas serao adicionadas 
  * e animadas durante a simulacao,
  * @param waterArea eh o caminho SVG que representa a area de agua, 
  * utilizado para gerar posicoes aleatorias para as bolhas surgirem dentro da agua
  * Retorno: @return void
  ********************************************************************* */
  private void startBubbleSpawner(Pane root, SVGPath waterArea) {

    // Cria uma Timeline para controlar o surgimento das bolhas, onde a Timeline dispara um evento a cada 1.5 segundos para criar uma nova bolha na tela, garantindo que as bolhas surjam periodicamente durante a simulacao, criando um efeito visual de bolhas subindo na agua de forma continua
    Timeline bubbleSpawner = new Timeline(
      new KeyFrame(Duration.seconds(1.5), e -> spawnBubble(root, waterArea))
    );

    // Define que a Timeline executara indefinidamente, fazendo com que as bolhas continuem surgindo na tela a cada 1.5 segundos durante toda a simulacao, criando um efeito visual de bolhas subindo na agua de forma continua e dinâmica
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

    // Adiciona na tela
    root.getChildren().add(airplane);

    // Garante que o aviao fique na frente de outros elementos
    airplane.toFront();

    // Animacao de voo
    TranslateTransition fly = new TranslateTransition(Duration.seconds(8), airplane);
    // Posicao inicial
    fly.setFromY(0);
    // Sobre ate sair da tela
    fly.setToY(-1200);
    fly.setInterpolator(Interpolator.LINEAR);

    // Remove o aviao quando terminar
    fly.setOnFinished(e -> root.getChildren().remove(airplane));

    fly.play();
  
  }

  /** ********************************************************************
  * Metodo: startAirplaneTraffic
  * Funcao: Inicia o sistema de trafego de avioes na simulacao. O metodo
  * cria uma Timeline que dispara periodicamente a criacao de um novo
  * aviao na tela a cada 15 segundos.
  * Parametros: @param root eh o Pane principal onde os avioes serao
  * adicionados e animados durante a simulacao.
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
  * Funcao: Metodo principal por iniciar a execucao do programa
  * Parametros: @param args representa os argumentos passados pela linha de comando.
  * Retorno: @return void
  ******************************************************************** */
  public static void main(String[] args) {
    launch(args);
  } // Fim do metodo main
} // Fim da classe Principal
