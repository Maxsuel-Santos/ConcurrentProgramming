/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 08/06/2026
* Ultima alteracao.: 18/06/2026
* Nome.............: LeitoresEscritores.java
* Funcao...........: Implementa o protocolo classico de
*                    Leitores/Escritores conforme Tanenbaum,
*                    com acquire e release no lugar de down e up.
*                    Os metodos leitor() e escritor() sao chamados
*                    diretamente pelas threads criadas no controller.
************************************************************************ */

package model;

import java.util.concurrent.Semaphore;
import java.util.function.IntConsumer;

/* ***************************************************************
* Classe: LeitoresEscritores
* Funcao: Controla a sincronizacao entre leitores e escritores
*         sobre a base de dados compartilhada, seguindo o
*         protocolo classico do Tanenbaum com semaforos.
*************************************************************** */
public class LeitoresEscritores {

  private final Semaphore mutex = new Semaphore(1);

  private final Semaphore db = new Semaphore(1);

  private int leitores = 0;

  private volatile String conteudo = "Nenhuma materia publicada ainda.";

  private volatile int edicao = 0;

  // Velocidades individuais por id (indice 0 ignorado)
  private final long[] velLeitura = new long[6];
  private final long[] velUtilizacao = new long[6];
  private final long[] velObtencao = new long[6];
  private final long[] velEscrita = new long[6];

  // Flags de pausa individuais por id (indice 0 ignorado)
  // Arrays separados para leitores e escritores evitam que o leitor N e o escritor N compartilhem o mesmo indice e se pausem mutuamente
  private final boolean[] pausadoLeitor = new boolean[6];
  private final boolean[] pausadoEscritor = new boolean[6];

  private final Semaphore[] portaoPausaLeitor = new Semaphore[6];
  private final Semaphore[] portaoPausaEscritor = new Semaphore[6];

  // Pautas disponiveis para os reporters
  private static final String[] PAUTAS = {
    "Exclusivo: Descoberta cientifica revoluciona a medicina",
    "Politica: Novo projeto de lei aprovado por unanimidade",
    "Economia: Mercado registra alta historica na bolsa",
    "Esportes: Time local conquista campeonato estadual",
    "Cultura: Festival de musica atrai milhares ao centro",
    "Tecnologia: Startup brasileira capta investimento recorde",
    "Internacional: Cupula mundial discute mudancas climaticas",
    "Cidade: Obras de reforma transformam parque urbano",
    "Educacao: Nova politica amplia acesso a universidades",
    "Saude: Campanha de vacinacao supera meta nacional"
  };

  // Callbacks para notificar a GUI
  private IntConsumer onLeitorAguardando = id -> {};
  private IntConsumer onLeitorLendo = id -> {};
  private IntConsumer onLeitorOcioso = id -> {};
  private IntConsumer onLeitorPausado = id -> {};
  private IntConsumer onEscritorAguardando = id -> {};
  private IntConsumer onEscritorEscrevendo = id -> {};
  private IntConsumer onEscritorOcioso = id -> {};
  private IntConsumer onEscritorPausado = id -> {};

  /* ***************************************************************
  * Metodo: LeitoresEscritores (construtor)
  * Funcao: Inicializa os portoes de pausa individuais (abertos) e
  *         os valores padrao de velocidade para cada leitor e
  *         escritor.
  * Parametros: nenhum
  * Retorno: nenhum
  *************************************************************** */
  public LeitoresEscritores() {
    for (int i = 1; i <= 5; i++) {
      portaoPausaLeitor[i] = new Semaphore(1);
      portaoPausaEscritor[i] = new Semaphore(1);
      velLeitura[i]  = 1500;
      velUtilizacao[i] = 1500;
      velObtencao[i] = 2000;
      velEscrita[i] = 2000;
    }
  } // Fim do construtor LeitoresEscritores

