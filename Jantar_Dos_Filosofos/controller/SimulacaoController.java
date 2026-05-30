/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 29/05/2026
* Ultima alteracao.: 31/05/2026
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

  // Nome dos Filosofos
  private static final String[] NOMES = {
    "kant", "nietzsche", "platao", "aristoteles", "socrates"
  };

  // Imagens pre-carregadas + Estado do Filosofo (0 = PENSANDO, 1 = FAMINTO, 2 = COMENDO)
  private final Image[][] imagens = new Image[Constantes.N][3];

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

}
