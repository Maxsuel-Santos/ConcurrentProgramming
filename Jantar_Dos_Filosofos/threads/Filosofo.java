/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 29/05/2026
* Ultima alteracao.: 30/05/2026
* Nome.............: Filosofo.java
* Funcao...........: Thread de cada filosofo.
*                    Implementa EXATAMENTE o pseudocodigo do livro
*                    de Tanenbaum (Sistemas Operacionais - 2th edition):
*
*                    void philosopher(int i) {
*                      while (TRUE) {
*                        think();
*                        take_forks(i);
*                        eat();
*                        put_forks(i);
*                      }
*                    }
*
*                    OBS: A implementacao sofreu adaptacoes para o
*                    contexto da simulacao (pausa, velocidade individual,
*                    callbacks de GUI).
************************************************************************ */
package threads;

import model.JantarFilosofos;

/* ***************************************************************
* Classe: Filosofo
* Funcao: Thread que representa um filosofo na mesa. Executa o
*         ciclo pensar -> pegar garfos -> comer -> devolver garfos
*         indefinidamente, respeitando pausa e velocidades individuais.
*************************************************************** */
public class Filosofo extends Thread {

  private final int id;

  private final JantarFilosofos jantar;

  private volatile int pensarMs;

  private volatile int comerMs;

  private volatile boolean pausado = false;

  /* ***************************************************************
  * Metodo: Filosofo (construtor)
  * Funcao: Inicializa o filosofo com seu indice, contexto
  *         compartilhado e velocidades iniciais.
  * Parametros: @param id       indice do filosofo (0 a N-1)
  *             @param jantar   contexto compartilhado
  *             @param pensarMs velocidade inicial de pensar em ms
  *             @param comerMs  velocidade inicial de comer em ms
  * Retorno: nao possui
  *************************************************************** */
  public Filosofo(int id, JantarFilosofos jantar, int pensarMs, int comerMs) {
    this.id = id;
    this.jantar = jantar;
    this.pensarMs = pensarMs;
    this.comerMs = comerMs;
    setDaemon(true); // encerra junto com a JVM
  } // Fim do construtor

  /* ***************************************************************
  * Metodo: run
  * Funcao: Laco principal do filosofo — traducao direta do
  *         pseudocodigo C do livro texto de Tanenbaum.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  @Override
  public void run() {
    try {
      while (!isInterrupted()) {           // Loop principal: mantem o filosofo ativo ate que a thread seja parada

        esperarSeEmPausa();                // Congela o filosofo se a simulacao for pausada pelo usuario

        if (isInterrupted()) break;        // Sai do loop se a thread foi interrompida durante a pausa

        think();                           // think() - filosofo pensa

        if (isInterrupted()) break;        // Sai do loop se a thread foi interrompida enquanto ele pensava

        jantar.pegarGarfos(id);            // take_forks(i) - tenta pegar os garfos

        if (isInterrupted()) break;        // Sai do loop se a thread foi parada enquanto esperava os garfos

        eat();                             // eat() - filosofo come

        if (isInterrupted()) break;        // Sai do loop se a thread foi interrompida enquanto ele comia

        jantar.devolverGarfos(id);         // put_forks(i) - devolve e acorda vizinhos

      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  } // Fim do metodo run

  /* ***************************************************************
  * Metodo: think
  * Funcao: Simula o tempo de pensar (think() do pseudocodigo).
  *         Dorme por pensarMs milissegundos.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void think() throws InterruptedException {
    Thread.sleep(pensarMs);
  } // Fim do metodo think

  /* ***************************************************************
  * Metodo: eat
  * Funcao: Simula o tempo de comer (eat() do pseudocodigo).
  *         Dorme por comerMs milissegundos.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private void eat() throws InterruptedException {
    Thread.sleep(comerMs);
  } // Fim do metodo eat

  /* ***************************************************************
  * Metodo: esperarSeEmPausa
  * Funcao: Bloqueia a thread enquanto o filosofo estiver pausado.
  *         Desbloqueia quando retomar() for chamado.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  private synchronized void esperarSeEmPausa() throws InterruptedException {
    while (pausado && !isInterrupted()) {
      wait();
    }
  } // Fim do metodo esperarSeEmPausa

  /* ***************************************************************
  * Metodo: pausar
  * Funcao: Pausa o filosofo no proximo ciclo do laco principal.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public synchronized void pausar() {
    pausado = true;
  } // Fim do metodo pausar

  /* ***************************************************************
  * Metodo: retomar
  * Funcao: Retoma o filosofo acordando o wait() interno.
  * Parametros: nao possui
  * Retorno: void
  *************************************************************** */
  public synchronized void retomar() {
    pausado = false;
    notifyAll();
  } // Fim do metodo retomar

  /* ***************************************************************
  * Metodo: isPausado
  * Funcao: Retorna se o filosofo esta pausado.
  * Parametros: nao possui
  * Retorno: @return boolean true se pausado
  *************************************************************** */
  public boolean isPausado() {
    return pausado;
  } // Fim do metodo isPausado

  /* ***************************************************************
  * Metodo: setPensarMs
  * Funcao: Atualiza a velocidade de pensar em tempo real.
  * Parametros: @param ms novo valor em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setPensarMs(int ms) {
    this.pensarMs = ms;
  } // Fim do metodo setPensarMs

  /* ***************************************************************
  * Metodo: setComerMs
  * Funcao: Atualiza a velocidade de comer em tempo real.
  * Parametros: @param ms novo valor em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setComerMs(int ms) {
    this.comerMs = ms;
  } // Fim do metodo setComerMs

  /* ***************************************************************
  * Metodo: getFilosofoId
  * Funcao: Retorna o indice do filosofo.
  * Parametros: nao possui
  * Retorno: @return int indice do filosofo
  *************************************************************** */
  public int getFilosofoId() {
    return id;
  } // Fim do metodo getFilosofoId

} // Fim da classe Filosofo