  /* ***************************************************************
  * Metodo: leitor
  * Funcao: Implementa o loop infinito do leitor conforme Tanenbaum:
  *
  *         while(true) {
  *           mutex.acquire()
  *             leitores++
  *             if leitores == 1: db.acquire()
  *           mutex.release()
  *           leBaseDeDados()        <- REGIAO CRITICA
  *           mutex.acquire()
  *             leitores--
  *             if leitores == 0: db.release()
  *           mutex.release()
  *           utilizaDadoLido()
  *         }
  *
  * Parametros: id - identificador do leitor (1 a 5)
  * Retorno: void
  *************************************************************** */
  public void leitor(int id) {
    while (true) {
      try {

        verificarPausa(id, true);

        onLeitorAguardando.accept(id);

        // --- ENTRADA DO LEITOR ---
        mutex.acquire();
        leitores++;

        if (leitores == 1) {
          db.acquire();
        }

        mutex.release();
        // --- FIM DA ENTRADA ---

        // --- REGIAO CRITICA ---
        onLeitorLendo.accept(id);
        Thread.sleep(velLeitura[id]);       // leBaseDeDados() - simula leitura
        // --- FIM DA REGIAO CRITICA ---

        // --- SAIDA DO LEITOR ---
        mutex.acquire();
        leitores--;

        if (leitores == 0) {
          db.release();
        }

        mutex.release();
        // --- FIM DA SAIDA ---

        // utilizaDadoLido()
        onLeitorOcioso.accept(id);
        Thread.sleep(velUtilizacao[id]);    // simula uso do dado lido

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  } // Fim do metodo leitor

  /* ***************************************************************
  * Metodo: escritor
  * Funcao: Implementa o loop infinito do escritor conforme Tanenbaum:
  *
  *         while(true) {
  *           obtemDado()
  *           db.acquire()
  *           escreveBaseDeDados()   <- REGIAO CRITICA
  *           db.release()
  *         }
  *
  * Parametros: id - identificador do escritor (1 a 5)
  * Retorno: void
  *************************************************************** */
  public void escritor(int id) {
    int pautaIdx = id - 1;                  // cada reporter inicia em uma pauta distinta

    while (true) {
      try {

        verificarPausa(id, false);

        // obtemDado() - reporter apura a materia
        onEscritorOcioso.accept(id);
        Thread.sleep(velObtencao[id]);      // simula tempo de apuracao da pauta

        String novaMateria = "Reporter " + id + ": " + PAUTAS[pautaIdx % PAUTAS.length];
        pautaIdx++;

        verificarPausa(id, false);

        // --- ENTRADA DO ESCRITOR ---
        onEscritorAguardando.accept(id);
        db.acquire();
        // --- FIM DA ENTRADA ---

        // --- REGIAO CRITICA ---
        onEscritorEscrevendo.accept(id);
        edicao++;
        conteudo = "[ Edicao " + edicao + " ] " + novaMateria;
        Thread.sleep(velEscrita[id]);       // escreveBaseDeDados() - simula escrita
        // --- FIM DA REGIAO CRITICA ---

        // --- SAIDA DO ESCRITOR ---
        db.release();
        // --- FIM DA SAIDA ---

        verificarPausa(id, false);

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  } // Fim do metodo escritor

  /* ***************************************************************
  * Metodo: verificarPausa
  * Funcao: Bloqueia a thread tentando atravessar o portao de pausa
  *         (acquire seguido de release imediato). Se o portao
  *         estiver fechado (pausado[id]=true), o acquire() bloqueia
  *         a thread ali mesmo, sem busy-wait, ate retomarLeitor()
  *         reabrir o portao com release(). Notifica a GUI com o
  *         estado PAUSADO antes de bloquear.
  * Parametros: id - id da thread a verificar
  *             ehLeitor - true se leitor, false se escritor
  * Retorno: void
  *************************************************************** */
  private void verificarPausa(int id, boolean ehLeitor) throws InterruptedException {
    if (ehLeitor) {
      if (pausadoLeitor[id]) {
        onLeitorPausado.accept(id);
      }

      portaoPausaLeitor[id].acquire();
      portaoPausaLeitor[id].release();
    } 
    else {
      if (pausadoEscritor[id]) {
        onEscritorPausado.accept(id);
      }

      portaoPausaEscritor[id].acquire();
      portaoPausaEscritor[id].release();
    }
  } // Fim do metodo verificarPausa

  /* ***************************************************************
  * Metodo: pausarLeitor
  * Funcao: Sinaliza o leitor de id informado para pausar, fechando
  *         o portao de pausa correspondente (acquire). So fecha o
  *         portao se ele ainda nao estiver fechado, evitando que
  *         chamadas redundantes travem a thread da GUI (que nao
  *         pode bloquear em acquire) ou acumulem fechamentos.
  * Parametros: id - id do leitor a pausar
  * Retorno: void
  *************************************************************** */
  public void pausarLeitor(int id) {
    if (!pausadoLeitor[id]) {
      pausadoLeitor[id] = true;
      portaoPausaLeitor[id].tryAcquire();
    }
  } // Fim do metodo pausarLeitor

  /* ***************************************************************
  * Metodo: retomarLeitor
  * Funcao: Remove a pausa do leitor de id informado, reabrindo o
  *         portao de pausa e liberando a thread bloqueada em
  *         verificarPausa. So reabre se o portao estiver fechado.
  * Parametros: id - id do leitor a retomar
  * Retorno: void
  *************************************************************** */
  public void retomarLeitor(int id) {
    if (pausadoLeitor[id]) {
      pausadoLeitor[id] = false;
      portaoPausaLeitor[id].release();
    }
  } // Fim do metodo retomarLeitor

  /* ***************************************************************
  * Metodo: pausarEscritor
  * Funcao: Sinaliza o escritor de id informado para pausar, fechando
  *         seu portao de pausa exclusivo. Nao interfere com o leitor
  *         de mesmo id.
  * Parametros: id - id do escritor a pausar
  * Retorno: void
  *************************************************************** */
  public void pausarEscritor(int id) {
    if (!pausadoEscritor[id]) {
      pausadoEscritor[id] = true;
      portaoPausaEscritor[id].tryAcquire();
    }
  } // Fim do metodo pausarEscritor

  /* ***************************************************************
  * Metodo: retomarEscritor
  * Funcao: Remove a pausa do escritor de id informado, reabrindo
  *         seu portao de pausa exclusivo. Nao interfere com o
  *         leitor de mesmo id.
  * Parametros: id - id do escritor a retomar
  * Retorno: void
  *************************************************************** */
  public void retomarEscritor(int id) {
    if (pausadoEscritor[id]) {
      pausadoEscritor[id] = false;
      portaoPausaEscritor[id].release();
    }
  } // Fim do metodo retomarEscritor

  /* ***************************************************************
  * Metodo: isPausadoLeitor
  * Funcao: Informa se o leitor de id informado esta pausado
  * Parametros: id - id do leitor a consultar
  * Retorno: boolean true se pausado, false caso contrario
  *************************************************************** */
  public boolean isPausadoLeitor(int id) {
    return pausadoLeitor[id];
  } // Fim do metodo isPausadoLeitor

  /* ***************************************************************
  * Metodo: isPausadoEscritor
  * Funcao: Informa se o escritor de id informado esta pausado
  * Parametros: id - id do escritor a consultar
  * Retorno: boolean true se pausado, false caso contrario
  *************************************************************** */
  public boolean isPausadoEscritor(int id) {
    return pausadoEscritor[id];
  } // Fim do metodo isPausadoEscritor

  /* ***************************************************************
  * Metodo: getConteudo
  * Funcao: Retorna o conteudo atual da base de dados
  * Parametros: nenhum
  * Retorno: String com o conteudo da base
  *************************************************************** */
  public String getConteudo() {
    return conteudo;
  } // Fim do metodo getConteudo

  /* ***************************************************************
  * Metodo: getEdicao
  * Funcao: Retorna o numero da edicao atual
  * Parametros: nenhum
  * Retorno: int com o numero da edicao
  *************************************************************** */
  public int getEdicao() {
    return edicao;
  } // Fim do metodo getEdicao

  /* ***************************************************************
  * Metodo: getLeitoresAtivos
  * Funcao: Retorna o numero de leitores ativos na regiao critica
  * Parametros: nenhum
  * Retorno: int com o numero de leitores ativos
  *************************************************************** */
  public int getLeitoresAtivos() {
    return leitores;
  } // Fim do metodo getLeitoresAtivos

  /* ***************************************************************
  * Metodo: setVelLeitura
  * Funcao: Ajusta o tempo simulado de leBaseDeDados para o leitor id
  * Parametros: id - id do leitor
  *             ms - tempo em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setVelLeitura(int id, long ms) {
    velLeitura[id] = ms;
  } // Fim do metodo setVelLeitura

  /* ***************************************************************
  * Metodo: setVelUtilizacao
  * Funcao: Ajusta o tempo simulado de utilizaDadoLido para o leitor id
  * Parametros: id - id do leitor
  *             ms - tempo em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setVelUtilizacao(int id, long ms) {
    velUtilizacao[id] = ms;
  } // Fim do metodo setVelUtilizacao

  /* ***************************************************************
  * Metodo: setVelObtencao
  * Funcao: Ajusta o tempo simulado de obtemDado para o escritor id
  * Parametros: id - id do escritor
  *             ms - tempo em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setVelObtencao(int id, long ms) {
    velObtencao[id] = ms;
  } // Fim do metodo setVelObtencao

  /* ***************************************************************
  * Metodo: setVelEscrita
  * Funcao: Ajusta o tempo simulado de escreveBaseDeDados para o escritor id
  * Parametros: id - id do escritor
  *             ms - tempo em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setVelEscrita(int id, long ms) {
    velEscrita[id] = ms;
  } // Fim do metodo setVelEscrita

  // SETTERS DE CALLBACKS
  public void setOnLeitorAguardando(IntConsumer cb) { 
    onLeitorAguardando = cb; 
  }

  public void setOnLeitorLendo(IntConsumer cb) { 
    onLeitorLendo = cb; 
  }

  public void setOnLeitorOcioso(IntConsumer cb) { 
    onLeitorOcioso = cb;
   }

  public void setOnLeitorPausado(IntConsumer cb) { 
    onLeitorPausado = cb; 
  }

  public void setOnEscritorAguardando(IntConsumer cb) { 
    onEscritorAguardando = cb; 
  }

  public void setOnEscritorEscrevendo(IntConsumer cb) { 
    onEscritorEscrevendo = cb; 
  }

  public void setOnEscritorOcioso(IntConsumer cb) { 
    onEscritorOcioso = cb; 
  }

  public void setOnEscritorPausado(IntConsumer cb) { 
    onEscritorPausado = cb; 
  }

} // Fim da classe LeitoresEscritores
