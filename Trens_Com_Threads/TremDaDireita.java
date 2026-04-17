/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 15/04/2026
* Ultima alteracao.: 15/04/2026
* Nome.............: TremDaDireita.java
* Funcao...........: Thread do trem verde. Gerencia a animacao via
*                    PathTransition e invoca o algoritmo de exclusao
*                    mutua ativo, monitorando a posicao Y real do trem
*                    na cena para detectar entrada no trilho simples.
************************************************************************ */

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Slider;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/* ***************************************************************
* Classe: TremDaDireita
* Funcao: Thread do trem verde (id=1). Possui um PathTransition
*         proprio e referencia para o algoritmo de exclusao mutua
*         ativo (apenas um por vez pode estar definido).
*************************************************************** */
public class TremDaDireita extends Thread {

  private PathTransition pathTransition2;
  private Rectangle greenTrain;
  private Slider greenSpeedSlider;
  private VariavelDeTravamento exclusaobasica;
  private EstritaAlternancia alternancia;
  private DoubleProperty dividedRateProperty2;
  private boolean isPaused = false;
  private SolucaoPeterson peterson;

  /* ***************************************************************
  * Metodo: TremDaDireita (construtor)
  * Funcao: Inicializa o trem verde com o retangulo e slider recebidos
  *         e configura o PathTransition.
  * Parametros: @param greenTrain2       retangulo do trem verde
  *             @param greenSpeedSlider2 slider de velocidade
  * Retorno: nao possui
  *************************************************************** */
  public TremDaDireita(Rectangle greenTrain2, Slider greenSpeedSlider2) {
    this.greenTrain = greenTrain2;
    this.greenSpeedSlider = greenSpeedSlider2;
    setupPathTransition();
  } // fim do construtor

  /* ***************************************************************
  * Metodo: setupPathTransition
  * Funcao: Configura o PathTransition com binding de velocidade ao
  *         slider e listener para pausar automaticamente ao chegar
  *         em zero e retomar quando o valor sobe novamente.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void setupPathTransition() {
    pathTransition2 = new PathTransition();
    dividedRateProperty2 = new SimpleDoubleProperty();
    dividedRateProperty2.bind(Bindings.divide(
        this.greenSpeedSlider.valueProperty().add(0.1), 30.0));
    pathTransition2.rateProperty().bind(dividedRateProperty2);
    pathTransition2.setNode(greenTrain);
    pathTransition2.setDuration(Duration.seconds(4));
    pathTransition2.setCycleCount(1);
    pathTransition2.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
    pathTransition2.setInterpolator(Interpolator.LINEAR);
    pathTransition2.setOnFinished(event ->
        Platform.runLater(() -> pathTransition2.play()));

    dividedRateProperty2.addListener((observable, oldValue, newValue) -> {
      if (newValue.doubleValue() <= 0.1) {
        if (!isPaused && pathTransition2.getStatus() != Animation.Status.PAUSED) {
          pathTransition2.rateProperty().unbind();
          pathTransition2.pause();
          isPaused = true;
        }
      } else {
        if (isPaused && pathTransition2.getStatus() == Animation.Status.PAUSED) {
          pathTransition2.play();
          pathTransition2.rateProperty().bind(dividedRateProperty2);
          isPaused = false;
        }
      }
    });
  } // fim do metodo setupPathTransition

  /* ***************************************************************
  * Metodo: play
  * Funcao: Inicia ou retoma a animacao do trem via FX thread.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public void play() {
    Platform.runLater(() -> {
      if (pathTransition2 != null) pathTransition2.play();
    });
  } // fim do metodo play

  /* ***************************************************************
  * Metodo: stoptrain
  * Funcao: Para a animacao do trem via FX thread.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public void stoptrain() {
    Platform.runLater(() -> {
      if (pathTransition2 != null) pathTransition2.stop();
    });
  } // fim do metodo stoptrain

  /* ***************************************************************
  * Metodo: setPath
  * Funcao: Define o caminho de animacao do trem antes de iniciar.
  * Parametros: @param path caminho a seguir
  * Retorno: void
  *************************************************************** */
  public void setPath(Path path) {
    if (pathTransition2 != null) pathTransition2.setPath(path);
  } // fim do metodo setPath

  /* ***************************************************************
  * Metodo: run
  * Funcao: Loop principal da thread. Inicia a animacao e monitora
  *         continuamente a posicao Y para acionar o algoritmo de
  *         exclusao mutua ativo (apenas um por vez).
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  @Override
  public void run() {
    Platform.runLater(() -> {
      if (pathTransition2 != null) pathTransition2.play();
    });

    while (true) {
      // --- Solucao de Peterson ---
      if (peterson != null) {
        double y = greenTrain.localToScene(greenTrain.getBoundsInLocal()).getMinY();
        if (y >= 50 && y <= 350) {
          peterson.entrarRegiaoCritica(1, pathTransition2, greenTrain, dividedRateProperty2);
          peterson.sairRegiaoCritica(1);
        } else if (y >= 450 && y <= 750) {
          peterson.entrarRegiaoCritica2(1, pathTransition2, greenTrain, dividedRateProperty2);
          peterson.sairRegiaoCritica2(1);
        } else {
          try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
      } else {
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
      }

      // --- Variavel de Travamento ---
      if (exclusaobasica != null) {
        exclusaobasica.entrarRegiaoCritica(pathTransition2, greenTrain, dividedRateProperty2);
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
      }

      // --- Estrita Alternancia ---
      if (alternancia != null) {
        alternancia.entrarRegiaoCritica(1, pathTransition2, greenTrain, dividedRateProperty2);
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
      }
    } // fim do while
  } // fim do metodo run

  /* ***************************************************************
  * Metodo: setExclusaoMutua
  * Funcao: Define (ou remove com null) a variavel de travamento.
  * Parametros: @param v instancia de VariavelDeTravamento ou null
  * Retorno: void
  *************************************************************** */
  public void setExclusaoMutua(VariavelDeTravamento v) {
    this.exclusaobasica = v;
  } // fim do metodo setExclusaoMutua

  /* ***************************************************************
  * Metodo: setEstritaAlternancia
  * Funcao: Define (ou remove com null) a estrita alternancia.
  * Parametros: @param a instancia de EstritaAlternancia ou null
  * Retorno: void
  *************************************************************** */
  public void setEstritaAlternancia(EstritaAlternancia a) {
    this.alternancia = a;
  } // fim do metodo setEstritaAlternancia

  /* ***************************************************************
  * Metodo: setSolucaoPeterson
  * Funcao: Define (ou remove com null) a solucao de Peterson.
  * Parametros: @param p instancia de SolucaoPeterson ou null
  * Retorno: void
  *************************************************************** */
  public void setSolucaoPeterson(SolucaoPeterson p) {
    this.peterson = p;
  } // fim do metodo setSolucaoPeterson

} // fim da classe TremDaDireita