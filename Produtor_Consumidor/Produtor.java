/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 07/05/2026
* Ultima alteracao.: 10/05/2026
* Nome.............: Produtor.java
* Funcao...........: Thread do churrasqueiro (produtor).
*                    Implementacao EXATA do pseudocodigo do livro do Tanembaun:
*
*                    void producer(void) {
*                      int item;
*                      while (TRUE) {
*                        produce_item(&item);
*                        down(&empty);
*                        down(&mutex);
*                        enter_item(item);
*                        up(&mutex);
*                        up(&full);
*                      }
*                    }
*
*                    OBS: A implemantacao sofreu adaptacoes para o contexto
*                    do algoritmo em questao.
************************************************************************ */

import javafx.application.Platform;
import javafx.scene.image.ImageView;

/* ***************************************************************
* Classe: Produtor
* Funcao: Thread do churrasqueiro. Gera espetos e os coloca na
*         mesa (buffer). Estrutura identica ao producer() do
*         pseudocodigo C do livro texto de Tanenbaum.
*************************************************************** */
public class Produtor extends Thread {

  private final ProdutorConsumidor pc;
  private volatile int speedMs;
  private volatile boolean pausado = false;
  private Runnable onProduziu;
  private Runnable onEsperando;
  private ImageView imgView;

  /* ***************************************************************
  * Metodo: Produtor (construtor)
  * Funcao: Inicializa o produtor com o contexto compartilhado.
  * Parametros: @param pc     contexto compartilhado (buffer + semaforos)
  *             @param speedMs velocidade inicial de producao
  * Retorno: nao possui
  *************************************************************** */
  public Produtor(ProdutorConsumidor pc, int speedMs) {
    this.pc = pc;
    this.speedMs = speedMs;
    setDaemon(true);
  } // Fim do construtor

  /* ***************************************************************
  * Metodo: run
  * Funcao: Laco principal do produtor — traducao direta do
  *         pseudocodigo C do livro texto.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  @Override
  public void run() {
    int item;

    while (!isInterrupted()) {

      // Respeita a pausa antes de comecar um novo ciclo
      esperarSeEmPausa();
      if (isInterrupted()) break;

      // produce_item(&item) — simula o tempo de grelhar
      item = produceItem();
      if (isInterrupted()) break;

      // Verifica ANTES do down se vai bloquear ou nao.
      // Se empty == 0 o buffer esta cheio: notifica "esperando".
      // Se empty > 0 ha espaco livre: vai produzir sem bloquear.
      if (pc.empty.availablePermits() == 0) {
        notificarEsperando();
      }

      // down(&empty) — aguarda espaco no buffer
      ProdutorConsumidor.down(pc.empty);
      if (isInterrupted()) break;

      // down(&mutex) — entra na secao critica
      ProdutorConsumidor.down(pc.mutex);

      // enter_item(item) — coloca espeto na mesa
      pc.enterItem(item);

      // up(&mutex) — sai da secao critica
      ProdutorConsumidor.up(pc.mutex);

      // up(&full) — avisa que ha mais um espeto disponivel
      ProdutorConsumidor.up(pc.full);

      // Notifica a GUI para atualizar os slots da mesa
      notificarProduziu();
    }
  } // Fim do metodo run

  /* ***************************************************************
  * Metodo: produceItem
  * Funcao: Simula o tempo de grelhar um espeto (produce_item).
  *         Dorme pelo tempo configurado no slider de velocidade.
  * Parametros: nao possui
  * Retorno: @return int numero do espeto produzido
  *************************************************************** */
  private int produceItem() {
    try {
      Thread.sleep(speedMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return (int)(Math.random() * 100) + 1;
  } // Fim do metodo produceItem

  /* ***************************************************************
  * Metodo: esperarSeEmPausa
  * Funcao: Bloqueia a thread enquanto o produtor estiver pausado.
  *         Desbloqueia quando retomar() for chamado.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private synchronized void esperarSeEmPausa() {
    while (pausado && !isInterrupted()) {
      try {
        wait();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  } // Fim do metodo esperarSeEmPausa

  /* ***************************************************************
  * Metodo: pausar
  * Funcao: Pausa o produtor no proximo ciclo do laco principal.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public synchronized void pausar() {
    pausado = true;
  } // Fim do metodo pausar

  /* ***************************************************************
  * Metodo: retomar
  * Funcao: Retoma o produtor acordando o wait() de esperarSeEmPausa.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public synchronized void retomar() {
    pausado = false;
    notifyAll();
  } // Fim do metodo retomar

  /* ***************************************************************
  * Metodo: isPausado
  * Funcao: Retorna se o produtor esta pausado.
  * Parametros: nao possui
  * Retorno: @return boolean true se pausado
  *************************************************************** */
  public boolean isPausado() {
    return pausado;
  } // Fim do metodo isPausado

  /* ***************************************************************
  * Metodo: setSpeedMs
  * Funcao: Atualiza a velocidade de producao em tempo real.
  * Parametros: @param ms novo valor em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setSpeedMs(int ms) {
    this.speedMs = ms;
  } // Fim do metodo setSpeedMs

  /* ***************************************************************
  * Metodo: setOnProduziu
  * Funcao: Define o callback chamado apos cada item produzido.
  * Parametros: @param r Runnable a executar na FX thread
  * Retorno: void
  *************************************************************** */
  public void setOnProduziu(Runnable r) {
    this.onProduziu = r;
  } // Fim do metodo setOnProduziu

  /* ***************************************************************
  * Metodo: setOnEsperando
  * Funcao: Define o callback chamado quando o produtor bloqueia.
  * Parametros: @param r Runnable a executar na FX thread
  * Retorno: void
  *************************************************************** */
  public void setOnEsperando(Runnable r) {
    this.onEsperando = r;
  } // Fim do metodo setOnEsperando

  private void notificarProduziu() {
    if (onProduziu != null) {
      Platform.runLater(onProduziu);
    }
  }

  private void notificarEsperando() {
    if (onEsperando != null) {
      Platform.runLater(onEsperando);
    }
  }

} // Fim da classe Produtor
