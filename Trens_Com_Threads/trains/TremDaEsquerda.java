package trains;
/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 15/04/2026
* Ultima alteracao.: 27/04/2026
* Nome.............: TremDaEsquerda.java
* Funcao...........: Thread do trem azul. Gerencia a animacao via
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
import sync.EstritaAlternancia;
import sync.SolucaoPeterson;
import sync.VariavelDeTravamento;

/* ***************************************************************
* Classe: TremDaEsquerda
* Funcao: Thread do trem azul (id = 0). Possui um PathTransition
*         proprio e referencia para o algoritmo de exclusao mutua
*         ativo (apenas um por vez pode estar definido).
*************************************************************** */
public class TremDaEsquerda extends Thread {

  private PathTransition pathTransition1;
  private Rectangle blueTrain;
  private Slider blueSpeedSlider;
  private VariavelDeTravamento exclusaomutua;
  private EstritaAlternancia alternancia;
  private DoubleProperty dividedRateProperty;
  private boolean isPaused = false;
  private SolucaoPeterson peterson;

  /* ***************************************************************
  * Metodo: TremDaEsquerda (construtor)
  * Funcao: Inicializa o trem azul com o retangulo e slider recebidos
  *         e configura o PathTransition.
  * Parametros: @param blueTrain2        retangulo do trem azul
  *             @param blueSpeedSlider2  slider de velocidade
  * Retorno: nao possui
  *************************************************************** */
  public TremDaEsquerda(Rectangle blueTrain2, Slider blueSpeedSlider2) {
    this.blueTrain = blueTrain2;
    this.blueSpeedSlider = blueSpeedSlider2;
    setupPathTransition();
  } // Fim do construtor

