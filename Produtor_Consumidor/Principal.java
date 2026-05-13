/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 07/05/2026
* Ultima alteracao.: 12/05/2026
* Nome.............: Principal.java
* Funcao...........: Classe principal da aplicacao JavaFX.
*                    Monta a GUI com tema de churrasco e conecta
*                    a interface visual ao nucleo do problema
*                    Produtor/Consumidor com semaforos.
*
*                    Layout:
*                    - Esquerda: churrasqueiro (produtor)
*                    - Centro:   mesa com 10 slots (buffer)
*                    - Direita:  comedor (consumidor)
*                    - Painel:   sliders, botoes pausa e reset
************************************************************************ */

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.ProdutorConsumidor;
import threads.Consumidor;
import threads.Produtor;
import util.Constants;

public class Principal extends Application {

  private ProdutorConsumidor pc;
  private Produtor produtor;
  private Consumidor consumidor;

  // Slots da mesa
  private final StackPane[] slots = new StackPane[Constants.N];

  // Controles da GUI
  private Slider sliderProdutor;
  private Slider sliderConsumidor;
  private Button btnPausaProdutor;
  private Button btnPausaConsumidor;
  private Button btnReset;
  private ProgressBar barBuffer;
  private Label lblBuffer;
  private Label lblStatusProdutor;
  private Label lblStatusConsumidor;

  // Imagens dos personagens
  private ImageView imgChurrasqueiro;
  private ImageView imgComedor;
  private Image imgChurrasqueiroAtivo;
  private Image imgChurrasqueiroParado;
  private Image imgComedorAtivo;
  private Image imgComedorParado;

  // Estado visual atual de cada personagem: evita troca desnecessaria de imagem.
  // O consumidor so volta a imagem ativa apos consumir, nunca por acao do produtor.
  private boolean churrasqueiroEstaAtivo = true;
  private boolean comedorEstaAtivo = true;

  /** ********************************************************************
  * Metodo: start
  * Funcao: Inicializa a aplicacao JavaFX e monta toda a GUI.
  * Parametros: @param stage janela principal
  * Retorno: @return void
  ******************************************************************** */
  @Override
  public void start(Stage stage) {

    AudioClip themeSound = new AudioClip(
      getClass().getResource("/sound/theme.wav").toExternalForm()
    );

    themeSound.setCycleCount(AudioClip.INDEFINITE);
    themeSound.play();

    stage.setTitle("CHURRAS DO MAMAX");
    stage.setOnCloseRequest(e -> { Platform.exit(); System.exit(0); });

    // Carrega as imagens
    imgChurrasqueiroAtivo  = new Image("/img/churrasqueiro.png");
    imgChurrasqueiroParado = new Image("/img/churrasqueiro_parado.png");
    imgComedorAtivo = new Image("/img/consumidor.png");
    imgComedorParado = new Image("/img/consumidor_parado.png");

    // Raiz da cena de simulacao
    Pane simulacaoPane = new Pane();
    simulacaoPane.setPrefSize(900, 380);

    // Fundo da cena
    Image bgImg = new Image("/img/background.png");
    BackgroundImage bg = new BackgroundImage(bgImg,
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.DEFAULT,
        new BackgroundSize(980, 380, false, false, false, false));
    simulacaoPane.setBackground(new Background(bg));

    // Churrasqueiro (produtor) — posicao: x = 30, y = 80
    imgChurrasqueiro = new ImageView(imgChurrasqueiroAtivo);
    imgChurrasqueiro.setFitWidth(80);
    imgChurrasqueiro.setFitHeight(120);
    imgChurrasqueiro.setLayoutX(98);
    imgChurrasqueiro.setLayoutY(80);
    simulacaoPane.getChildren().add(imgChurrasqueiro);

    // Comedor (consumidor) — posicao: x = 790, y = 80
    // ImageView 80x120px, posicionado a direita da mesa
    imgComedor = new ImageView(imgComedorAtivo);
    imgComedor.setFitWidth(80);
    imgComedor.setFitHeight(120);
    imgComedor.setLayoutX(850);
    imgComedor.setLayoutY(80);
    simulacaoPane.getChildren().add(imgComedor);

    // Mesa com 10 slots — posicao central
    construirSlotsMesa(simulacaoPane);

    // Decoracao: texto "MESA (BUFFER)" acima da mesa
    Text txtMesa = new Text("MESA");
    txtMesa.setFont(Font.font("Arial", FontWeight.BOLD, 18));
    txtMesa.setFill(Color.web("#3e2000"));
    txtMesa.setLayoutX(470);
    txtMesa.setLayoutY(155);
    simulacaoPane.getChildren().add(txtMesa);

    // Labels de status dos personagens
    lblStatusProdutor = new Label("GRELHANDO...");
    lblStatusProdutor.setLayoutX(70);
    lblStatusProdutor.setLayoutY(240);
    lblStatusProdutor.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #FFF;");
    simulacaoPane.getChildren().add(lblStatusProdutor);

    lblStatusConsumidor = new Label("COMENDO...");
    lblStatusConsumidor.setLayoutX(850);
    lblStatusConsumidor.setLayoutY(210);
    lblStatusConsumidor.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #FFF;");
    simulacaoPane.getChildren().add(lblStatusConsumidor);

    // Painel de controle
    VBox painelControle = construirPainelControle();

    //  Layout raiz
    VBox root = new VBox(0, simulacaoPane, painelControle);
    root.getStyleClass().add("root-container");

    Scene scene = new Scene(root, 980, 600);
    scene.getStylesheets().add("/css/style.css");
    stage.setScene(scene);
    stage.setResizable(false);
    stage.show();

    // Inicia a simulacao
    iniciarSimulacao();

  } // Fim do metodo start

