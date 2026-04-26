package sync;
/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 15/04/2026
* Ultima alteracao.: 26/04/2026
* Nome.............: SolucaoPeterson.java
* Funcao...........: Implementa exclusao mutua pelo algoritmo de Peterson.
*                    Garante exclusao mutua sem inanicao para 2 processos
*                    usando want[] (interesse) e turn (vez).
*                    Possui dois conjuntos de variaveis para os dois
*                    trilhos simples da simulacao.
************************************************************************ */

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.animation.PathTransition;
import javafx.scene.shape.Rectangle;

/* ***************************************************************
* Classe: SolucaoPeterson
* Funcao: Exclusao mutua pelo algoritmo de Peterson (2 processos).
*         want[]  / turn  gerenciam o primeiro trilho simples.
*         want2[] / turn2 gerenciam o segundo trilho simples.
*************************************************************** */
public class SolucaoPeterson {

  // (volatile em array protege apenas a referencia, nao os elementos)
  private final AtomicBoolean[] want  = { new AtomicBoolean(false), new AtomicBoolean(false) };
  private volatile int turn;

  private final AtomicBoolean[] want2 = { new AtomicBoolean(false), new AtomicBoolean(false) };
  private volatile int turn2;

  private volatile boolean shouldStop = false;

  /* ***************************************************************
  * Metodo: entrarRegiaoCritica
  * Funcao: Entrada de Peterson no primeiro trilho simples.
  *         O trem sinaliza interesse (want[id] = true), cede a
  *         vez ao outro (turn = outra) e aguarda enquanto o outro
  *         esta interessado E a vez e do outro.
  * Parametros: @param id          0=trem azul, 1=trem verde
  *             @param pathtrans   PathTransition do trem
  *             @param train       retangulo do trem
  *             @param rate        propriedade de taxa de velocidade
  * Retorno: void
  *************************************************************** */
  public void entrarRegiaoCritica(int id, PathTransition pathtrans,
      Rectangle train, DoubleProperty rate) {

    if (!shouldStop) {
      int outra = 1 - id;
      want[id].set(true);
      turn = outra;

      while (want[outra].get() && turn == outra) {
        if (pathtrans.getStatus() != Animation.Status.PAUSED) {
          Platform.runLater(() -> {
            pathtrans.pause(); pathtrans.rateProperty().unbind();
          });
        }
      } // Fim do while Peterson

      Platform.runLater(() -> { pathtrans.play(); pathtrans.rateProperty().bind(rate); });

      while (true) {
        double y = train.localToScene(train.getBoundsInLocal()).getMinY();
        if (y >= 350 || y <= 50)
          break;
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    } // Fim do if shouldStop
  } // Fim do metodo entrarRegiaoCritica

  /* ***************************************************************
  * Metodo: entrarRegiaoCritica2
  * Funcao: Entrada de Peterson no segundo trilho simples.
  *         Logica identica ao primeiro, porem usando want2 e turn2.
  * Parametros: @param id          0=trem azul, 1=trem verde
  *             @param pathtrans   PathTransition do trem
  *             @param train       retangulo do trem
  *             @param rate        propriedade de taxa de velocidade
  * Retorno: void
  *************************************************************** */
  public void entrarRegiaoCritica2(int id, PathTransition pathtrans,
      Rectangle train, DoubleProperty rate) {

    if (!shouldStop) {
      int outra2 = 1 - id;
      want2[id].set(true);
      turn2 = outra2;

      while (want2[outra2].get() && turn2 == outra2) {
        if (pathtrans.getStatus() != Animation.Status.PAUSED) {
          Platform.runLater(() -> {
            pathtrans.pause(); pathtrans.rateProperty().unbind();
          });
        }
      } // Fim do while Peterson2

      Platform.runLater(() -> {
        pathtrans.play(); pathtrans.rateProperty().bind(rate);
      });

      while (true) {
        double y = train.localToScene(train.getBoundsInLocal()).getMinY();
        if (y >= 750 || y <= 450)
          break;
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    } // Fim do if shouldStop
  } // Fim do metodo entrarRegiaoCritica2

  /* ***************************************************************
  * Metodo: sairRegiaoCritica
  * Funcao: Libera o interesse no primeiro trilho simples.
  * Parametros: @param id 0=trem azul, 1=trem verde
  * Retorno: void
  *************************************************************** */
  public void sairRegiaoCritica(int id) {
    if (!shouldStop) want[id].set(false);
  } // Fim do metodo sairRegiaoCritica

  /* ***************************************************************
  * Metodo: sairRegiaoCritica2
  * Funcao: Libera o interesse no segundo trilho simples.
  * Parametros: @param id 0=trem azul, 1=trem verde
  * Retorno: void
  *************************************************************** */
  public void sairRegiaoCritica2(int id) {
    if (!shouldStop) want2[id].set(false);
  } // Fim do metodo sairRegiaoCritica2

  /* ***************************************************************
  * Metodo: encerrarExclusaoMutua
  * Funcao: Sinaliza o encerramento do algoritmo.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public void encerrarExclusaoMutua() {
    shouldStop = true;
  } // Fim do metodo encerrarExclusaoMutua

} // Fim da classe SolucaoPeterson
