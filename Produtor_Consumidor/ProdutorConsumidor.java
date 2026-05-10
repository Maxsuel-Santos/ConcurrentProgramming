/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 07/05/2026
* Ultima alteracao.: 08/05/2026
* Nome.............: ProdutorConsumidor.java
* Funcao...........: Nucleo do problema do Produtor/Consumidor.
*                    Contem o buffer circular e os tres semaforos
*                    exatamente como definidos no codigo do livro texto
*                    (Tanenbaum). Os metodos down() e up() traduzem
*                    diretamente as operacoes P e V do pseudocodigo C.
************************************************************************ */

import java.util.concurrent.Semaphore;
import util.Constants;

/* ***************************************************************
* Classe: ProdutorConsumidor
* Funcao: Gerencia o buffer compartilhado e os semaforos.
*         Estrutura identica ao pseudocodigo do livro texto:
*
*         typedef int semaphore;
*         semaphore mutex = 1;
*         semaphore empty = N;
*         semaphore full  = 0;
*************************************************************** */
public class ProdutorConsumidor {

  public final Semaphore mutex = new Semaphore(1);
  public final Semaphore empty = new Semaphore(Constants.N);
  public final Semaphore full  = new Semaphore(0);

  private final int[] buffer = new int[Constants.N];
  private int in  = 0;
  private int out = 0;
  private int count = 0;

  /* ***************************************************************
  * Metodo: enterItem
  * Funcao: Insere um item no buffer circular.
  *         Equivalente a enter_item(item) do pseudocodigo C.
  *         Deve ser chamado DENTRO da secao critica (apos down(mutex)).
  * Parametros: @param item valor inteiro a inserir
  * Retorno: void
  *************************************************************** */
  public void enterItem(int item) {
    buffer[in] = item;
    in = (in + 1) % Constants.N;
    count++;
  } // Fim do metodo enterItem

  /* ***************************************************************
  * Metodo: removeItem
  * Funcao: Remove e retorna um item do buffer circular.
  *         Equivalente a remove_item(item) do pseudocodigo C.
  *         Deve ser chamado DENTRO da secao critica (apos down(mutex)).
  * Parametros: nao possui
  * Retorno: @return int item removido do buffer
  *************************************************************** */
  public int removeItem() {
    int item = buffer[out];
    buffer[out] = 0;
    out = (out + 1) % Constants.N;
    count--;
    return item;
  } // Fim do metodo removeItem

  /* ***************************************************************
  * Metodo: getCount
  * Funcao: Retorna a quantidade atual de itens no buffer.
  *         Usado pela GUI para atualizar a barra de progresso
  *         e os slots visuais da mesa.
  * Parametros: nao possui
  * Retorno: @return int quantidade de itens no buffer
  *************************************************************** */
  public int getCount() {
    return count;
  } // Fim do metodo getCount

  /* ***************************************************************
  * Metodo: getBuffer
  * Funcao: Retorna copia do estado atual do buffer para a GUI.
  * Parametros: nao possui
  * Retorno: @return int[] copia do buffer
  *************************************************************** */
  public int[] getBuffer() {
    return buffer.clone();
  } // Fim do metodo getBuffer

  /* ***************************************************************
  * Metodo: down
  * Funcao: Operacao P do semaforo — decrementa ou bloqueia a thread
  *         ate que outro processo execute up().
  *         Traducao direta de down(&sem) do pseudocodigo C.
  * Parametros: @param sem semaforo a decrementar
  * Retorno: void
  *************************************************************** */
  public static void down(Semaphore sem) {
    try {
      sem.acquire();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  } // Fim do metodo down

  /* ***************************************************************
  * Metodo: up
  * Funcao: Operacao V do semaforo — incrementa e acorda uma thread
  *         bloqueada no down() correspondente.
  *         Traducao direta de up(&sem) do pseudocodigo C.
  * Parametros: @param sem semaforo a incrementar
  * Retorno: void
  *************************************************************** */
  public static void up(Semaphore sem) {
    sem.release();
  } // Fim do metodo up

  /* ***************************************************************
  * Metodo: reset
  * Funcao: Reinicia o buffer e os semaforos para o estado inicial.
  *         Chamado pelo botao RESET da GUI.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public void reset() {
    // Zera o buffer
    for (int i = 0; i < Constants.N; i++) {
      buffer[i] = 0;
    }

    in = 0;
    out = 0;
    count = 0;

    // Drena todos os semaforos para zero antes de reconfigurar
    mutex.drainPermits();
    empty.drainPermits();
    full.drainPermits();

    // Restaura valores iniciais
    mutex.release(1);
    empty.release(Constants.N);
    // full permanece 0
  } // Fim do metodo reset

} // Fim da classe ProdutorConsumidor