  /** ********************************************************************
  * Metodo: construirSlotsMesa
  * Funcao: Cria os 10 slots visuais da mesa (buffer) e os adiciona
  *         ao pane de simulacao.
  *         Cada slot: 50x60px, posicao x=(155 + i*61), y=165.
  *         Slot vazio = prato cinza; slot cheio = imagem do espeto.
  * Parametros: @param pane pane onde os slots serao adicionados
  * Retorno: @return void
  ******************************************************************** */
  private void construirSlotsMesa(Pane pane) {
    Image imgEspeto = new Image("/img/espeto.png");
    Image imgPratoVazio = new Image("/img/prato_vazio.png");

    for (int i = 0; i < Constants.N; i++) {
      StackPane slot = new StackPane();
      slot.setPrefSize(50, 60);
      slot.setLayoutX(185 + i * 61);
      slot.setLayoutY(165);

      // Imagem base: prato vazio
      ImageView ivBase = new ImageView(imgPratoVazio);
      ivBase.setFitWidth(50);
      ivBase.setFitHeight(40);

      // Imagem do espeto - inicialmente oculto
      ImageView ivEspeto = new ImageView(imgEspeto);
      ivEspeto.setFitWidth(50);
      ivEspeto.setFitHeight(20);
      ivEspeto.setVisible(false);

      slot.getChildren().addAll(ivBase, ivEspeto);
      slots[i] = slot;
      pane.getChildren().add(slot);
    }
  } // Fim do metodo construirSlotsMesa

