/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 07/05/2026
* Ultima alteracao.: 10/05/2026
* Nome.............: Consumidor.java
* Funcao...........: Thread do comedor (consumidor).
*                    Implementacao EXATA do pseudocodigo do livro do Tanembaun:
*
*                    void consumer(void) {
*                      int item;
*                      while (TRUE) {
*                        down(&full);
*                        down(&mutex);
*                        remove_item(item);
*                        up(&mutex);
*                        up(&empty);
*                        consume_item(item);
*                      }
*                    }
*
*                    OBS: A implemantacao sofreu adaptacoes para o contexto
*                    do algoritmo em questao.
************************************************************************ */

import javafx.application.Platform;

/* ***************************************************************
* Classe: Consumidor
* Funcao: Thread do comedor. Retira espetos da mesa (buffer) e os
*         consome. Estrutura identica ao consumer() do pseudocodigo
*         C do livro texto de Tanenbaum.
*************************************************************** */
public class Consumidor extends Thread {

  private final ProdutorConsumidor pc;
  private volatile int speedMs;
  private volatile boolean pausado = false;
  private Runnable onConsumiu;
  private Runnable onEsperando;

  /* ***************************************************************
  * Metodo: Consumidor (construtor)
  * Funcao: Inicializa o consumidor com o contexto compartilhado.
  * Parametros: @param pc      contexto compartilhado (buffer + semaforos)
  *             @param speedMs velocidade inicial de consumo
  * Retorno: nao possui
  *************************************************************** */
  public Consumidor(ProdutorConsumidor pc, int speedMs) {
    this.pc = pc;
    this.speedMs = speedMs;
    setDaemon(true);
  } // Fim do construtor

  /* ***************************************************************
  * Metodo: run
  * Funcao: Laco principal do consumidor — traducao direta do
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

      // Verifica ANTES do down se vai bloquear ou nao.
      // Se full == 0 o buffer esta vazio: notifica "esperando" e muda imagem.
      // Se full > 0 ha espeto disponivel: notifica "consumindo" antes de pegar.
      if (pc.full.availablePermits() == 0) {
        notificarEsperando();
      } else {
        notificarConsumindo();
      }

      // down(&full) — aguarda item disponivel no buffer
      ProdutorConsumidor.down(pc.full);
      if (isInterrupted()) break;

      // Ha espeto disponivel: garante que a imagem ativa seja exibida
      notificarConsumindo();

      // down(&mutex) — entra na secao critica
      ProdutorConsumidor.down(pc.mutex);

      // remove_item(item) — pega espeto da mesa
      item = pc.removeItem();

      // up(&mutex) — sai da secao critica
      ProdutorConsumidor.up(pc.mutex);

      // up(&empty) — avisa que ha mais um espaco livre
      ProdutorConsumidor.up(pc.empty);

      // consume_item(item) — come o espeto (fora da secao critica)
      consumeItem(item);

      // Notifica a GUI para atualizar os slots da mesa
      notificarConsumiu();
    }
  } // Fim do metodo run

  /* ***************************************************************
  * Metodo: consumeItem
  * Funcao: Simula o tempo de comer um espeto (consume_item).
  *         Dorme pelo tempo configurado no slider de velocidade.
  * Parametros: @param item espeto a ser consumido
  * Retorno: void
  *************************************************************** */
  private void consumeItem(int item) {
    try {
      Thread.sleep(speedMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  } // Fim do metodo consumeItem

  /* ***************************************************************
  * Metodo: esperarSeEmPausa
  * Funcao: Bloqueia a thread enquanto o consumidor estiver pausado.
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
  * Funcao: Pausa o consumidor no proximo ciclo do laco principal.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public synchronized void pausar() {
    pausado = true;
  } // Fim do metodo pausar

  /* ***************************************************************
  * Metodo: retomar
  * Funcao: Retoma o consumidor acordando o wait().
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public synchronized void retomar() {
    pausado = false;
    notifyAll();
  } // Fim do metodo retomar

  /* ***************************************************************
  * Metodo: isPausado
  * Funcao: Retorna se o consumidor esta pausado.
  * Parametros: nao possui
  * Retorno: @return boolean true se pausado
  *************************************************************** */
  public boolean isPausado() {
    return pausado;
  } // Fim do metodo isPausado

  /* ***************************************************************
  * Metodo: setSpeedMs
  * Funcao: Atualiza a velocidade de consumo em tempo real.
  * Parametros: @param ms novo valor em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setSpeedMs(int ms) {
    this.speedMs = ms;
  } // Fim do metodo setSpeedMs

  /* ***************************************************************
  * Metodo: setOnConsumiu
  * Funcao: Define o callback chamado apos cada item consumido.
  * Parametros: @param r Runnable a executar na FX thread
  * Retorno: void
  *************************************************************** */
  public void setOnConsumiu(Runnable r) {
    this.onConsumiu = r;
  } // Fim do metodo setOnConsumiu

  /* ***************************************************************
  * Metodo: setOnEsperando
  * Funcao: Define o callback chamado quando o consumidor bloqueia.
  * Parametros: @param r Runnable a executar na FX thread
  * Retorno: void
  *************************************************************** */
  public void setOnEsperando(Runnable r) {
    this.onEsperando = r;
  } // Fim do metodo setOnEsperando

  private void notificarConsumiu() {
    if (onConsumiu != null) {
      Platform.runLater(onConsumiu);
    }
  }

  private void notificarEsperando() {
    if (onEsperando != null) {
      Platform.runLater(onEsperando);
    }
  }

  // Notifica que o consumidor esta ativamente consumindo (buffer nao vazio).
  // Usado para garantir que a imagem ativa apareca antes do sleep do consumeItem.
  private void notificarConsumindo() {
    if (onConsumiu != null) {
      Platform.runLater(onConsumiu);
    }
  }

} // Fim da classe Consumidor