  /* ***************************************************************
  * Metodo: setupPathTransition
  * Funcao: Configura o PathTransition com binding de velocidade ao
  *         slider e listener para pausar automaticamente ao chegar
  *         em zero e retomar quando o valor sobe novamente.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void setupPathTransition() {
    pathTransition1 = new PathTransition();
    dividedRateProperty = new SimpleDoubleProperty();
    dividedRateProperty.bind(Bindings.divide(
        this.blueSpeedSlider.valueProperty().add(0.1), 30.0));
    pathTransition1.rateProperty().bind(dividedRateProperty);
    pathTransition1.setNode(blueTrain);
    pathTransition1.setDuration(Duration.seconds(4));
    pathTransition1.setCycleCount(1);
    pathTransition1.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
    pathTransition1.setInterpolator(Interpolator.LINEAR);
    pathTransition1.setOnFinished(event ->
        Platform.runLater(() -> pathTransition1.play()));

    dividedRateProperty.addListener((observable, oldValue, newValue) -> {
      if (newValue.doubleValue() <= 0.1) {
        if (!isPaused && pathTransition1.getStatus() != Animation.Status.PAUSED) {
          pathTransition1.rateProperty().unbind();
          pathTransition1.pause();
          isPaused = true;
        }
      } else {
        if (isPaused && pathTransition1.getStatus() == Animation.Status.PAUSED) {
          pathTransition1.play();
          pathTransition1.rateProperty().bind(dividedRateProperty);
          isPaused = false;
        }
      }
    });
  } // Fim do metodo setupPathTransition

  /* ***************************************************************
  * Metodo: play
  * Funcao: Inicia ou retoma a animacao do trem via FX thread.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public void play() {
    Platform.runLater(() -> {
      if (pathTransition1 != null) 
        pathTransition1.play();
    });
  } // Fim do metodo play

  /* ***************************************************************
  * Metodo: stoptrain
  * Funcao: Para a animacao do trem via FX thread.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public void stoptrain() {
    Platform.runLater(() -> {
      if (pathTransition1 != null) 
        pathTransition1.stop();
    });
  } // Fim do metodo stoptrain

  /* ***************************************************************
  * Metodo: setPath
  * Funcao: Define o caminho de animacao do trem antes de iniciar.
  * Parametros: @param path caminho a seguir
  * Retorno: void
  *************************************************************** */
  public void setPath(Path path) {
    if (pathTransition1 != null) 
      pathTransition1.setPath(path);
  } // Fim do metodo setPath

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
      if (pathTransition1 != null) pathTransition1.play();
    });

    while (true) {
      // Copia local: evita NullPointerException se o Principal trocar o
      // algoritmo entre o "if != null" e a chamada do metodo seguinte.
      SolucaoPeterson p       = peterson;
      VariavelDeTravamento vt = exclusaomutua;
      EstritaAlternancia ea   = alternancia;

      // Apenas UM algoritmo pode estar ativo por vez: if/else if/else garante
      // que somente o bloco do algoritmo ativo seja executado por iteracao.
      if (p != null) {
        double y = blueTrain.localToScene(blueTrain.getBoundsInLocal()).getMinY();
        if (y >= 50 && y <= 350) {
          p.entrarRegiaoCritica(0, pathTransition1, blueTrain, dividedRateProperty);
          p.sairRegiaoCritica(0);
          // Aguarda o trem sair completamente da zona antes da proxima verificacao,
          // evitando re-entrar na regiao critica imediatamente apos a travessia.
          try { 
            Thread.sleep(200); 
          } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
            break; 
          }
        } else if (y >= 450 && y <= 750) {
          p.entrarRegiaoCritica2(0, pathTransition1, blueTrain, dividedRateProperty);
          p.sairRegiaoCritica2(0);
          try { 
            Thread.sleep(200); 
          } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
            break; 
          }
        } else {
          try { 
            Thread.sleep(50); 
          } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
            break; 
          }
        }
      } else if (vt != null) {
        vt.entrarRegiaoCritica(pathTransition1, blueTrain, dividedRateProperty);
        try { 
          Thread.sleep(200); 
        } catch (InterruptedException e) { 
          Thread.currentThread().interrupt(); 
          break; 
        }
      } else if (ea != null) {
        ea.entrarRegiaoCritica(0, pathTransition1, blueTrain, dividedRateProperty);
        try { 
          Thread.sleep(200); 
        } catch (InterruptedException e) { 
          Thread.currentThread().interrupt(); 
          break; 
        }
      } else {
        // Nenhum algoritmo ativo: dorme ate ser necessario verificar novamente.
        try { 
          Thread.sleep(100); 
        } catch (InterruptedException e) { 
          Thread.currentThread().interrupt(); 
          break; 
        }
      }
    } // Fim do while
  } // Fim do metodo run

  /* ***************************************************************
  * Metodo: setExclusaoMutua
  * Funcao: Define (ou remove com null) a variavel de travamento.
  * Parametros: @param v instancia de VariavelDeTravamento ou null
  * Retorno: void
  *************************************************************** */
  public void setExclusaoMutua(VariavelDeTravamento v) {
    this.exclusaomutua = v;
  } // Fim do metodo setExclusaoMutua

  /* ***************************************************************
  * Metodo: setEstritaAlternancia
  * Funcao: Define (ou remove com null) a estrita alternancia.
  * Parametros: @param a instancia de EstritaAlternancia ou null
  * Retorno: void
  *************************************************************** */
  public void setEstritaAlternancia(EstritaAlternancia a) {
    this.alternancia = a;
  } // Fim do metodo setEstritaAlternancia

  /* ***************************************************************
  * Metodo: setSolucaoPeterson
  * Funcao: Define (ou remove com null) a solucao de Peterson.
  * Parametros: @param p instancia de SolucaoPeterson ou null
  * Retorno: void
  *************************************************************** */
  public void setSolucaoPeterson(SolucaoPeterson p) {
    this.peterson = p;
  } // Fim do metodo setSolucaoPeterson

} // Fim da classe TremDaEsquerda