  /** ********************************************************************
  * Metodo: construirPainelControle
  * Funcao: Monta o painel inferior com sliders, botoes de pausa
  *         individuais, barra de buffer e botao reset.
  * Parametros: nao possui
  * Retorno: @return VBox painel montado
  ******************************************************************** */
  private VBox construirPainelControle() {

    // Produtor
    Label lblProd = new Label("CHURRASQUEIRO (PRODUTOR)");
    lblProd.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

    // Slider 0-10: esquerda=lento, direita=rapido. Conversao para ms em sliderParaMs()
    sliderProdutor = new Slider(0, 10, 5);
    sliderProdutor.setShowTickMarks(true);
    sliderProdutor.setShowTickLabels(true);
    sliderProdutor.setMajorTickUnit(1);
    sliderProdutor.setMinorTickCount(0);
    sliderProdutor.setSnapToTicks(true);
    sliderProdutor.setPrefWidth(300);
    sliderProdutor.getStyleClass().add("slider-produtor");

    Label lblVelProd = new Label("VELOCIDADE:");
    btnPausaProdutor = new Button("PAUSAR PRODUTOR");
    btnPausaProdutor.getStyleClass().add("btn-pausa");

    HBox linhaProdutor = new HBox(15,
        lblProd, lblVelProd, sliderProdutor, btnPausaProdutor);
    linhaProdutor.setAlignment(Pos.CENTER_LEFT);
    linhaProdutor.setPadding(new Insets(8, 20, 4, 20));

    // Consumidor 
    Label lblCons = new Label("COMEDOR (CONSUMIDOR)");
    lblCons.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

    sliderConsumidor = new Slider(0, 10, 5);
    sliderConsumidor.setShowTickMarks(true);
    sliderConsumidor.setShowTickLabels(true);
    sliderConsumidor.setMajorTickUnit(1);
    sliderConsumidor.setMinorTickCount(0);
    sliderConsumidor.setSnapToTicks(true);
    sliderConsumidor.setPrefWidth(300);
    sliderConsumidor.getStyleClass().add("slider-consumidor");

    Label lblVelCons = new Label("VELOCIDADE:");
    btnPausaConsumidor = new Button("PAUSAR CONSUMIDOR");
    btnPausaConsumidor.getStyleClass().add("btn-pausa");

    HBox linhaConsumidor = new HBox(15,
        lblCons, lblVelCons, sliderConsumidor, btnPausaConsumidor);
    linhaConsumidor.setAlignment(Pos.CENTER_LEFT);
    linhaConsumidor.setPadding(new Insets(4, 20, 4, 20));

    // Buffer + Reset
    barBuffer = new ProgressBar(0);
    barBuffer.setPrefWidth(400);
    barBuffer.getStyleClass().add("bar-buffer");

    lblBuffer = new Label("Buffer: 0/" + Constants.N);
    lblBuffer.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

    btnReset = new Button("↺  RESET");
    btnReset.getStyleClass().add("btn-reset");

    HBox linhaBuffer = new HBox(20,
        lblBuffer, barBuffer, btnReset);
    linhaBuffer.setAlignment(Pos.CENTER);
    linhaBuffer.setPadding(new Insets(8, 20, 8, 20));

    // Painel
    VBox painel = new VBox(0, linhaProdutor, linhaConsumidor, linhaBuffer);
    painel.setStyle("-fx-background-color: #fdf3e7; -fx-border-color: #8B5E3C; -fx-border-width: 2 0 0 0;");

    // Eventos dos controles

    // Slider do produtor: 0=lento(MAX_MS) a 10=rapido(MIN_MS)
    sliderProdutor.valueProperty().addListener((obs, o, n) -> {
      if (produtor != null) produtor.setSpeedMs(sliderParaMs(n.doubleValue()));
    });

    // Slider do consumidor
    sliderConsumidor.valueProperty().addListener((obs, o, n) -> {
      if (consumidor != null) consumidor.setSpeedMs(sliderParaMs(n.doubleValue()));
    });

    // Botao pausa/retomada do produtor
    btnPausaProdutor.setOnAction(e -> {
      if (produtor == null) return;
      if (produtor.isPausado()) {
        produtor.retomar();
        btnPausaProdutor.setText("PAUSAR PRODUTOR");
        atualizarImagemPersonagem(imgChurrasqueiro, imgChurrasqueiroAtivo);
      } else {
        produtor.pausar();
        btnPausaProdutor.setText("RETOMAR PRODUTOR");
        atualizarImagemPersonagem(imgChurrasqueiro, imgChurrasqueiroParado);
      }
    });

    // Botao pausa/retomada do consumidor
    btnPausaConsumidor.setOnAction(e -> {
      if (consumidor == null) return;
      if (consumidor.isPausado()) {
        consumidor.retomar();
        btnPausaConsumidor.setText("PAUSAR CONSUMIDOR");
        atualizarImagemPersonagem(imgComedor, imgComedorAtivo);
      } else {
        consumidor.pausar();
        btnPausaConsumidor.setText("RETOMAR CONSUMIDOR");
        atualizarImagemPersonagem(imgComedor, imgComedorParado);
      }
    });

    // Botao reset
    btnReset.setOnAction(e -> resetarSimulacao());

    return painel;
  } // Fim do metodo construirPainelControle

