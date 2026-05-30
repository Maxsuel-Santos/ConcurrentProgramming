/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 29/05/2026
* Ultima alteracao.: 29/05/2026
* Nome.............: JantarFilosofos.java
* Funcao...........: Nucleo do problema do Jantar dos Filosofos.
*                    Implementa EXATAMENTE o pseudocodigo do livro
*                    de Tanenbaum (Modern Operating Systems):
*
*                    semaphore mutex = 1;
*                    semaphore s[N];          // inicializados com 0
*                    int state[N];
*
*                    void take_forks(int i) {
*                      down(&mutex);
*                      state[i] = HUNGRY;
*                      test(i);
*                      up(&mutex);
*                      down(&s[i]);
*                    }
*
*                    void put_forks(int i) {
*                      down(&mutex);
*                      state[i] = THINKING;
*                      test(LEFT);
*                      test(RIGHT);
*                      up(&mutex);
*                    }
*
*                    void test(int i) {
*                      if (state[i] == HUNGRY &&
*                          state[LEFT] != EATING &&
*                          state[RIGHT] != EATING) {
*                            state[i] = EATING;
*                            up(&s[i]);
*                      }
*                    }
************************************************************************ */
package model;

import java.util.concurrent.Semaphore;
import util.Constantes;

/* ***************************************************************
* Classe: JantarFilosofos
* Funcao: Gerencia os semaforos e o vetor de estados dos filosofos.
*         Implementacao identica ao pseudocodigo do livro texto.
*************************************************************** */
public class JantarFilosofos {

  public final Semaphore mutex = new Semaphore(1);

  public final Semaphore[] s = new Semaphore[Constantes.N];

  private final EstadoFilosofo[] estado = new EstadoFilosofo[Constantes.N];

  private Runnable[] onEstadoMudou = new Runnable[Constantes.N];

  /* ***************************************************************
  * Metodo: JantarFilosofos (construtor)
  * Funcao: Inicializa todos os filosofos como PENSANDO e todos os
  *         semaforos s[i] com 0 (bloqueados), conforme o livro.
  * Parametros: nao possui
  * Retorno: nao possui
  *************************************************************** */
  public JantarFilosofos() {
    for (int i = 0; i < Constantes.N; i++) {
      estado[i] = EstadoFilosofo.PENSANDO;
      s[i] = new Semaphore(0); // inicia bloqueado
    }
  } // Fim do construtor

  /* ***************************************************************
  * Metodo: pegarGarfos
  * Funcao: Traducao direta de take_forks(int i) do livro.
  *         O filosofo sinaliza que esta FAMINTO, tenta pegar os
  *         garfos via test() e bloqueia em s[i] se nao conseguir.
  * Parametros: @param i indice do filosofo (0 a N-1)
  * Retorno: void
  *************************************************************** */
  public void pegarGarfos(int i) throws InterruptedException {
    mutex.acquire();                      // down(&mutex)
    estado[i] = EstadoFilosofo.FAMINTO;   // state[i] = HUNGRY;
    notificarEstado(i);
    testar(i);                            // test(i)
    mutex.release();                      // up(&mutex)
    s[i].acquire();                       // down(&s[i]) - bloqueia se necessario
  } // Fim do metodo pegarGarfos

  /* ***************************************************************
  * Metodo: devolverGarfos
  * Funcao: Traducao direta de put_forks(int i) do livro.
  *         O filosofo termina de comer, volta a PENSANDO e verifica
  *         se os vizinhos podem agora comer.
  * Parametros: @param i indice do filosofo (0 a N-1)
  * Retorno: void
  *************************************************************** */
  public void devolverGarfos(int i) throws InterruptedException {
    mutex.acquire();                          // down(&mutex)
    estado[i] = EstadoFilosofo.PENSANDO;      // state[i] = THINKING;
    notificarEstado(i);
    testar(Constantes.esquerda(i));           // test(LEFT)
    testar(Constantes.direita(i));            // test(RIGHT)
    mutex.release();                          // up(&mutex)
  } // Fim do metodo devolverGarfos

  /* ***************************************************************
  * Metodo: testar
  * Funcao: Traducao direta de test(int i) do livro.
  *         Verifica se o filosofo i pode comer: ele precisa estar
  *         FAMINTO e nenhum vizinho pode estar COMENDO.
  *         Se puder, muda para COMENDO e libera s[i].
  * Parametros: @param i indice do filosofo a testar
  * Retorno: void
  *************************************************************** */
  private void testar(int i) {
    if (estado[i] == EstadoFilosofo.FAMINTO
        && estado[Constantes.esquerda(i)] != EstadoFilosofo.COMENDO
        && estado[Constantes.direita(i)] != EstadoFilosofo.COMENDO) {
      estado[i] = EstadoFilosofo.COMENDO;
      notificarEstado(i);
      s[i].release();                         // up(&s[i])
    }
  } // Fim do metodo testar

  /* ***************************************************************
  * Metodo: getEstado
  * Funcao: Retorna o estado atual de um filosofo.
  * Parametros: @param i indice do filosofo
  * Retorno: @return EstadoFilosofo estado atual
  *************************************************************** */
  public EstadoFilosofo getEstado(int i) {
    return estado[i];
  } // Fim do metodo getEstado

  /* ***************************************************************
  * Metodo: setOnEstadoMudou
  * Funcao: Registra o callback da GUI que sera chamado sempre que
  *         o estado do filosofo i mudar.
  * Parametros: @param i indice do filosofo
  *             @param r Runnable a executar na FX thread
  * Retorno: void
  *************************************************************** */
  public void setOnEstadoMudou(int i, Runnable r) {
    onEstadoMudou[i] = r;
  } // Fim do metodo setOnEstadoMudou

  /* ***************************************************************
  * Metodo: reset
  * Funcao: Reinicia todos os estados e semaforos para o estado
  *         inicial. Chamado pelo botao RESET da GUI.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public void reset() {
    mutex.drainPermits();
    mutex.release(1);
    for (int i = 0; i < Constantes.N; i++) {
      estado[i] = EstadoFilosofo.PENSANDO;
      s[i].drainPermits();
      // s[i] permanece 0 apos o reset
      notificarEstado(i);
    }
  } // Fim do metodo reset

  /* ***************************************************************
  * Metodo: notificarEstado
  * Funcao: Dispara o callback da GUI para o filosofo i.
  *         Chamado sempre que estado[i] muda dentro de testar(),
  *         pegarGarfos() ou devolverGarfos().
  * Parametros: @param i indice do filosofo
  * Retorno: void
  *************************************************************** */
  private void notificarEstado(int i) {
    if (onEstadoMudou[i] != null) {
      javafx.application.Platform.runLater(onEstadoMudou[i]);
    }
  } // Fim do metodo notificarEstado

} // Fim da classe JantarFilosofos
