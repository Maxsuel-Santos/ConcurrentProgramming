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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import model.Carro;
import model.Percurso;
import model.Vertice;
import util.Constantes;
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

                garantirRegioesDoTrechoAtualEProximo(indiceAtual);

                if (!carro.isAtivo()) {
                    liberarTodasAsRegioes();
                    break;
                }

                moverParaVertice(destino);

                carro.dormirComPausa(carro.getTempoPassoMs());

                liberarRegioesSeForSaida(indiceAtual);

                carro.avancarUmTrecho();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            liberarTodasAsRegioes();
        }
    }

    /* ***************************************************************
    * Metodo: garantirRegioesDoTrechoAtualEProximo
    * Funcao: Adquire todos os semaforos das RCs que protegem o trecho
    *         atual e o proximo trecho antes de o carro sair do ponto em
    *         que esta'. Esse lookahead evita que o carro avance ate' o
    *         centro de um cruzamento para so' entao descobrir que deve
    *         esperar: ele para no vertice anterior, preservando a
    *         animacao reta original.
    * Parametros: @param indice posicao atual do carro no ciclo
    * Retorno: sem retorno
    * Excecoes: InterruptedException se a espera for interrompida
    *************************************************************** */
    private void garantirRegioesDoTrechoAtualEProximo(int indice) throws InterruptedException {
        Set<String> regioesNecessarias = new TreeSet<>();
        regioesNecessarias.addAll(percurso.getRegioesDoTrecho(indice));
        regioesNecessarias.addAll(percurso.getRegioesDoTrecho(indice + 1));

        List<String> adquiridasNestaTentativa = new ArrayList<>();
        boolean precisouEsperar = false;

        for (String nomeRegiao : regioesNecessarias) {
            if (regioesOcupadasAtualmente.containsKey(nomeRegiao)) {
                continue;
            }

            Semaphore semaforo = gerenciadorSemaforos.getSemaforo(nomeRegiao);
            if (semaforo != null) {
                if (semaforo.tryAcquire()) {
                    regioesOcupadasAtualmente.put(nomeRegiao, semaforo);
                    adquiridasNestaTentativa.add(nomeRegiao);
                    continue;
                }

                precisouEsperar = true;
                liberarRegioesAdquiridasNaTentativa(adquiridasNestaTentativa);
                break;
            }
        }

        if (precisouEsperar) {
            moverParaPontoDeEspera(indice);
        }

        for (String nomeRegiao : regioesNecessarias) {
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

    private void liberarRegioesAdquiridasNaTentativa(List<String> nomesRegioes) {
        for (String nomeRegiao : nomesRegioes) {
            Semaphore semaforo = regioesOcupadasAtualmente.remove(nomeRegiao);
            if (semaforo != null) {
                semaforo.release();
            }
        }
    }

    private void moverParaPontoDeEspera(int indice) {
        Vertice entrada = percurso.getVertice(indice);
        Vertice destino = percurso.getVertice(indice + 1);

        double dx = destino.getX() - entrada.getX();
        double dy = destino.getY() - entrada.getY();
        double distancia = Math.sqrt(dx * dx + dy * dy);

        if (distancia == 0) {
            return;
        }

        double recuo = Constantes.RECUO_ESPERA_REGIAO_CRITICA_PX;
        double xEspera = entrada.getX() - (dx / distancia) * recuo;
        double yEspera = entrada.getY() - (dy / distancia) * recuo;

        carro.setPosicaoAtual(xEspera, yEspera);
        if (aoMover != null) {
            aoMover.accept(carro);
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
