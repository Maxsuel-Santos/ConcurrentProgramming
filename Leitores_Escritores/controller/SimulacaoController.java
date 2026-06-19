/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 10/06/2026
* Ultima alteracao.: 17/06/2026
* Nome.............: SimulacaoController.java
* Funcao...........: Controller JavaFX da tela de simulacao. Gerencia
*                    o ciclo de vida das threads leitoras e escritoras
*                    executando os metodos leitor()/escritor() da
*                    classe LeitoresEscritores, vincula sliders e
*                    botoes de pausa, e atualiza a GUI via
*                    Platform.runLater conforme os callbacks da
*                    classe LeitoresEscritores.
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

import model.LeitoresEscritores;
import util.Constantes;

import java.net.URL;
import java.util.ResourceBundle;

/* ***************************************************************
* Classe: SimulacaoController
* Funcao: Controller da tela Simulacao.fxml. Inicializa e controla
*         as 5 threads leitoras e 5 escritoras executando os
*         metodos leitor()/escritor() de LeitoresEscritores, conecta
*         os componentes FXML aos callbacks e atualiza imagens,
*         labels e estilos dos cards conforme o estado de cada thread.
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

  // ESCRITORES (Reporters)
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
  private LeitoresEscritores le;
  private Thread[] threadsLeitores = new Thread[Constantes.NUM_LEITORES + 1];
  private Thread[] threadsEscritores = new Thread[Constantes.NUM_ESCRITORES + 1];

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
  * Funcao: Instancia LeitoresEscritores, registra os callbacks que
  *         atualizam a GUI via Platform.runLater, vincula os
  *         sliders de velocidade e cria as 5 threads leitoras e
  *         5 escritoras executando le.leitor(id) e le.escritor(id).
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void iniciarSimulacao() {
    le = new LeitoresEscritores();
    registrarCallbacks();

    for (int i = 1; i <= Constantes.NUM_LEITORES; i++) {
      final int id = i;

      vincularSlidersLeitor(id);
      vincularSlidersEscritor(id);

      Thread tl = new Thread(() -> le.leitor(id));
      tl.setDaemon(true);
      tl.setName("Editor-" + id);
      threadsLeitores[id] = tl;
      tl.start();

      Thread te = new Thread(() -> le.escritor(id));
      te.setDaemon(true);
      te.setName("Reporter-" + id);
      threadsEscritores[id] = te;
      te.start();
    }

    log("Redacao iniciada. 5 editores e 5 reporters em operacao.");
  } // Fim do metodo iniciarSimulacao

  /* ***************************************************************
  * Metodo: registrarCallbacks
  * Funcao: Registra em LeitoresEscritores os callbacks que serao
  *         disparados pelas threads ao mudar de estado. Cada
  *         callback executa na thread leitora/escritora e por isso
  *         repassa a atualizacao da GUI via Platform.runLater.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void registrarCallbacks() {
    le.setOnLeitorAguardando(id -> Platform.runLater(() -> atualizarLeitorAguardando(id)));
    le.setOnLeitorLendo(id -> Platform.runLater(() -> atualizarLeitorLendo(id)));
    le.setOnLeitorOcioso(id -> Platform.runLater(() -> atualizarLeitorOcioso(id)));
    le.setOnLeitorPausado(id -> Platform.runLater(() -> aplicarEstadoLeitorGUI(id, "PAUSADO")));

    le.setOnEscritorAguardando(id -> Platform.runLater(() -> aplicarEstadoEscritorGUI(id, "AGUARDANDO")));
    le.setOnEscritorEscrevendo(id -> Platform.runLater(() -> atualizarEscritorEscrevendo(id)));
    le.setOnEscritorOcioso(id -> Platform.runLater(() -> atualizarEscritorOcioso(id)));
    le.setOnEscritorPausado(id -> Platform.runLater(() -> aplicarEstadoEscritorGUI(id, "PAUSADO")));
  } // Fim do metodo registrarCallbacks

  /* ***************************************************************
  * Metodo: vincularSlidersLeitor
  * Funcao: Registra listeners nos sliders de velocidade do leitor
  *         para propagar alteracoes diretamente a LeitoresEscritores
  *         em tempo real, sem necessidade de botao confirmar.
  * Parametros: id - id do leitor
  * Retorno: void
  *************************************************************** */
  private void vincularSlidersLeitor(int id) {
    slidersLeitLeitura[id].valueProperty().addListener((obs, ov, nv) -> le.setVelLeitura(id, nv.longValue()));

    slidersLeitUtiliza[id].valueProperty().addListener((obs, ov, nv) -> le.setVelUtilizacao(id, nv.longValue()));
  } // Fim do metodo vincularSlidersLeitor

  /* ***************************************************************
  * Metodo: vincularSlidersEscritor
  * Funcao: Registra listeners nos sliders de velocidade do escritor
  *         para propagar alteracoes diretamente a LeitoresEscritores
  *         em tempo real, sem necessidade de botao confirmar.
  * Parametros: id - id do escritor
  * Retorno: void
  *************************************************************** */
  private void vincularSlidersEscritor(int id) {
    slidersEscObtencao[id].valueProperty().addListener((obs, ov, nv) -> le.setVelObtencao(id, nv.longValue()));

    slidersEscEscrita[id].valueProperty().addListener((obs, ov, nv) -> le.setVelEscrita(id, nv.longValue()));
  } // Fim do metodo vincularSlidersEscritor

  /* ***************************************************************
  * Metodo: handleReset
  * Funcao: Interrompe todas as threads em execucao, reseta o
  *         visual da GUI para o estado inicial e reinicia a
  *         simulacao com uma nova instancia de LeitoresEscritores.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  @FXML
  private void handleReset() {
    log("--- RESET: encerrando threads ---");

    for (int i = 1; i <= Constantes.NUM_LEITORES; i++) {
      if (threadsLeitores[i] != null) {
        threadsLeitores[i].interrupt();
      }

      if (threadsEscritores[i] != null) {
        threadsEscritores[i].interrupt();
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
      aplicarEstadoLeitorGUI(i, "OCIOSO");
      aplicarEstadoEscritorGUI(i, "OCIOSO");
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
  * Funcao: Alterna o estado de pausa de um leitor especifico via
  *         LeitoresEscritores. Se pausado, retoma; se rodando,
  *         pausa. Atualiza o texto do botao e registra no log.
  * Parametros: id - id do leitor a alternar
  * Retorno: void
  *************************************************************** */
  private void togglePausaLeitor(int id) {
    if (le.isPausadoLeitor(id)) {
      le.retomarLeitor(id);
      btnsPausaLeitor[id].setText("Pausar");

      log("Editor " + id + " retomado.");
    } 
    else {
      le.pausarLeitor(id);
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
  * Funcao: Alterna o estado de pausa de um escritor especifico via
  *         LeitoresEscritores. Se pausado, retoma; se rodando,
  *         pausa. Atualiza o texto do botao e registra no log.
  * Parametros: id - id do escritor a alternar
  * Retorno: void
  *************************************************************** */
  private void togglePausaEscritor(int id) {
    if (le.isPausadoEscritor(id)) {
      le.retomarEscritor(id);
      btnsPausaEscritor[id].setText("Pausar");
    
      log("Reporter " + id + " retomado.");
    } 
    else {
      le.pausarEscritor(id);
      btnsPausaEscritor[id].setText("Retomar");
    
      log("Reporter " + id + " pausado.");
    }
  } // Fim do metodo togglePausaEscritor

  /* ***************************************************************
  * Metodo: atualizarLeitorAguardando
  * Funcao: Atualiza a GUI quando o leitor de id informado entra no
  *         estado AGUARDANDO (esperando acesso a base de dados).
  * Parametros: id - id do leitor
  * Retorno: void
  *************************************************************** */
  private void atualizarLeitorAguardando(int id) {
    aplicarEstadoLeitorGUI(id, "AGUARDANDO");
  } // Fim do metodo atualizarLeitorAguardando

  /* ***************************************************************
  * Metodo: atualizarLeitorLendo
  * Funcao: Atualiza a GUI quando o leitor de id informado entra na
  *         regiao critica (lendo a base de dados). Atualiza tambem
  *         o conteudo exibido, a imagem do jornal e o contador de
  *         leitores ativos.
  * Parametros: id - id do leitor
  * Retorno: void
  *************************************************************** */
  private void atualizarLeitorLendo(int id) {
    aplicarEstadoLeitorGUI(id, "ATIVO");

    lblConteudoBase.setText(le.getConteudo());
    imgJornal.setImage(imgJornalAberto);
    lblLeitoresAtivos.setText("Editores lendo: " + le.getLeitoresAtivos());

    log("Editor " + id + " entrou na base (leitura).");
  } // Fim do metodo atualizarLeitorLendo

  /* ***************************************************************
  * Metodo: atualizarLeitorOcioso
  * Funcao: Atualiza a GUI quando o leitor de id informado volta ao
  *         estado OCIOSO (utilizando o dado lido). Fecha a imagem
  *         do jornal apenas quando nao houver mais leitores ativos.
  * Parametros: id - id do leitor
  * Retorno: void
  *************************************************************** */
  private void atualizarLeitorOcioso(int id) {
    aplicarEstadoLeitorGUI(id, "OCIOSO");

    if (le.getLeitoresAtivos() == 0) {
      imgJornal.setImage(imgJornalFechado);
    }

    lblLeitoresAtivos.setText("Editores lendo: " + le.getLeitoresAtivos());
  } // Fim do metodo atualizarLeitorOcioso

  /* ***************************************************************
  * Metodo: aplicarEstadoLeitorGUI
  * Funcao: Troca a imagem do personagem, o texto e estilo do label
  *         de estado e a borda do card do leitor de acordo com o
  *         nome do estado informado.
  * Parametros: id     - id do leitor
  *             estado - nome do estado a aplicar (ATIVO, AGUARDANDO,
  *                      PAUSADO ou OCIOSO)
  * Retorno: void
  *************************************************************** */
  private void aplicarEstadoLeitorGUI(int id, String estado) {
    switch (estado) {
      case "ATIVO":
        imgLeitores[id].setImage(imgsCacheLeitores[IDX_ATIVO]);
        setEstado(lblEstadoLeitores[id], "Lendo manchete", "estado-ativo");
        setCardStyle(imgLeitores[id], "card-ativo");
        break;
      case "AGUARDANDO":
        imgLeitores[id].setImage(imgsCacheLeitores[IDX_AGUARDANDO]);
        setEstado(lblEstadoLeitores[id], "Aguardando acesso", "estado-aguardando");
        setCardStyle(imgLeitores[id], "card-aguardando");
        break;
      case "PAUSADO":
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
  * Metodo: atualizarEscritorEscrevendo
  * Funcao: Atualiza a GUI quando o escritor de id informado entra
  *         na regiao critica (publicando a materia). Atualiza
  *         tambem o conteudo exibido, o numero da edicao e a
  *         imagem do jornal.
  * Parametros: id - id do escritor
  * Retorno: void
  *************************************************************** */
  private void atualizarEscritorEscrevendo(int id) {
    aplicarEstadoEscritorGUI(id, "ATIVO");

    lblConteudoBase.setText(le.getConteudo());
    lblEdicao.setText("Edicao n. " + le.getEdicao());
    imgJornal.setImage(imgJornalAberto);

    log("Reporter " + id + " publicou - Edicao " + le.getEdicao());
  } // Fim do metodo atualizarEscritorEscrevendo

  /* ***************************************************************
  * Metodo: atualizarEscritorOcioso
  * Funcao: Atualiza a GUI quando o escritor de id informado volta
  *         ao estado OCIOSO (apurando a proxima pauta). Fecha a
  *         imagem do jornal.
  * Parametros: id - id do escritor
  * Retorno: void
  *************************************************************** */
  private void atualizarEscritorOcioso(int id) {
    aplicarEstadoEscritorGUI(id, "OCIOSO");
    imgJornal.setImage(imgJornalFechado);
  } // Fim do metodo atualizarEscritorOcioso

  /* ***************************************************************
  * Metodo: aplicarEstadoEscritorGUI
  * Funcao: Troca a imagem do personagem, o texto e estilo do label
  *         de estado e a borda do card do escritor de acordo com
  *         o nome do estado informado.
  * Parametros: id     - id do escritor
  *             estado - nome do estado a aplicar (ATIVO, AGUARDANDO,
  *                      PAUSADO ou OCIOSO)
  * Retorno: void
  *************************************************************** */
  private void aplicarEstadoEscritorGUI(int id, String estado) {
    switch (estado) {
      case "ATIVO":
        imgEscritores[id].setImage(imgsCacheEscritores[IDX_ATIVO]);
        setEstado(lblEstadoEscritores[id], "Publicando materia", "estado-ativo");
        setCardStyle(imgEscritores[id], "card-ativo");
        break;
      case "AGUARDANDO":
        imgEscritores[id].setImage(imgsCacheEscritores[IDX_AGUARDANDO]);
        setEstado(lblEstadoEscritores[id], "Aguardando acesso", "estado-aguardando");
        setCardStyle(imgEscritores[id], "card-aguardando");
        break;
      case "PAUSADO":
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
