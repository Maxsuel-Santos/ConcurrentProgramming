/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 23/06/2026
* Nome.............: ThreadCarro.java
* Funcao...........: Thread responsavel por mover um unico Carro ao
*                    longo do seu Percurso, em loop infinito, respeitando
*                    pausa/retomada e velocidade individuais.
*
*                    Sincronizacao (regioes criticas): antes de entrar em
*                    cada trecho (Aresta), a thread tenta adquirir o
*                    Semaphore daquele trecho (se ele for uma regiao
*                    critica compartilhada com outro carro). So' libera o
*                    semaforo do trecho ANTERIOR depois de garantir o do
*                    trecho atual - isso evita que dois carros fiquem
*                    "se esperando" em arestas diferentes (livre de
*                    deadlock, pois cada thread so' tenta adquirir UM
*                    semaforo novo por vez, nunca dois ao mesmo tempo).
*
*                    Quando o trecho nao e' regiao critica (uso
*                    exclusivo daquele carro), nao ha' nenhuma espera:
*                    o carro passa direto.
************************************************************************ */

package threads;

import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import model.Aresta;
import model.Carro;
import model.Vertice;

public class ThreadCarro extends Thread {

    private final Carro carro;
    private final Consumer<Carro> aoMover; // callback p/ a UI redesenhar o carro a cada passo
    private Semaphore semaforoOcupadoAtualmente = null;

    public ThreadCarro(Carro carro, Consumer<Carro> aoMover) {
        super("ThreadCarro-" + carro.getNumero());
        this.carro = carro;
        this.aoMover = aoMover;
        setDaemon(true); // nao impede o encerramento da aplicacao
    }

    @Override
    public void run() {
        try {
            while (carro.isAtivo() && !isInterrupted()) {

                Aresta proximaAresta = carro.getProximaAresta();
                Vertice destino = carro.getVerticeDestino();

                entrarNoTrecho(proximaAresta);

                if (!carro.isAtivo()) {
                    liberarSemaforoAtual();
                    break;
                }

                moverParaVertice(destino);

                carro.avancarUmTrecho();

                carro.dormirComPausa(carro.getTempoPassoMs());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            liberarSemaforoAtual();
        }
    }

    /* ***************************************************************
    * Metodo: entrarNoTrecho
    * Funcao: Adquire o semaforo do trecho informado (bloqueando ate'
    *         que ele esteja livre, se for uma regiao critica ocupada
    *         por outro carro) e, somente depois de garanti-lo, libera
    *         o semaforo do trecho anterior. Se o trecho nao for regiao
    *         critica, nao faz nada (passagem livre).
    * Parametros: @param aresta trecho que o carro vai percorrer agora
    * Retorno: sem retorno
    * Excecoes: InterruptedException se a espera for interrompida
    *************************************************************** */
    private void entrarNoTrecho(Aresta aresta) throws InterruptedException {
        Semaphore semaforoNovo = aresta.getSemaforo();

        if (semaforoNovo != null) {
            semaforoNovo.acquire(); // aguarda o trecho ficar livre
        }

        // so' libera o trecho anterior depois de garantir o novo:
        // evita um instante em que o carro "solta tudo" e outro
        // carro avanca para uma posicao que ainda nao deveria estar livre
        liberarSemaforoAtual();

        semaforoOcupadoAtualmente = semaforoNovo;
    }

    private void liberarSemaforoAtual() {
        if (semaforoOcupadoAtualmente != null) {
            semaforoOcupadoAtualmente.release();
            semaforoOcupadoAtualmente = null;
        }
    }

    /* ***************************************************************
    * Metodo: moverParaVertice
    * Funcao: Atualiza a posicao logica do carro para o vertice destino
    *         e notifica a UI (via callback) para que ela anime o
    *         deslocamento na tela.
    * Parametros: @param destino vertice de chegada do trecho atual
    * Retorno: sem retorno
    *************************************************************** */
    private void moverParaVertice(Vertice destino) {
        carro.setPosicaoAtual(destino.getX(), destino.getY());
        if (aoMover != null) {
            aoMover.accept(carro);
        }
    }
}
