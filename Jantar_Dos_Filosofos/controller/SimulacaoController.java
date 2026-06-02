/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 29/05/2026
* Ultima alteracao.: 02/06/2026
* Nome.............: SimulacaoController.java
* Funcao...........: Controller da tela simulacao.fxml.
*                    Liga os widgets JavaFX (ImageViews, Sliders,
*                    Buttons) ao modelo JantarFilosofos e as threads
*                    Filosofo, atualizando a GUI em tempo real.
************************************************************************ */

package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import model.EstadoFilosofo;
import model.JantarFilosofos;
import threads.Filosofo;
import util.Constantes;

import java.net.URL;
import java.util.ResourceBundle;

/* ***************************************************************
* Classe: SimulacaoController
* Funcao: Inicializa o modelo JantarFilosofos, cria as 5 threads
*         Filosofo, registra callbacks de estado para atualizar
*         imagens e garfos na GUI, e conecta sliders (velocidade)
*         e botoes (pausar/retomar e reset).
*************************************************************** */
public class SimulacaoController implements  Initializable {

  // Filosofos 
  @FXML private ImageView filosofo1; // Kant        - indice 0
  @FXML private ImageView filosofo2; // Nietzsche   - indice 1
  @FXML private ImageView filosofo3; // Platao      - indice 2
  @FXML private ImageView filosofo4; // Aristoteles - indice 3
  @FXML private ImageView filosofo5; // Socrates    - indice 4

  // Garfos
  @FXML private ImageView garfo1;
  @FXML private ImageView garfo2;
  @FXML private ImageView garfo3;
  @FXML private ImageView garfo4;
  @FXML private ImageView garfo5;

  // Sliders de Velocidade - Pensar
  @FXML private Slider sliderKantPensar;
  @FXML private Slider sliderNietzschePensar;
  @FXML private Slider sliderPlataoPensar;
  @FXML private Slider sliderAristotelesPensar;
  @FXML private Slider sliderSocratesPensar;
  
  // Sliders de Velocidade - Comer
  @FXML private Slider sliderKantComer;
  @FXML private Slider sliderNietzscheComer;
  @FXML private Slider sliderPlataoComer;
  @FXML private Slider sliderAristotelesComer;
  @FXML private Slider sliderSocratesComer;

  // Botoes de pausar/retomar de cada Filosofo
  @FXML private Button pausarKant;
  @FXML private Button pausarNietzsche;
  @FXML private Button pausarPlatao;
  @FXML private Button pausarAristoteles;
  @FXML private Button pausarSocrates;
  @FXML private Button btnReset;

  // Modelo e Threads
  private JantarFilosofos jantar;
  private Filosofo[] filosofos;

  // Imagens pre-carregadas + Estado do Filosofo (0 = PENSANDO, 1 = FAMINTO, 2 = COMENDO)
  private final Image[][] imagens = new Image[Constantes.N][3];

  // Matriz com as dimensoes e posicoes dos filosofos
  // { fitWidth, fitHeight, layoutX, layoutY }
  private static final double[][][] LAYOUT = {
    { // Kant — indice 0
      { 204, 173, 411, 25  },  // PENSANDO
      { 64,  120, 411, 78  },  // FAMINTO 
      { 107, 134, 391, 70  }   // COMENDO 
    },
    { // Nietzsche — indice 1
      { 204, 173, 566, 151 },  // PENSANDO
      { 70,  120, 567, 205 },  // FAMINTO 
      { 110, 138, 550, 192 }   // COMENDO 
    },
    { // Platao — indice 2
      { 204, 173, 530, 324 },  // PENSANDO
      { 70,  120, 530, 378 },  // FAMINTO 
      { 101, 148, 509, 362 }   // COMENDO 
    },
    { // Aristoteles — indice 3
      { 204, 173, 193, 339 },  // PENSANDO
      { 67,  130, 290, 393 },  // FAMINTO 
      { 131, 140, 267, 380 }   // COMENDO    
    },
    { // Socrates — indice 4
      { 204, 173, 143, 151 },  // PENSANDO
      { 76,  120, 241, 204 },  // FAMINTO 
      { 98,  145, 225, 181 }   // COMENDO 
    }
  };

