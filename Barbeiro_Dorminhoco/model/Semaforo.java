/* *********************************************************************
* Autor............: Arthur Leite Felix
* Matricula........: 202511527
* Inicio...........: 12/06/2026
* Ultima alteracao.: 16/06/2026
* Nome.............: Semaforo.java
* Funcao...........: Prover a representacao customizada de um semaforo contador classico para controle de sincronismo e barreiras de acesso
************************************************************************ */

package model;

public class Semaforo {
    private int valor;

    /* *********************************************************************
    * Nome.............: Semaforo (Construtor)
    * Funcao...........: Inicializa a variavel inteira de contagem interna com o valor passado por parâmetro
    ************************************************************************ */
    public Semaforo(int valorInicial) {
        this.valor = valorInicial;
    }

    /* *********************************************************************
    * Nome.............: Down
    * Funcao...........: Operacao de decremento ou bloqueio; retém a execucao caso o contador interno esteja zerado ou negativo
    ************************************************************************ */
    public synchronized void Down() throws InterruptedException {
        while (this.valor <= 0) {
            wait();
        }
        this.valor--;
    }

    /* *********************************************************************
    * Nome.............: Up
    * Funcao...........: Operacao de incremento e sinalizacao; adiciona um recurso a contagem e libera threads suspensas
    ************************************************************************ */
    public synchronized void Up() {
        this.valor++;
        notify(); 
    }

    /* *********************************************************************
    * Nome.............: getValor
    * Funcao...........: Retorna o estado atual da variavel contadora inteira armazenada no semaforo
    ************************************************************************ */
    public synchronized int getValor() {
        return this.valor;
    }
}