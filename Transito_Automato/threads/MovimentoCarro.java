/* ***************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 01/07/2026
* Ultima alteracao.: 01/07/2026
* Nome.............: MovimentoCarro.java
* Funcao...........: Define o contrato de animacao do movimento de um carro.
************************************************************************ */
package threads;

import model.Carro;

/* ***************************************************************
* Interface: MovimentoCarro
* Funcao: Define o contrato de animacao do movimento de um carro.
*************************************************************** */
@FunctionalInterface
public interface MovimentoCarro {
    /* ***************************************************************
    * Metodo: mover
    * Funcao: Move o carro durante o tempo informado.
    * Parametros: carro carro movimentado; duracaoMs duracao em milissegundos
    * Retorno: sem retorno
    *************************************************************** */
    void mover(Carro carro, long duracaoMs) throws InterruptedException;
}
