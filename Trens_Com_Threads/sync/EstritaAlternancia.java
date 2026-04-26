package sync;
/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 15/04/2026
* Ultima alteracao.: 26/04/2026
* Nome.............: EstritaAlternancia.java
* Funcao...........: Implementa exclusao mutua por estrita alternancia.
*                    Os trens se revezam na entrada do trilho simples:
*                    um passa, depois o outro, alternando o turno apos
*                    cada travessia completa.
************************************************************************ */

import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.animation.PathTransition;
import javafx.scene.shape.Rectangle;

/* ***************************************************************
* Classe: EstritaAlternancia
* Funcao: Exclusao mutua por estrita alternancia.
*         turno  controla o primeiro trilho simples (inicia id=0).
*         turno2 controla o segundo trilho simples  (inicia id=0).
*         Ambos iniciam com o trem azul para comportamento simetrico.
*************************************************************** */
public class EstritaAlternancia {

  private volatile int turno  = 0; // primeiro trilho: vez do trem azul (id=0)
  private volatile int turno2 = 0; // segundo trilho:  vez do trem azul (id=0) -- CORRECAO: era 1
  private volatile boolean shouldStop = false;

  /* ***************************************************************
  * Metodo: entrarRegiaoCritica
  * Funcao: Quando o trem entra na zona do trilho simples aguarda
  *         ser a sua vez (turno == id). Apos a travessia completa
  *         passa a vez para o outro trem.
  * Parametros: @param id          0=trem azul, 1=trem verde
  *             @param pathtrans   PathTransition do trem
  *             @param train       retangulo do trem
  *             @param rate        propriedade de taxa de velocidade
  * Retorno: void
  *************************************************************** */
  public void entrarRegiaoCritica(int id, PathTransition pathtrans,
      Rectangle train, DoubleProperty rate) {

    while (!shouldStop) {
      double y = train.localToScene(train.getBoundsInLocal()).getMinY();

      if (y >= 50 && y <= 350) {
        while (turno != id) {
          if (pathtrans.getStatus() != Animation.Status.PAUSED) {
            pathtrans.pause();
            pathtrans.rateProperty().unbind();
          }
        } // Fim do while turno
        Platform.runLater(() -> { pathtrans.play(); pathtrans.rateProperty().bind(rate); });
        criticalRegion(train);
        turno = (id + 1) % 2;
        nonCriticalRegion();

      } else if (y >= 450 && y <= 750) {
        while (turno2 != id) {
          if (pathtrans.getStatus() != Animation.Status.PAUSED) {
            pathtrans.pause();
            pathtrans.rateProperty().unbind();
          }
        } // Fim do while turno2
        Platform.runLater(() -> { pathtrans.play(); pathtrans.rateProperty().bind(rate); });
        criticalRegion2(train);
        turno2 = 1 - id;
        nonCriticalRegion2();

      } else {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    } // Fim do while shouldStop
  } // Fim do metodo entrarRegiaoCritica

  /* ***************************************************************
  * Metodo: criticalRegion
  * Funcao: Aguarda o trem sair do primeiro trilho simples.
  * Parametros: @param train retangulo do trem
  * Retorno: void
  *************************************************************** */
  private void criticalRegion(Rectangle train) {
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
  } // Fim do metodo criticalRegion

  /* ***************************************************************
  * Metodo: nonCriticalRegion
  * Funcao: Representa a regiao nao critica apos o primeiro trilho.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void nonCriticalRegion() { } // regiao nao critica

  /* ***************************************************************
  * Metodo: criticalRegion2
  * Funcao: Aguarda o trem sair do segundo trilho simples.
  * Parametros: @param train retangulo do trem
  * Retorno: void
  *************************************************************** */
  private void criticalRegion2(Rectangle train) {
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
  } // Fim do metodo criticalRegion2

  /* ***************************************************************
  * Metodo: nonCriticalRegion2
  * Funcao: Representa a regiao nao critica apos o segundo trilho.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void nonCriticalRegion2() { } // regiao nao critica

  /* ***************************************************************
  * Metodo: encerrarExclusaoMutua
  * Funcao: Sinaliza o encerramento do algoritmo.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public void encerrarExclusaoMutua() {
    shouldStop = true;
  } // Fim do metodo encerrarExclusaoMutua

} // Fim da classe EstritaAlternancia
