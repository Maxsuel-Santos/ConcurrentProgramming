/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 10/06/2026
* Ultima alteracao.: 15/06/2026
* Nome.............: SimulacaoController.java
* Funcao...........: Controller JavaFX da tela de simulacao. Gerencia
*                    o ciclo de vida das threads leitoras e escritoras,
*                    vincula sliders e botoes de pausa a cada thread,
*                    e atualiza a GUI via Platform.runLater conforme
*                    as mudancas de estado notificadas pelas threads.
************************************************************************ */

package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import model.BaseDeDados;
import model.EstadoThread;
import threads.ThreadEscritor;
import threads.ThreadLeitor;
import util.Constantes;

import java.net.URL;
import java.util.ResourceBundle;

/* ***************************************************************
* Classe: SimulacaoController
* Funcao: Controller da tela Simulacao.fxml. Inicializa e controla
*         as 5 threads leitoras e 5 escritoras, conecta os
*         componentes FXML as threads e atualiza imagens, labels
*         e estilos dos cards conforme o estado de cada thread.
*************************************************************** */
public class SimulacaoController implements Initializable {

  // Cabecalho
  @FXML private Label lblEdicao;
  @FXML private Label lblLeitoresAtivos;

  // Base de Dados
  @FXML private ImageView imgJornal;
  @FXML private Label     lblConteudoBase;
  @FXML private Button    btnReset;

  // LEITORES (Editores)
  @FXML private ImageView imgLeitor1;
  @FXML private ImageView imgLeitor2;
  @FXML private ImageView imgLeitor3;
  @FXML private ImageView imgLeitor4;
  @FXML private ImageView imgLeitor5;

  @FXML private Label lblEstadoLeitor1;
  @FXML private Label lblEstadoLeitor2;
  @FXML private Label lblEstadoLeitor3;
  @FXML private Label lblEstadoLeitor4;
  @FXML private Label lblEstadoLeitor5;

  @FXML private Slider sliderLeitLeitura1;
  @FXML private Slider sliderLeitLeitura2;
  @FXML private Slider sliderLeitLeitura3;
  @FXML private Slider sliderLeitLeitura4;
  @FXML private Slider sliderLeitLeitura5;

  @FXML private Slider sliderLeitUtiliza1;
  @FXML private Slider sliderLeitUtiliza2;
  @FXML private Slider sliderLeitUtiliza3;
  @FXML private Slider sliderLeitUtiliza4;
  @FXML private Slider sliderLeitUtiliza5;

  @FXML private Button btnPausaLeitor1;
  @FXML private Button btnPausaLeitor2;
  @FXML private Button btnPausaLeitor3;
  @FXML private Button btnPausaLeitor4;
  @FXML private Button btnPausaLeitor5;

  // ESCRITORES (Reposters)
  @FXML private ImageView imgEscritor1;
  @FXML private ImageView imgEscritor2;
  @FXML private ImageView imgEscritor3;
  @FXML private ImageView imgEscritor4;
  @FXML private ImageView imgEscritor5;

  @FXML private Label lblEstadoEscritor1;
  @FXML private Label lblEstadoEscritor2;
  @FXML private Label lblEstadoEscritor3;
  @FXML private Label lblEstadoEscritor4;
  @FXML private Label lblEstadoEscritor5;

  @FXML private Slider sliderEscObtencao1;
  @FXML private Slider sliderEscObtencao2;
  @FXML private Slider sliderEscObtencao3;
  @FXML private Slider sliderEscObtencao4;
  @FXML private Slider sliderEscObtencao5;

  @FXML private Slider sliderEscEscrita1;
  @FXML private Slider sliderEscEscrita2;
  @FXML private Slider sliderEscEscrita3;
  @FXML private Slider sliderEscEscrita4;
  @FXML private Slider sliderEscEscrita5;

  @FXML private Button btnPausaEscritor1;
  @FXML private Button btnPausaEscritor2;
  @FXML private Button btnPausaEscritor3;
  @FXML private Button btnPausaEscritor4;
  @FXML private Button btnPausaEscritor5;

  // Log
  @FXML private TextArea areaLog;

