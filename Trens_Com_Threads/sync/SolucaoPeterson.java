package sync;
/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 15/04/2026
* Ultima alteracao.: 28/04/2026
* Nome.............: SolucaoPeterson.java
* Funcao...........: Implementa exclusao mutua pelo algoritmo de Peterson.
*                    Garante exclusao mutua sem inanicao para 2 processos
*                    usando want[] (interesse) e turn (vez).
*                    Possui dois conjuntos de variaveis para os dois
*                    trilhos simples da simulacao.
*
*                    NOTA DIDATICA: Este algoritmo eh a implementacao
*                    mais adequada para o problema em questão com 
*                    2 processos compartilhando o mesmo recurso 
*                    compartilhado sem conflito na regiao critica.
*                    Assim, Garante a exclusao mutua entre dois processos, 
*                    evitando condicoes de corrida (race conditions).
************************************************************************ */

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

  private boolean[] want  = new boolean[2]; // interesse no primeiro trilho simples
  private volatile int turn;                // turno do primeiro trilho simples
  private boolean[] want2 = new boolean[2]; // interesse no segundo trilho simples
  private volatile int turn2;               // turno do segundo trilho simples
  private volatile boolean shouldStop = false;

  /* ***************************************************************
  * Metodo: entrarRegiaoCritica
  * Funcao: Entrada de Peterson no primeiro trilho simples.
  *         O trem sinaliza interesse (want[id] = true), cede a
  *         vez ao outro (turn = outra) e aguarda enquanto o outro
  *         esta interessado E a vez e do outro.
  * Parametros: @param id          0 = trem azul, 1 = trem verde
  *             @param pathtrans   PathTransition do trem
  *             @param train       retangulo do trem
  *             @param rate        propriedade de taxa de velocidade
  * Retorno: void
  *************************************************************** */
  public void entrarRegiaoCritica(int id, PathTransition pathtrans,
    Rectangle train, DoubleProperty rate) {

    if (!shouldStop) {
      int outra = 1 - id;
      want[id] = true;
      turn = outra;

      // Pausa o trem uma unica vez antes de entrar no busy-wait.
      // Isso evita chamar Platform.runLater repetidamente dentro do loop,
      // o que saturaria a fila da FX thread e congelaria a interface.
      Platform.runLater(() -> {
        if (pathtrans.getStatus() != Animation.Status.PAUSED) {
          pathtrans.rateProperty().unbind();
          pathtrans.pause();
        }
      });

      // Busy-wait com sleep: aguarda o outro trem sair da regiao critica.
      // O Thread.sleep(10) cede a CPU entre as verificacoes, evitando que
      // esta thread consuma 100% de um nucleo enquanto espera.
      while (want[outra] && turn == outra) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      } // Fim do while Peterson

      // Retoma o trem apos obter acesso a regiao critica.
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
  * Parametros: @param id          0 = trem azul, 1=trem verde
  *             @param pathtrans   PathTransition do trem
  *             @param train       retangulo do trem
  *             @param rate        propriedade de taxa de velocidade
  * Retorno: void
  *************************************************************** */
  public void entrarRegiaoCritica2(int id, PathTransition pathtrans, Rectangle train, DoubleProperty rate) {

    if (!shouldStop) {
      int outra2 = 1 - id;
      want2[id] = true;
      turn2 = outra2;

      // Pausa o trem uma unica vez antes de entrar no busy-wait.
      Platform.runLater(() -> {
        if (pathtrans.getStatus() != Animation.Status.PAUSED) {
          pathtrans.rateProperty().unbind();
          pathtrans.pause();
        }
      });

      // Busy-wait com sleep para o segundo trilho simples.
      while (want2[outra2] && turn2 == outra2) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      } // Fim do while Peterson2

      // Retoma o trem apos obter acesso ao segundo trilho simples.
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
  * Parametros: @param id 0 = trem azul, 1=trem verde
  * Retorno: void
  *************************************************************** */
  public void sairRegiaoCritica(int id) {
    if (!shouldStop) want[id] = false;
  } // Fim do metodo sairRegiaoCritica

  /* ***************************************************************
  * Metodo: sairRegiaoCritica2
  * Funcao: Libera o interesse no segundo trilho simples.
  * Parametros: @param id 0 = trem azul, 1=trem verde
  * Retorno: void
  *************************************************************** */
  public void sairRegiaoCritica2(int id) {
    if (!shouldStop) want2[id] = false;
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
