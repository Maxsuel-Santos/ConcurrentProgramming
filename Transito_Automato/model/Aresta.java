/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 23/06/2026
* Nome.............: Aresta.java
* Funcao...........: Representa um trecho de rua (RHxx ou RVxx) que liga
*                    dois vertices da malha. Se o trecho for utilizado
*                    por mais de um carro, ele e' uma REGIAO CRITICA e
*                    guarda uma referencia ao Semaphore que a protege
*                    (compartilhado entre todas as Arestas com o mesmo
*                    nome, ver util.GerenciadorSemaforos). Se o trecho
*                    for exclusivo de um unico carro, semaforo fica null
*                    e o carro nunca precisa esperar para usa-lo.
************************************************************************ */

package model;

import java.util.concurrent.Semaphore;

public class Aresta {

    private final String nome;     // ex: "RH01", "RV30"
    private final Vertice origem;
    private final Vertice destino;
    private final Semaphore semaforo; // null se o trecho NAO for compartilhado

    public Aresta(String nome, Vertice origem, Vertice destino, Semaphore semaforo) {
        this.nome = nome;
        this.origem = origem;
        this.destino = destino;
        this.semaforo = semaforo;
    }

    public String getNome() {
        return nome;
    }

    public Vertice getOrigem() {
        return origem;
    }

    public Vertice getDestino() {
        return destino;
    }

    public Semaphore getSemaforo() {
        return semaforo;
    }

    /* ***************************************************************
    * Metodo: ehRegiaoCritica
    * Funcao: Indica se este trecho precisa de sincronizacao porque e'
    *         compartilhado por mais de um carro.
    * Parametros: nenhum
    * Retorno: @return boolean true se houver semaforo associado
    *************************************************************** */
    public boolean ehRegiaoCritica() {
        return semaforo != null;
    }

    /* ***************************************************************
    * Metodo: outroExtremo
    * Funcao: Dado um vertice de entrada nesta aresta, devolve o outro
    *         extremo (a aresta nao tem direcao fixa: pode ser
    *         percorrida de origem->destino ou de destino->origem,
    *         dependendo do sentido do carro).
    * Parametros: @param de vertice de partida
    * Retorno: @return Vertice extremo oposto a "de"
    *************************************************************** */
    public Vertice outroExtremo(Vertice de) {
        if (de.equals(origem)) {
            return destino;
        }
        if (de.equals(destino)) {
            return origem;
        }
        throw new IllegalArgumentException(
            "Vertice " + de + " nao pertence a aresta " + nome
        );
    }

    @Override
    public String toString() {
        return nome + "[" + origem + " - " + destino + "]"
            + (ehRegiaoCritica() ? " (regiao critica)" : "");
    }
}