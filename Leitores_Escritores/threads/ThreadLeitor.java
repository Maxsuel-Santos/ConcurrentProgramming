/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 08/06/2026
* Ultima alteracao.: 11/06/2026
* Nome.............: ThreadLeitor.java
* Funcao...........: Representa a thread de um Editor (Leitor) da
*                    redacao. Implementa o protocolo classico de
*                    leitores do Tanenbaum, com suporte a pausa,
*                    retomada e controle individual de velocidade.
************************************************************************ */

package threads;

import model.BaseDeDados;
import model.EstadoThread;

import java.util.function.BiConsumer;

/* ***************************************************************
* Classe: ThreadLeitor
* Funcao: Thread que representa um Editor (Leitor) da redacao.
*         Executa continuamente o ciclo: aguarda acesso, entra
*         na regiao critica, le a base, sai e utiliza o dado lido.
*************************************************************** */
public class ThreadLeitor extends Thread {

  private final int id;
  private final BaseDeDados base;

  private volatile long velLeitura = 1500;
  private volatile long velUtilizacao = 1500;

  private volatile boolean pausado = false;
  private volatile boolean rodando = true;

  private final BiConsumer<Integer, EstadoThread> onEstadoMudou;

  private volatile String ultimaMateria = "";

  /* ***************************************************************
  * Metodo: ThreadLeitor (construtor)
  * Funcao: Inicializa a thread do leitor com seu id, referencia
  *         a base de dados e o callback de atualizacao da GUI.
  *         Define a thread como daemon e atribui um nome legivel.
  * Parametros: id - identificador do leitor (1 a 5)
  *             base - referencia a base de dados compartilhada
  *             onEstadoMudou - callback (id, estado) para a GUI
  * Retorno: nenhum
  *************************************************************** */
  public ThreadLeitor(int id, BaseDeDados base, BiConsumer<Integer, EstadoThread> onEstadoMudou) {
    this.id = id;
    this.base = base;
    this.onEstadoMudou = onEstadoMudou;
    setDaemon(true);
    setName("Editor-" + id);
  } // Fim do construtor ThreadLeitor

  /* ***************************************************************
  * Metodo: run
  * Funcao: Loop principal do leitor. Executa continuamente o
  *         protocolo classico de leitores: notifica aguardando,
  *         entra na regiao critica, le a base, sai e utiliza
  *         o dado lido. Respeita pausas e interrupcoes.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  @Override
  public void run() {
    while (rodando) {
      try {

        verificarPausa();

        notificar(EstadoThread.AGUARDANDO);

        base.entradaLeitor();                   // O leitor tem acesso a base de dados

        // --- REGIAO CRITICA ---
        notificar(EstadoThread.ATIVO);

        ultimaMateria = base.leBaseDeDados();   // leBaseDeDados()

        Thread.sleep(velLeitura);               // simula tempo de leitura

        base.saidaLeitor();                     // O leitor sai da base de dados
        // --- FIM DA REGIAO CRITICA ---

        notificar(EstadoThread.OCIOSO);

        Thread.sleep(velUtilizacao);            // simula uso do dado lido

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } // Fim do try-catch
    } // Fim do while

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
  * Metodo: setVelLeitura
  * Funcao: Ajusta o tempo simulado da operacao leBaseDeDados
  * Parametros: ms - tempo em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setVelLeitura(long ms) {
    this.velLeitura = ms;
  } // Fim do metodo setVelLeitura

  /* ***************************************************************
  * Metodo: setVelUtilizacao
  * Funcao: Ajusta o tempo simulado da operacao utilizaDadoLido
  * Parametros: ms - tempo em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setVelUtilizacao(long ms) {
    this.velUtilizacao = ms;
  } // Fim do metodo setVelUtilizacao

  /* ***************************************************************
  * Metodo: getId
  * Funcao: Retorna o identificador numerico deste leitor
  * Parametros: nenhum
  * Retorno: int com o id do leitor
  *************************************************************** */
  public int getLeitorId() {
    return id;
  } // Fim do metodo getId

  /* ***************************************************************
  * Metodo: getUltimaMateria
  * Funcao: Retorna o ultimo conteudo lido da base de dados
  * Parametros: nenhum
  * Retorno: String com o conteudo da ultima leitura
  *************************************************************** */
  public String getUltimaMateria() {
    return ultimaMateria;
  } // Fim do metodo getUltimaMateria

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
  * Funcao: Bloqueia a thread em wait() enquanto pausado = true,
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

} // Fim da classe ThreadLeitor