  /* ***************************************************************
  * Metodo: initialize
  * Funcao: Ponto de entrada do JavaFX apos o FXML ser carregado.
  *         Orquestra toda a inicializacao da tela.
  * Parametros: @param url  nao utilizado
  *             @param rb   nao utilizado
  * Retorno: void
  *************************************************************** */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    carregarImagens();
    inicializarModelo();
    configurarCallbacksDeEstado();
    configurarSliders();
    configurarBotoes();
    iniciarThreads();
  } // Fim do metodo initialize

  /* ***************************************************************
  * Metodo: carregarImagens
  * Funcao: Pre-carrega as 3 imagens (pensando, faminto, comendo)
  *         de cada filosofo para troca rapida durante a simulacao.
  *         Convencao de nome: /img/<nome>_pensando.png, etc.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void carregarImagens() {
    // [0] = PENSANDO, [1] = FAMINTO, [2] = COMENDO
    String[][] arquivos = {
        { "kant_pensando.png",        "kant.png",        "kant_comendo.png"        },
        { "nietzsche_pensando.png",   "nietzsche.png",   "nietzsche_comendo.png"   },
        { "platao_pensando.png",      "platao.png",      "platao_comendo.png"      },
        { "aristoteles_pensando.png", "aristoteles.png", "aristoteles_comendo.png" },
        { "socrates_pensando.png",    "socrates.png",    "socrates_comendo.png"    }
    };

    for (int i = 0; i < Constantes.N; i++) {
        for (int e = 0; e < 3; e++) {
            imagens[i][e] = new Image(getClass().getResourceAsStream("/img/" + arquivos[i][e]));
        }
    }
  } // Fim do metodo carregarImagens

  /* ***************************************************************
  * Metodo: inicializarModelo
  * Funcao: Cria o JantarFilosofos (semaforos) e as 5 threads
  *         Filosofo com velocidade padrao Constantes.DEFAULT_SPEED_MS.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void inicializarModelo() {
    jantar = new JantarFilosofos();
    filosofos = new Filosofo[Constantes.N];

    for (int i = 0; i < Constantes.N; i++) {
      filosofos[i] = new Filosofo(
        i, 
        jantar,
        Constantes.DEFAULT_SPEED_MS,  // Pensar
        Constantes.DEFAULT_SPEED_MS   // Comer
      );
    }
  } // Fim do metodo inicializarModelo

  /* ***************************************************************
  * Metodo: configurarCallbacksDeEstado
  * Funcao: Registra em JantarFilosofos um Runnable para cada
  *         filosofo. Cada Runnable e chamado via Platform.runLater
  *         sempre que o estado muda, atualizando a imagem do
  *         filosofo e a visibilidade dos garfos vizinhos.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void configurarCallbacksDeEstado() {
    ImageView[] views = { filosofo1, filosofo2, filosofo3, filosofo4, filosofo5 };

    for (int i = 0; i < Constantes.N; i++) {
      final int idx = i;
      jantar.setOnEstadoMudou(idx, () -> {
        atualizarImagemFilosofo(idx, views[idx]);
        atualizarGarfos();
      });
    }
  } // Fim do metodo configurarCallbacksDeEstado

  /* ***************************************************************
  * Metodo: atualizarImagemFilosofo
  * Funcao: Troca a imagem do ImageView de acordo com o estado
  *         atual do filosofo i no modelo.
  * Parametros: @param i    indice do filosofo
  *             @param view ImageView correspondente
  * Retorno: void
  *************************************************************** */
  private void atualizarImagemFilosofo(int i, ImageView view) {
    EstadoFilosofo estado = jantar.getEstado(i);
    int idxImagem;

    switch (estado) {
      case FAMINTO -> idxImagem = 1;  // FAMINTO
      case COMENDO -> idxImagem = 2;  // COMENDO
      default -> idxImagem = 0;       // PENSANDO
    }

    double[] layout = LAYOUT[i][idxImagem];

    view.setImage(imagens[i][idxImagem]);
    view.setFitWidth(layout[0]);
    view.setFitHeight(layout[1]);
    view.setLayoutX(layout[2]);
    view.setLayoutY(layout[3]);
    view.setPreserveRatio(true);
  } // Fim do metodo atualizarImagemFilosofo

  /* ***************************************************************
  * Metodo: atualizarGarfos
  * Funcao: Esconde o garfo entre dois filosofos quando algum deles
  *         esta COMENDO (garfo em uso) e exibe quando nenhum dos
  *         dois esta comendo.
  *         Mapeamento: garfo[i] fica entre filosofo[i] e
  *         filosofo[direita(i)], seguindo a ordem circular.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void atualizarGarfos() {
    ImageView[] garfos = { garfo1, garfo2, garfo3, garfo4, garfo5 };

    for (int i = 0; i < Constantes.N; i++) {
      int dir = Constantes.direita(i);
      boolean emUso = jantar.getEstado(i) == EstadoFilosofo.COMENDO || jantar.getEstado(dir) == EstadoFilosofo.COMENDO;
      garfos[i].setVisible(!emUso);
    }
  } // Fim do metodo atualizarGarfos

  /* ***************************************************************
  * Metodo: configurarSliders
  * Funcao: Define min/max/valor inicial dos 10 sliders e registra
  *         listeners que atualizam pensarMs/comerMs das threads em
  *         tempo real conforme o usuario arrasta o slider.
  *         Faixa: Constantes.MIN_SPEED_MS a Constantes.MAX_SPEED_MS
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void configurarSliders() {
    Slider[] slidersPensar = {
      sliderKantPensar, 
      sliderNietzschePensar, 
      sliderPlataoPensar,
      sliderAristotelesPensar, 
      sliderSocratesPensar
    };

    Slider[] slidersComer = {
      sliderKantComer, 
      sliderNietzscheComer, 
      sliderPlataoComer,
      sliderAristotelesComer, 
      sliderSocratesComer
    };

    for (int i = 0; i < Constantes.N; i++) {
      final int idx = i;

      Slider sp = slidersPensar[i];
      sp.setMin(Constantes.MIN_SPEED_MS);
      sp.setMax(Constantes.MAX_SPEED_MS);
      sp.setValue(Constantes.DEFAULT_SPEED_MS);
      sp.valueProperty().addListener((obs, antigo, novo) -> 
        filosofos[idx].setComerMs(novo.intValue())
      );

      Slider sc = slidersComer[i];
      sc.setMin(Constantes.MIN_SPEED_MS);
      sc.setMax(Constantes.MAX_SPEED_MS);
      sc.setValue(Constantes.DEFAULT_SPEED_MS);
      sc.valueProperty().addListener((obs, antigo, novo) ->
        filosofos[idx].setComerMs(novo.intValue())
      );
    }
  } // Fim do metodo configurarSliders

  /* ***************************************************************
  * Metodo: configurarBotoes
  * Funcao: Registra os handlers de pausar/retomar individuais e
  *         de reset global diretamente nos campos @FXML injetados.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void configurarBotoes() {
    Button[] btns = {
      pausarKant, 
      pausarNietzsche, 
      pausarPlatao,
      pausarAristoteles, 
      pausarSocrates
    };

    for (int i = 0; i < Constantes.N; i++) {
      final int idx = i;
      btns[i].setOnAction(e -> alternarPausa(idx, btns[idx]));
    }

    btnReset.setOnAction(e -> executarReset());
  } // Fim do metodo configurarBotoes

   /* ***************************************************************
  * Metodo: alternarPausa
  * Funcao: Pausa o filosofo se estiver rodando, ou retoma se
  *         estiver pausado. Atualiza o texto do botao para
  *         refletir o estado atual.
  * Parametros: @param idx indice do filosofo
  *             @param btn botao correspondente
  * Retorno: void
  *************************************************************** */
  private void alternarPausa(int idx, Button btn) {
    if (filosofos[idx].isPausado()) {
      filosofos[idx].retomar();
      btn.setText("PAUSAR");
    } else {
      filosofos[idx].pausar();
      btn.setText("RETOMAR");
    }
  } // Fim do metodo alternarPausa

  /* ***************************************************************
  * Metodo: executarReset
  * Funcao: Para todas as threads, reinicia o modelo e recria as
  *         threads do zero, garantindo estado limpo.
  *         Passos: (1) interrompe threads; (2) reseta semaforos;
  *                 (3) recria threads e callbacks; (4) reinicia
  *                 sliders; (5) restaura texto dos botoes.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void executarReset() {
    // Para as Threads em execucao
    for (Filosofo f : filosofos) {
      f.retomar();
      f.interrupt();
    }

    // Reinicia semaforos e estados 
    jantar.reset();

    // Recria Threads e registra callbacks
    inicializarModelo();
    configurarCallbacksDeEstado();
    configurarSliders();
    iniciarThreads();

    // Restaura texto dos botoes
    Button[] btns = {
      pausarKant, 
      pausarNietzsche, 
      pausarPlatao,
      pausarAristoteles, 
      pausarSocrates
    };

    for (Button b : btns) {
      b.setText("PAUSAR");
    }
  } // Fim do metodo executarReset

  /* ***************************************************************
  * Metodo: iniciarThreads
  * Funcao: Inicia as 5 threads Filosofo. Chamado uma vez no
  *         initialize() e novamente apos cada reset.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void iniciarThreads() {
    for (Filosofo f : filosofos) {
      f.start();
    }
  } // Fim do metodo iniciarThreads

} // Fim da classe SimulacaoController
