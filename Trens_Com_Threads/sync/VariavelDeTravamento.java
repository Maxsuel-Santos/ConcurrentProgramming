package sync;
/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 15/04/2026
* Ultima alteracao.: 26/04/2026
* Nome.............: VariavelDeTravamento.java
* Funcao...........: Implementa exclusao mutua pela variavel de travamento.
*                    Usa um inteiro (lock) para bloquear o acesso ao trilho
*                    simples: quando lock != 0 o recurso esta ocupado e o
*                    segundo trem aguarda (busy-wait) com a animacao pausada.
*
*                    NOTA DIDATICA: Este algoritmo propositalmente demonstra
*                    a limitacao da variavel de travamento simples. A sequencia
*                    "while(lock!=0) ... lock=1" nao e atomica: duas threads
*                    podem passar pelo while simultaneamente e ambas setarem
*                    lock=1, violando a exclusao mutua. Em hardware real isso
*                    seria corrigido com instrucoes atomicas (test-and-set).
*                    Aqui o comportamento e mantido intencional para fins
*                    pedagogicos da disciplina.
************************************************************************ */

import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.animation.PathTransition;
import javafx.scene.shape.Rectangle;

/* ***************************************************************
* Classe: VariavelDeTravamento
* Funcao: Exclusao mutua por variavel de travamento (lock inteiro).
*         Dois locks independentes: um para cada trilho simples.
*         NOTA: A verificacao + escrita do lock nao e atomica,
*         o que pode causar violacao de exclusao mutua em cargas
*         reais. Comportamento preservado para fins didaticos.
*************************************************************** */
public class VariavelDeTravamento {

  private volatile int lock  = 0; // lock do primeiro trilho simples
  private volatile int lock2 = 0; // lock do segundo trilho simples
  private volatile boolean shouldStop = false;

  /* ***************************************************************
  * Metodo: entrarRegiaoCritica
  * Funcao: Monitora a posicao Y do trem. Ao entrar na zona do
  *         trilho simples, aguarda o lock ficar livre, adquire-o,
  *         retoma a animacao, aguarda a travessia e libera o lock.
  * Parametros: @param pathtrans PathTransition do trem
  *             @param train     retangulo do trem
  *             @param rate      propriedade de taxa de velocidade
  * Retorno: void
  *************************************************************** */
  public void entrarRegiaoCritica(PathTransition pathtrans,
      Rectangle train, DoubleProperty rate) {

    while (!shouldStop) {
      double y = train.localToScene(train.getBoundsInLocal()).getMinY();

      if (y >= 50 && y <= 350) {
        while (lock != 0) {
          if (pathtrans.getStatus() != Animation.Status.PAUSED) {
            pathtrans.pause();
            pathtrans.rateProperty().unbind();
          }
        } // Fim do while lock
        lock = 1;
        Platform.runLater(() -> { pathtrans.play(); pathtrans.rateProperty().bind(rate); });
        criticalRegion(train);
        lock = 0;
        nonCriticalRegion();

      } else if (y >= 450 && y <= 750) {
        while (lock2 != 0) {
          if (pathtrans.getStatus() != Animation.Status.PAUSED) {
            pathtrans.pause();
            pathtrans.rateProperty().unbind();
          }
        } // Fim do while lock2
        lock2 = 1;
        Platform.runLater(() -> { pathtrans.play(); pathtrans.rateProperty().bind(rate); });
        criticalRegion2(train);
        lock2 = 0;
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
  * Funcao: Aguarda o trem sair do primeiro trilho simples
  *         (y >= 350 ou y <= 50) para liberar o lock.
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
  * Funcao: Aguarda o trem sair do segundo trilho simples
  *         (y >= 750 ou y <= 450) para liberar o lock.
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
  * Funcao: Sinaliza o encerramento do algoritmo para que o loop
  *         interno pare de executar.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public void encerrarExclusaoMutua() {
    shouldStop = true;
  } // Fim do metodo encerrarExclusaoMutua

} // Fim da classe VariavelDeTravamento
