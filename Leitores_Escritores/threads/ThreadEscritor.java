/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 09/06/2026
* Ultima alteracao.: 15/06/2026
* Nome.............: ThreadEscritor.java
* Funcao...........: Representa a thread de um Reporter (Escritor) da
*                    redacao. Implementa o protocolo classico de
*                    escritores do Tanenbaum, com suporte a pausa,
*                    retomada e controle individual de velocidade.
************************************************************************ */

package threads;

import model.BaseDeDados;
import model.EstadoThread;
import util.Constantes;

import java.util.function.BiConsumer;

/* ***************************************************************
* Classe: ThreadEscritor
* Funcao: Thread que representa um Reporter (Escritor) da redacao.
*         Executa continuamente o ciclo: apura a pauta, aguarda
*         acesso exclusivo, entra na regiao critica, publica a
*         materia e sai da regiao critica.
*************************************************************** */
public class ThreadEscritor extends Thread {

  private final int id;
  private final BaseDeDados base;

  private volatile long velObtencao = 2000;
  private volatile long velEscrita = 2000;

  private volatile boolean pausado = false;
  private volatile boolean rodando = true;

  private final BiConsumer<Integer, EstadoThread> onEstadoMudou; //callback para atualizar a GUI

  private int pautaIdx;

  /* ***************************************************************
  * Metodo: ThreadEscritor (construtor)
  * Funcao: Inicializa a thread do escritor com seu id, referencia
  *         a base de dados e o callback de atualizacao da GUI.
  *         Define a thread como daemon e atribui um nome legivel.
  *         Cada reporter inicia em uma pauta diferente (id - 1).
  * Parametros: id - identificador do escritor (1 a 5)
  *             base - referencia a base de dados compartilhada
  *             onEstadoMudou - callback (id, estado) para a GUI
  * Retorno: nenhum
  *************************************************************** */
  public ThreadEscritor(int id, BaseDeDados base, BiConsumer<Integer, EstadoThread> onEstadoMudou) {
    this.id = id;
    this.base = base;
    this.onEstadoMudou = onEstadoMudou;
    this.pautaIdx = id - 1;
    setDaemon(true);
    setName("Reporter-" + id);
  } // Fim do construtor ThreadEscritor

  /* ***************************************************************
  * Metodo: run
  * Funcao: Loop principal do escritor. Executa continuamente o
  *         protocolo classico de escritores: apura a pauta
  *         (obtemDado), aguarda acesso exclusivo, entra na regiao
  *         critica, publica a materia (escreveBaseDeDados) e sai.
  *         Respeita pausas e interrupcoes.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  @Override
  public void run() {
    while (rodando) {
      try {

        verificarPausa();

        notificar(EstadoThread.OCIOSO);              // obtemDado() - reporter apura a materia
        Thread.sleep(velObtencao);                   // simula tempo de apuracao da pauta

        String novaMateria = "Reporter " + id + ": " + Constantes.PAUTAS[pautaIdx % Constantes.PAUTAS.length];
        pautaIdx++;

        verificarPausa();

        // Entrada do escritos
        notificar(EstadoThread.AGUARDANDO);
        base.entradaEscritor();

        // --- REGIAO CRITICA ---
        notificar(EstadoThread.ATIVO);
        base.escreveBaseDeDados(novaMateria);         // escreveBaseDeDados()
        Thread.sleep(velEscrita);                     // simula tempo de escrita na base
        base.saidaEscritor();
        // --- FIM REGIAO CRITICA ---

        verificarPausa();

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }

    notificar(EstadoThread.OCIOSO);
  } // Fim do metodo run

  /* ***************************************************************
  * Metodo: pausar
  * Funcao: Sinaliza a thread para entrar em estado de pausa
  *         no proximo ponto de verificacao
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public synchronized void pausar() {
    pausado = true;
  } // Fim do metodo pausar

  /* ***************************************************************
  * Metodo: retomar
  * Funcao: Remove o estado de pausa e acorda a thread caso
  *         esteja bloqueada em wait()
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public synchronized void retomar() {
    pausado = false;
    notifyAll();
  } // Fim do metodo retomar

  /* ***************************************************************
  * Metodo: parar
  * Funcao: Encerra definitivamente o loop da thread, sinalizando
  *         rodando=false e chamando interrupt() para desbloquear
  *         sleeps e waits pendentes
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void parar() {
    rodando = false;
    interrupt();
  } // Fim do metodo parar

  /* ***************************************************************
  * Metodo: setVelObtencao
  * Funcao: Ajusta o tempo simulado da operacao obtemDado
  * Parametros: ms - tempo em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setVelObtencao(long ms) {
    this.velObtencao = ms;
  } // Fim do metodo setVelObtencao

  /* ***************************************************************
  * Metodo: setVelEscrita
  * Funcao: Ajusta o tempo simulado da operacao escreveBaseDeDados
  * Parametros: ms - tempo em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setVelEscrita(long ms) {
    this.velEscrita = ms;
  } // Fim do metodo setVelEscrita

  /* ***************************************************************
  * Metodo: getId
  * Funcao: Retorna o identificador numerico deste escritor
  * Parametros: nenhum
  * Retorno: int com o id do escritor
  *************************************************************** */
  public int getEscritorId() {
    return id;
  } // Fim do metodo getId

  /* ***************************************************************
  * Metodo: isPausado
  * Funcao: Informa se a thread esta atualmente pausada
  * Parametros: nenhum
  * Retorno: boolean true se pausada, false caso contrario
  *************************************************************** */
  public boolean isPausado() {
    return pausado;
  } // Fim do metodo isPausado

  /* ***************************************************************
  * Metodo: notificar
  * Funcao: Dispara o callback onEstadoMudou para informar a GUI
  *         sobre a mudanca de estado desta thread
  * Parametros: estado - novo EstadoThread a ser notificado
  * Retorno: void
  *************************************************************** */
  private void notificar(EstadoThread estado) {
    onEstadoMudou.accept(id, estado);
  } // Fim do metodo notificar

  /* ***************************************************************
  * Metodo: verificarPausa
  * Funcao: Bloqueia a thread em wait() enquanto pausado=true,
  *         notificando a GUI com o estado PAUSADO a cada ciclo
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void verificarPausa() throws InterruptedException {
    synchronized (this) {
      while (pausado) {
        notificar(EstadoThread.PAUSADO);
        wait();
      }
    }
  } // Fim do metodo verificarPausa

} // Fim da classe ThreadEscritor