  // Estado interno
  private BaseDeDados base;
  private ThreadLeitor[] leitores = new ThreadLeitor[Constantes.NUM_LEITORES + 1];
  private ThreadEscritor[] escritores = new ThreadEscritor[Constantes.NUM_ESCRITORES + 1];

  // Arrays para acesso indexado aos componentes da GUI (indice 0 ignorado)
  private ImageView[] imgLeitores;
  private ImageView[] imgEscritores;
  private Label[] lblEstadoLeitores;
  private Label[] lblEstadoEscritores;
  private Button[] btnsPausaLeitor;
  private Button[] btnsPausaEscritor;
  private Slider[] slidersLeitLeitura;
  private Slider[] slidersLeitUtiliza;
  private Slider[] slidersEscObtencao;
  private Slider[] slidersEscEscrita;

  // Cache de imagens carregadas uma unica vez na inicializacao
  private Image[] imgsCacheLeitores;
  private Image[] imgsCacheEscritores;
  private Image imgJornalFechado;
  private Image imgJornalAberto;

  // Indices do array de cache por estado
  private static final int IDX_OCIOSO = 0;
  private static final int IDX_AGUARDANDO = 1;
  private static final int IDX_ATIVO = 2;
  private static final int IDX_PAUSADO = 3;

  /* ***************************************************************
  * Metodo: initialize
  * Funcao: Chamado automaticamente pelo FXMLLoader apos injetar
  *         os componentes. Monta os arrays de componentes, carrega
  *         o cache de imagens e inicia a simulacao.
  * Parametros: url - localizacao do FXML (nao utilizado)
  *             rb  - recursos de internacionalizacao (nao utilizado)
  * Retorno: void
  *************************************************************** */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    montarArrays();
    carregarImagens();
    iniciarSimulacao();
  } // Fim do metodo initialize

  /* ***************************************************************
  * Metodo: montarArrays
  * Funcao: Agrupa os componentes FXML em arrays indexados por id
  *         de thread (1 a 5), facilitando o acesso programatico
  *         sem precisar de switch/if por numero de thread.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void montarArrays() {
    imgLeitores = new ImageView[] {
      null, imgLeitor1, imgLeitor2, imgLeitor3, imgLeitor4, imgLeitor5
    };

    imgEscritores = new ImageView[] {
      null, imgEscritor1, imgEscritor2, imgEscritor3, imgEscritor4, imgEscritor5
    };

    lblEstadoLeitores = new Label[] {
      null, lblEstadoLeitor1, lblEstadoLeitor2, lblEstadoLeitor3, lblEstadoLeitor4, lblEstadoLeitor5
    };

    lblEstadoEscritores = new Label[] {
      null, lblEstadoEscritor1, lblEstadoEscritor2, lblEstadoEscritor3, lblEstadoEscritor4, lblEstadoEscritor5
    };

    btnsPausaLeitor = new Button[] {
      null, btnPausaLeitor1, btnPausaLeitor2, btnPausaLeitor3, btnPausaLeitor4, btnPausaLeitor5
    };

    btnsPausaEscritor = new Button[] {
      null, btnPausaEscritor1, btnPausaEscritor2, btnPausaEscritor3, btnPausaEscritor4, btnPausaEscritor5
    };

    slidersLeitLeitura = new Slider[] {
      null, sliderLeitLeitura1, sliderLeitLeitura2, sliderLeitLeitura3, sliderLeitLeitura4, sliderLeitLeitura5
    };

    slidersLeitUtiliza = new Slider[] {
      null, sliderLeitUtiliza1, sliderLeitUtiliza2, sliderLeitUtiliza3, sliderLeitUtiliza4, sliderLeitUtiliza5
    };

    slidersEscObtencao = new Slider[] {
      null, sliderEscObtencao1, sliderEscObtencao2, sliderEscObtencao3, sliderEscObtencao4, sliderEscObtencao5
    };

    slidersEscEscrita = new Slider[] {
      null, sliderEscEscrita1, sliderEscEscrita2, sliderEscEscrita3, sliderEscEscrita4, sliderEscEscrita5
    };

  } // Fim do metodo montarArrays

  /* ***************************************************************
  * Metodo: carregarImagens
  * Funcao: Carrega todas as imagens de personagens e do jornal
  *         uma unica vez na inicializacao e armazena em cache,
  *         evitando leituras repetidas do disco durante a simulacao.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void carregarImagens() {
    imgsCacheLeitores = new Image[] {
      new Image(getClass().getResourceAsStream(Constantes.IMG_EDITOR_OCIOSO)),
      new Image(getClass().getResourceAsStream(Constantes.IMG_EDITOR_AGUARDANDO)),
      new Image(getClass().getResourceAsStream(Constantes.IMG_EDITOR_LENDO)),
      new Image(getClass().getResourceAsStream(Constantes.IMG_EDITOR_PAUSADO))
    };

    imgsCacheEscritores = new Image[] {
      new Image(getClass().getResourceAsStream(Constantes.IMG_REPORTER_OCIOSO)),
      new Image(getClass().getResourceAsStream(Constantes.IMG_REPORTER_AGUARDANDO)),
      new Image(getClass().getResourceAsStream(Constantes.IMG_REPORTER_ESCREVENDO)),
      new Image(getClass().getResourceAsStream(Constantes.IMG_REPORTER_PAUSADO))
    };

    imgJornalFechado = new Image(getClass().getResourceAsStream(Constantes.IMG_JORNAL_FECHADO));
    imgJornalAberto = new Image(getClass().getResourceAsStream(Constantes.IMG_JORNAL_ABERTO));
  } // Fim do metodo carregarImagens

  /* ***************************************************************
  * Metodo: iniciarSimulacao
  * Funcao: Instancia a base de dados e cria as 5 threads leitoras
  *         e 5 escritoras, vinculando os sliders de velocidade a
  *         cada thread via listener e iniciando a execucao.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void iniciarSimulacao() {
    base = new BaseDeDados();

    for (int i = 1; i <= Constantes.NUM_LEITORES; i++) {
      final int id = i;

      ThreadLeitor tl = new ThreadLeitor(id, base, (tid, estado) -> Platform.runLater(() -> atualizarLeitor(tid, estado)));

      vincularSlidersLeitor(id, tl);
      leitores[id] = tl;
      tl.start();

      ThreadEscritor te = new ThreadEscritor(id, base, (tid, estado) -> Platform.runLater(() -> atualizarEscritor(tid, estado)));

      vincularSlidersEscritor(id, te);
      escritores[id] = te;
      te.start();
    }

    log("Redacao iniciada. 5 editores e 5 reporters em operacao.");
  } // Fim do metodo iniciarSimulacao

  /* ***************************************************************
  * Metodo: vincularSlidersLeitor
  * Funcao: Registra listeners nos sliders de velocidade do leitor
  *         para propagar alteracoes diretamente a thread em tempo
  *         real, sem necessidade de botao confirmar.
  * Parametros: id - id do leitor
  *             tl - referencia a ThreadLeitor correspondente
  * Retorno: void
  *************************************************************** */
  private void vincularSlidersLeitor(int id, ThreadLeitor tl) {
    slidersLeitLeitura[id].valueProperty().addListener((obs, ov, nv) -> tl.setVelLeitura(nv.longValue()));

    slidersLeitUtiliza[id].valueProperty().addListener((obs, ov, nv) -> tl.setVelUtilizacao(nv.longValue()));
  } // Fim do metodo vincularSlidersLeitor

  /* ***************************************************************
  * Metodo: vincularSlidersEscritor
  * Funcao: Registra listeners nos sliders de velocidade do escritor
  *         para propagar alteracoes diretamente a thread em tempo
  *         real, sem necessidade de botao confirmar.
  * Parametros: id - id do escritor
  *             te - referencia a ThreadEscritor correspondente
  * Retorno: void
  *************************************************************** */
  private void vincularSlidersEscritor(int id, ThreadEscritor te) {
    slidersEscObtencao[id].valueProperty().addListener((obs, ov, nv) -> te.setVelObtencao(nv.longValue()));

    slidersEscEscrita[id].valueProperty().addListener((obs, ov, nv) -> te.setVelEscrita(nv.longValue()));
  } // Fim do metodo vincularSlidersEscritor

  /* ***************************************************************
  * Metodo: handleReset
  * Funcao: Para todas as threads em execucao, reseta o visual da
  *         GUI para o estado inicial e reinicia a simulacao com
  *         uma nova instancia da base de dados.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  @FXML
  private void handleReset() {
    log("--- RESET: encerrando threads ---");

    for (int i = 1; i <= Constantes.NUM_LEITORES; i++) {
      if (leitores[i] != null) {
        leitores[i].parar();
      }

      if (escritores[i] != null) {
        escritores[i].parar();
      }
    }

    resetarVisual();
    iniciarSimulacao();

    log("Redacao reiniciada.");
  } // Fim do metodo handleReset

  /* ***************************************************************
  * Metodo: resetarVisual
  * Funcao: Restaura todos os componentes da GUI para o estado
  *         inicial (ocioso), limpando labels, imagens e estilos
  *         dos cards de leitores e escritores.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void resetarVisual() {
    for (int i = 1; i <= Constantes.NUM_LEITORES; i++) {
      aplicarEstadoLeitorGUI(i, EstadoThread.OCIOSO);
      aplicarEstadoEscritorGUI(i, EstadoThread.OCIOSO);
      btnsPausaLeitor[i].setText("Pausar");
      btnsPausaEscritor[i].setText("Pausar");
    }

    lblConteudoBase.setText("Nenhuma materia publicada ainda.");
    lblEdicao.setText("Edicao n. 0");
    lblLeitoresAtivos.setText("Editores lendo: 0");
    imgJornal.setImage(imgJornalFechado);
  } // Fim do metodo resetarVisual

  
  // PAUSA/RETOMADA - LEITORES     
  @FXML private void handlePausaLeitor1() { togglePausaLeitor(1); }
  @FXML private void handlePausaLeitor2() { togglePausaLeitor(2); }
  @FXML private void handlePausaLeitor3() { togglePausaLeitor(3); }
  @FXML private void handlePausaLeitor4() { togglePausaLeitor(4); }
  @FXML private void handlePausaLeitor5() { togglePausaLeitor(5); }

  /* ***************************************************************
  * Metodo: togglePausaLeitor
  * Funcao: Alterna o estado de pausa de um leitor especifico.
  *         Se pausado, retoma; se rodando, pausa. Atualiza o
  *         texto do botao correspondente e registra no log.
  * Parametros: id - id do leitor a alternar
  * Retorno: void
  *************************************************************** */
  private void togglePausaLeitor(int id) {
    ThreadLeitor t = leitores[id];

    if (t == null) {
      return;
    }

    if (t.isPausado()) {
      t.retomar();
      btnsPausaLeitor[id].setText("Pausar");

      log("Editor " + id + " retomado.");
    } 
    else {
      t.pausar();
      btnsPausaLeitor[id].setText("Retomar");

      log("Editor " + id + " pausado.");
    }
  } // Fim do metodo togglePausaLeitor

  // PAUSA/RETOMADA - ESCRITORES
  @FXML private void handlePausaEscritor1() { togglePausaEscritor(1); }
  @FXML private void handlePausaEscritor2() { togglePausaEscritor(2); }
  @FXML private void handlePausaEscritor3() { togglePausaEscritor(3); }
  @FXML private void handlePausaEscritor4() { togglePausaEscritor(4); }
  @FXML private void handlePausaEscritor5() { togglePausaEscritor(5); }

  /* ***************************************************************
  * Metodo: togglePausaEscritor
  * Funcao: Alterna o estado de pausa de um escritor especifico.
  *         Se pausado, retoma; se rodando, pausa. Atualiza o
  *         texto do botao correspondente e registra no log.
  * Parametros: id - id do escritor a alternar
  * Retorno: void
  *************************************************************** */
  private void togglePausaEscritor(int id) {
    ThreadEscritor t = escritores[id];

    if (t == null) {
      return;
    }

    if (t.isPausado()) {
      t.retomar();
      btnsPausaEscritor[id].setText("Pausar");
    
      log("Reporter " + id + " retomado.");
    } 
    else {
      t.pausar();
      btnsPausaEscritor[id].setText("Retomar");
    
      log("Reporter " + id + " pausado.");
    }
  } // Fim do metodo togglePausaEscritor

  /* ***************************************************************
  * Metodo: atualizarLeitor
  * Funcao: Atualiza os componentes visuais de um leitor conforme
  *         seu novo estado. Tambem atualiza o conteudo da base e
  *         a imagem do jornal quando o leitor entra na regiao critica.
  *         Deve ser chamado dentro de Platform.runLater.
  * Parametros: id     - id do leitor
  *             estado - novo EstadoThread notificado pela thread
  * Retorno: void
  *************************************************************** */
  private void atualizarLeitor(int id, EstadoThread estado) {
    aplicarEstadoLeitorGUI(id, estado);

    if (estado == EstadoThread.ATIVO) {
      lblConteudoBase.setText(base.leBaseDeDados());
      imgJornal.setImage(imgJornalAberto);

      log("Editor " + id + " entrou na base (leitura).");
    } 
    else if (estado == EstadoThread.OCIOSO) {
      if (base.getNumLeitores() == 0) {
        imgJornal.setImage(imgJornalFechado);
      }
    }

    lblLeitoresAtivos.setText("Editores lendo: " + base.getNumLeitores());
  } // Fim do metodo atualizarLeitor

  /* ***************************************************************
  * Metodo: aplicarEstadoLeitorGUI
  * Funcao: Troca a imagem do personagem, o texto e estilo do label
  *         de estado e a borda do card do leitor de acordo com o
  *         EstadoThread recebido.
  * Parametros: id     - id do leitor
  *             estado - EstadoThread a aplicar visualmente
  * Retorno: void
  *************************************************************** */
  private void aplicarEstadoLeitorGUI(int id, EstadoThread estado) {
    switch (estado) {
      case ATIVO:
        imgLeitores[id].setImage(imgsCacheLeitores[IDX_ATIVO]);
        setEstado(lblEstadoLeitores[id], "Lendo manchete", "estado-ativo");
        setCardStyle(imgLeitores[id], "card-ativo");
        break;
      case AGUARDANDO:
        imgLeitores[id].setImage(imgsCacheLeitores[IDX_AGUARDANDO]);
        setEstado(lblEstadoLeitores[id], "Aguardando acesso", "estado-aguardando");
        setCardStyle(imgLeitores[id], "card-aguardando");
        break;
      case PAUSADO:
        imgLeitores[id].setImage(imgsCacheLeitores[IDX_PAUSADO]);
        setEstado(lblEstadoLeitores[id], "Pausado", "estado-pausado");
        setCardStyle(imgLeitores[id], "card-pausado");
        break;
      default:
        imgLeitores[id].setImage(imgsCacheLeitores[IDX_OCIOSO]);
        setEstado(lblEstadoLeitores[id], "Ocioso", "estado-ocioso");
        setCardStyle(imgLeitores[id], "card-personagem");
        break;
    }
  } // Fim do metodo aplicarEstadoLeitorGUI

  /* ***************************************************************
  * Metodo: atualizarEscritor
  * Funcao: Atualiza os componentes visuais de um escritor conforme
  *         seu novo estado. Tambem atualiza o conteudo da base, o
  *         numero da edicao e a imagem do jornal quando o escritor
  *         esta na regiao critica. Deve ser chamado em runLater.
  * Parametros: id     - id do escritor
  *             estado - novo EstadoThread notificado pela thread
  * Retorno: void
  *************************************************************** */
  private void atualizarEscritor(int id, EstadoThread estado) {
    aplicarEstadoEscritorGUI(id, estado);

    if (estado == EstadoThread.ATIVO) {
      lblConteudoBase.setText(base.leBaseDeDados());
      lblEdicao.setText("Edicao n. " + base.getEdicao());
      imgJornal.setImage(imgJornalAberto);
      
      log("Reporter " + id + " publicou - Edicao " + base.getEdicao());
    } 
    else if (estado == EstadoThread.OCIOSO) {
      imgJornal.setImage(imgJornalFechado);
    }
  } // Fim do metodo atualizarEscritor

  /* ***************************************************************
  * Metodo: aplicarEstadoEscritorGUI
  * Funcao: Troca a imagem do personagem, o texto e estilo do label
  *         de estado e a borda do card do escritor de acordo com o
  *         EstadoThread recebido.
  * Parametros: id     - id do escritor
  *             estado - EstadoThread a aplicar visualmente
  * Retorno: void
  *************************************************************** */
  private void aplicarEstadoEscritorGUI(int id, EstadoThread estado) {
    switch (estado) {
      case ATIVO:
        imgEscritores[id].setImage(imgsCacheEscritores[IDX_ATIVO]);
        setEstado(lblEstadoEscritores[id], "Publicando materia", "estado-ativo");
        setCardStyle(imgEscritores[id], "card-ativo");
        break;
      case AGUARDANDO:
        imgEscritores[id].setImage(imgsCacheEscritores[IDX_AGUARDANDO]);
        setEstado(lblEstadoEscritores[id], "Aguardando acesso", "estado-aguardando");
        setCardStyle(imgEscritores[id], "card-aguardando");
        break;
      case PAUSADO:
        imgEscritores[id].setImage(imgsCacheEscritores[IDX_PAUSADO]);
        setEstado(lblEstadoEscritores[id], "Pausado", "estado-pausado");
        setCardStyle(imgEscritores[id], "card-pausado");
        break;
      default:
        imgEscritores[id].setImage(imgsCacheEscritores[IDX_OCIOSO]);
        setEstado(lblEstadoEscritores[id], "Apurando pauta", "estado-ocioso");
        setCardStyle(imgEscritores[id], "card-personagem");
        break;
    }
  } // Fim do metodo aplicarEstadoEscritorGUI

  /* ***************************************************************
  * Metodo: setEstado
  * Funcao: Atualiza o texto e a styleClass CSS de um label de
  *         estado, removendo as classes de estado anteriores antes
  *         de adicionar a nova, evitando acumulo de estilos.
  * Parametros: lbl        - Label a atualizar
  *             texto      - novo texto a exibir
  *             styleClass - classe CSS a aplicar
  * Retorno: void
  *************************************************************** */
  private void setEstado(Label lbl, String texto, String styleClass) {
    lbl.setText(texto);
    lbl.getStyleClass().removeAll("estado-ocioso", "estado-ativo", "estado-aguardando", "estado-pausado");
    lbl.getStyleClass().add(styleClass);
  } // Fim do metodo setEstado

  /* ***************************************************************
  * Metodo: setCardStyle
  * Funcao: Atualiza a styleClass CSS do VBox card do personagem.
  *         Sobe dois niveis na hierarquia: ImageView -> HBox -> VBox.
  *         Trata excecoes silenciosamente para nao travar a simulacao
  *         em caso de hierarquia de nos inesperada.
  * Parametros: img        - ImageView do personagem dentro do card
  *             styleClass - classe CSS a aplicar no VBox pai
  * Retorno: void
  *************************************************************** */
  private void setCardStyle(ImageView img, String styleClass) {
    try {
      VBox card = (VBox) img.getParent().getParent();
      card.getStyleClass().removeAll("card-personagem", "card-ativo", "card-aguardando", "card-pausado");
      card.getStyleClass().add(styleClass);
    } catch (Exception e) {
      // Hierarquia inesperada: ignora sem travar a simulacao
    }
  } // Fim do metodo setCardStyle

  /* ***************************************************************
  * Metodo: log
  * Funcao: Acrescenta uma mensagem ao TextArea de log da GUI,
  *         garantindo execucao na thread JavaFX via runLater e
  *         rolando automaticamente para a ultima linha.
  * Parametros: msg - mensagem a registrar no log
  * Retorno: void
  *************************************************************** */
  private void log(String msg) {
    Platform.runLater(() -> {
      areaLog.appendText(msg + "\n");
      areaLog.setScrollTop(Double.MAX_VALUE);
    });
  }

} // Fim da classe SimulacaoController