  /** ********************************************************************
  * Metodo: iniciarSimulacao
  * Funcao: Cria o contexto compartilhado e as duas threads, configura
  *         os callbacks de GUI e inicia a producao/consumo.
  * Parametros: nao possui
  * Retorno: @return void
  ******************************************************************** */
  private void iniciarSimulacao() {
    pc = new ProdutorConsumidor();
    produtor = new Produtor(pc,  sliderParaMs(sliderProdutor.getValue()));
    consumidor = new Consumidor(pc, sliderParaMs(sliderConsumidor.getValue()));

    // Callback do produtor: atualiza slots e barra apos cada producao
    produtor.setOnProduziu(() -> {
      atualizarSlots();
      atualizarBarra();
      animarSlotAdicionado();
      lblStatusProdutor.setText("GRELHANDO...");
      // Restaura churrasqueiro ativo somente se estava parado
      if (!churrasqueiroEstaAtivo) {
        churrasqueiroEstaAtivo = true;
        atualizarImagemPersonagem(imgChurrasqueiro, imgChurrasqueiroAtivo);
      }
    });

    // Callback quando produtor bloqueia (buffer cheio)
    produtor.setOnEsperando(() -> {
      lblStatusProdutor.setText("BUFFER CHEIO!");
      // Troca para parado somente se estava ativo
      if (churrasqueiroEstaAtivo) {
        churrasqueiroEstaAtivo = false;
        atualizarImagemPersonagem(imgChurrasqueiro, imgChurrasqueiroParado);
      }
    });

    // Callback do consumidor: atualiza slots e barra apos cada consumo
    consumidor.setOnConsumiu(() -> {
      atualizarSlots();
      atualizarBarra();
      lblStatusConsumidor.setText("COMENDO...");
      // Restaura comedor ativo somente se estava parado
      if (!comedorEstaAtivo) {
        comedorEstaAtivo = true;
        atualizarImagemPersonagem(imgComedor, imgComedorAtivo);
      }
    });

    // Callback quando consumidor bloqueia (buffer vazio)
    consumidor.setOnEsperando(() -> {
      lblStatusConsumidor.setText("AGUARDANDO \nESPETO...");
      // Troca para parado somente se estava ativo
      if (comedorEstaAtivo) {
        comedorEstaAtivo = false;
        atualizarImagemPersonagem(imgComedor, imgComedorParado);
      }
    });

    produtor.start();
    consumidor.start();
  } // Fim do metodo iniciarSimulacao

  /** ********************************************************************
  * Metodo: resetarSimulacao
  * Funcao: Interrompe as threads atuais, reinicia o buffer e os
  *         semaforos e cria novas threads do zero.
  * Parametros: nao possui
  * Retorno: @return void
  ******************************************************************** */
  private void resetarSimulacao() {
    // Para as threads atuais
    if (produtor  != null) {
      produtor.interrupt();
    }

    if (consumidor != null) {
      consumidor.interrupt();
    }

    // Aguarda um frame para as threads processarem a interrupcao
    try { 
      Thread.sleep(100); 
    } catch (InterruptedException ignored) {}

    // Reinicia o estado compartilhado
    pc.reset();

    // Reinicia a GUI
    sliderProdutor.setValue(5);
    sliderConsumidor.setValue(5);
    btnPausaProdutor.setText("PAUSAR PRODUTOR");
    btnPausaConsumidor.setText("PAUSAR CONSUMIDOR");
    lblStatusProdutor.setText("GRELHANDO...");
    lblStatusConsumidor.setText("COMENDO...");
    churrasqueiroEstaAtivo = true;
    comedorEstaAtivo = true;
    atualizarImagemPersonagem(imgChurrasqueiro, imgChurrasqueiroAtivo);
    atualizarImagemPersonagem(imgComedor, imgComedorAtivo);
    limparSlots();
    atualizarBarra();

    // Recria e inicia as threads
    iniciarSimulacao();
  } // Fim do metodo resetarSimulacao

