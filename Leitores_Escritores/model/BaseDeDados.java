/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 08/06/2026
* Ultima alteracao.: 11/06/2026
* Nome.............: BaseDeDados.java
* Funcao...........: Implementacao da Base de Dados compartilhada.
*                    Contem o protocolo classico de Leitores/Escritores
*                    conforme Tanenbaum, utilizando dois semaforos:
*                    mutex (protege o contador de leitores) e
*                    db (controla o acesso exclusivo a base).
************************************************************************ */

package model;

import java.util.concurrent.Semaphore;

/* ***************************************************************
* Classe: BaseDeDados
* Funcao: Representa a base de dados compartilhada entre leitores
*         e escritores. Implementa o protocolo classico do livro
*         do Tanenbaum com semaforos mutex e db.
*************************************************************** */
public class BaseDeDados {

  private int leitores = 0;
  private final Semaphore mutex = new Semaphore(1);
  private final Semaphore db = new Semaphore(1);

  private volatile String conteudo = "Nenhuma materia publicada ainda.";
  private volatile int edicao = 0;

  /* ***************************************************************
  * Metodo: entradaLeitor
  * Funcao: Executa o protocolo de entrada do leitor na regiao
  *         critica. O primeiro leitor bloqueia os escritores
  *         adquirindo o semaforo db.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void entradaLeitor() throws InterruptedException {
    mutex.acquire();
    leitores++;

    if (leitores == 1) { 
      db.acquire();
    }

    mutex.release(); 
  } // Fim do metodo entradaLeitor

  /* ***************************************************************
  * Metodo: saidaLeitor
  * Funcao: Executa o protocolo de saida do leitor da regiao
  *         critica. O ultimo leitor libera os escritores
  *         liberando o semaforo db.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void saidaLeitor() throws InterruptedException {
    mutex.acquire();
    leitores--;

    if (leitores == 0) {  
      db.release();
    }

    mutex.release();      
  } // Fim do metodo saidaLeitor

  /* ***************************************************************
  * Metodo: entradaEscritor
  * Funcao: Executa o protocolo de entrada do escritor na regiao
  *         critica. Aguarda acesso exclusivo adquirindo db.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void entradaEscritor() throws InterruptedException {
    db.acquire();
  } // Fim do metodo entradaEscritor

  /* ***************************************************************
  * Metodo: saidaEscritor
  * Funcao: Executa o protocolo de saida do escritor da regiao
  *         critica. Libera o acesso exclusivo liberando db.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void saidaEscritor() {
    db.release();
  } // Fim do metodo saidaEscritor

  /* ***************************************************************
  * Metodo: leBaseDeDados
  * Funcao: Retorna o conteudo atual da base. Deve ser chamado
  *         DENTRO da regiao critica do leitor.
  * Parametros: nenhum
  * Retorno: String com o conteudo atual da base
  *************************************************************** */
  public String leBaseDeDados() {
    return conteudo;
  } // Fim do metodo leBaseDeDados

  /* ***************************************************************
  * Metodo: escreveBaseDeDados
  * Funcao: Atualiza o conteudo da base com a nova materia e
  *         incrementa o numero da edicao. Deve ser chamado
  *         DENTRO da regiao critica do escritor.
  * Parametros: novaMateria - texto da materia a ser publicada
  * Retorno: void
  *************************************************************** */
  public void escreveBaseDeDados(String novaMateria) {
    edicao++;
    this.conteudo = "[ Edicao " + edicao + " ] " + novaMateria;
  } // Fim do metodo escreveBaseDeDados

  /* ***************************************************************
  * Metodo: getNumLeitores
  * Funcao: Retorna o numero atual de leitores na regiao critica
  * Parametros: nenhum
  * Retorno: int com o numero de leitores ativos
  *************************************************************** */
  public int getNumLeitores() {
    return leitores;
  } // Fim do metodo getNumLeitores

  /* ***************************************************************
  * Metodo: getEdicao
  * Funcao: Retorna o numero da edicao atual da base
  * Parametros: nenhum
  * Retorno: int com o numero da edicao atual
  *************************************************************** */
  public int getEdicao() {
    return edicao;
  } // Fim do metodo getEdicao

  /* ***************************************************************
  * Metodo: resetar
  * Funcao: Restaura o estado inicial da base de dados, zerando
  *         contadores e limpando o conteudo publicado
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void resetar() {
    leitores = 0;
    edicao = 0;
    conteudo = "Nenhuma materia publicada ainda.";
  } // Fim do metodo resetar

} // Fim da classe BaseDeDados
