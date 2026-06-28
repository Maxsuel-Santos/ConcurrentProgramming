/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: ThreadCarro.java
* Funcao...........: Thread responsavel por mover um unico Carro ao
*                    longo do seu Percurso, em loop infinito, respeitando
*                    pausa/retomada e velocidade individuais.
*
*                    SINCRONIZACAO POR REGIAO CRITICA: ao se aproximar
*                    de um trecho que e' ENTRADA de uma ou mais RCs, a
*                    thread adquire os semaforos dessas RCs ANTES de
*                    avancar. Ela so' libera cada semaforo ao concluir o
*                    trecho que e' SAIDA daquela mesma RC. Assim a
*                    verificacao acontece por regiao critica do arquivo,
*                    nao por aresta individual.
*
*                    Quando o trecho nao pertence a nenhuma zona (uso
*                    exclusivo daquele carro), nao ha' nenhuma espera: o
*                    carro passa direto.
************************************************************************ */

package threads;

import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Carro;
import model.Percurso;
import model.Vertice;
import util.GerenciadorSemaforos;

public class ThreadCarro extends Thread {

    private final Carro carro;
    private final Percurso percurso;
    private final GerenciadorSemaforos gerenciadorSemaforos;
    private final Consumer<Carro> aoMover; // callback: avisa a UI que ha' um novo trecho (origem->destino) para animar

    private final Map<String, Semaphore> regioesOcupadasAtualmente = new LinkedHashMap<>();

    public ThreadCarro(Carro carro, GerenciadorSemaforos gerenciadorSemaforos, Consumer<Carro> aoMover) {
        super("ThreadCarro-" + carro.getNumero());
        this.carro = carro;
        this.percurso = carro.getPercurso();
        this.gerenciadorSemaforos = gerenciadorSemaforos;
        this.aoMover = aoMover;
        setDaemon(true); // nao impede o encerramento da aplicacao
    }

    @Override
    public void run() {
        try {
            while (carro.isAtivo() && !isInterrupted()) {

                int indiceAtual = carro.getIndiceCicloAtual();
                Vertice destino = carro.getVerticeDestino();

                garantirRegioesDoTrecho(indiceAtual);

                if (!carro.isAtivo()) {
                    liberarTodasAsRegioes();
                    break;
                }

                moverParaVertice(destino);

                liberarRegioesSeForSaida(indiceAtual);

                carro.avancarUmTrecho();

                carro.dormirComPausa(carro.getTempoPassoMs());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            liberarTodasAsRegioes();
        }
    }

    /* ***************************************************************
    * Metodo: garantirRegioesDoTrecho
    * Funcao: Adquire todos os semaforos das RCs que protegem o indice
    *         informado, antes de o carro entrar naquele trecho. Isso
    *         tambem cobre carros que iniciam a simulacao no meio de
    *         uma RC circular.
    * Parametros: @param indice posicao atual do carro no ciclo
    * Retorno: sem retorno
    * Excecoes: InterruptedException se a espera for interrompida
    *************************************************************** */
    private void garantirRegioesDoTrecho(int indice) throws InterruptedException {
        List<String> regioesDoTrecho = percurso.getRegioesDoTrecho(indice);
        for (String nomeRegiao : regioesDoTrecho) {
            if (regioesOcupadasAtualmente.containsKey(nomeRegiao)) {
                continue;
            }

            Semaphore semaforo = gerenciadorSemaforos.getSemaforo(nomeRegiao);
            if (semaforo != null) {
                semaforo.acquire();
                regioesOcupadasAtualmente.put(nomeRegiao, semaforo);
            }
        }
    }

    /* ***************************************************************
    * Metodo: liberarRegioesSeForSaida
    * Funcao: Libera todos os semaforos das RCs que terminam no indice
    *         informado.
    * Parametros: @param indice posicao do trecho que acabou de ser
    *             concluido
    * Retorno: sem retorno
    *************************************************************** */
    private void liberarRegioesSeForSaida(int indice) {
        for (String nomeRegiao : percurso.getRegioesSaida(indice)) {
            Semaphore semaforo = regioesOcupadasAtualmente.remove(nomeRegiao);
            if (semaforo != null) {
                semaforo.release();
            }
        }
    }

    private void liberarTodasAsRegioes() {
        for (Semaphore semaforo : regioesOcupadasAtualmente.values()) {
            semaforo.release();
        }
        regioesOcupadasAtualmente.clear();
    }

    /* ***************************************************************
    * Metodo: moverParaVertice
    * Funcao: Atualiza a posicao LOGICA do carro para o vertice destino
    *         (origem/destino do trecho, usados para a interpolacao) e
    *         notifica a UI via callback. A ThreadCarro NAO anima nada
    *         na tela - ela so' marca onde o carro estava e onde deve
    *         chegar; quem desenha a transicao suave entre esses dois
    *         pontos, ao longo da duracao de getTempoPassoMs(), e' o
    *         Controller (unico lugar que pode tocar em nodos JavaFX).
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