  /** ********************************************************************
  * Metodo: atualizarSlots
  * Funcao: Atualiza a visibilidade dos espetos nos slots da mesa
  *         de acordo com o estado atual do buffer.
  *         Os primeiros count slots mostram espeto; os demais mostram
  *         o prato vazio.
  * Parametros: nao possui
  * Retorno: @return void
  ******************************************************************** */
  private void atualizarSlots() {
    int count = pc.getCount();
    for (int i = 0; i < Constants.N; i++) {
      ImageView ivEspeto = (ImageView) slots[i].getChildren().get(1);
      ivEspeto.setVisible(i < count);
    }
  } // Fim do metodo atualizarSlots

  /** ********************************************************************
  * Metodo: limparSlots
  * Funcao: Oculta todos os espetos dos slots (usado no reset).
  * Parametros: nao possui
  * Retorno: @return void
  ******************************************************************** */
  private void limparSlots() {
    for (StackPane slot : slots) {
      ImageView ivEspeto = (ImageView) slot.getChildren().get(1);
      ivEspeto.setVisible(false);
    }
  } // Fim do metodo limparSlots

  /** ********************************************************************
  * Metodo: atualizarBarra
  * Funcao: Atualiza a ProgressBar e o label de ocupacao do buffer.
  * Parametros: nao possui
  * Retorno: @return void
  ******************************************************************** */
  private void atualizarBarra() {
    int count = pc.getCount();
    barBuffer.setProgress((double) count / Constants.N);
    lblBuffer.setText("Buffer: " + count + "/" + Constants.N);
  } // Fim do metodo atualizarBarra

  /** ********************************************************************
  * Metodo: animarSlotAdicionado
  * Funcao: Aplica um FadeTransition no slot recem preenchido
  *         para dar feedback visual de que um espeto foi adicionado.
  * Parametros: nao possui
  * Retorno: @return void
  ******************************************************************** */
  private void animarSlotAdicionado() {
    int count = pc.getCount();
    if (count > 0 && count <= Constants.N) {
      StackPane slot = slots[count - 1];
      FadeTransition ft = new FadeTransition(Duration.millis(300), slot);
      ft.setFromValue(0.3);
      ft.setToValue(1.0);
      ft.play();
    }
  } // Fim do metodo animarSlotAdicionado

  /** ********************************************************************
  * Metodo: atualizarImagemPersonagem
  * Funcao: Troca a imagem exibida no ImageView do personagem.
  * Parametros: @param iv    ImageView do personagem
  *             @param nova  nova imagem a exibir
  * Retorno: @return void
  ******************************************************************** */
  private void atualizarImagemPersonagem(ImageView iv, Image nova) {
    if (iv != null && nova != null) {
      iv.setImage(nova);
    }
  } // Fim do metodo atualizarImagemPersonagem

  /** ********************************************************************
  * Metodo: sliderParaMs
  * Funcao: Converte o valor do slider (0-10) para milissegundos.
  *         Escala invertida: 0 = mais lento (MAX_SPEED_MS),
  *                           10 = mais rapido (MIN_SPEED_MS).
  * Parametros: @param val valor do slider (0.0 a 10.0)
  * Retorno: @return int velocidade em milissegundos
  ******************************************************************** */
  private int sliderParaMs(double val) {
    int range = Constants.MAX_SPEED_MS - Constants.MIN_SPEED_MS;
    return (int)(Constants.MAX_SPEED_MS - (val / 10.0) * range);
  } // Fim do metodo sliderParaMs

  /** ********************************************************************
  * Metodo: main
  * Funcao: Ponto de entrada da aplicacao.
  * Parametros: @param args argumentos da linha de comando
  * Retorno: @return void
  ******************************************************************** */
  public static void main(String[] args) {
    launch(args);
  } // Fim do metodo main

} // Fim da classe Principal
