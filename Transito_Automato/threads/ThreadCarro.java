/* ***************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 25/06/2026
* Ultima alteracao.: 12/07/2026
* Nome.............: ThreadCarro.java
* Funcao...........: Executa o protocolo concorrente de movimento de um carro.
************************************************************************ */
package threads;

import java.util.ArrayList;
import java.util.List;

import model.Carro;
import util.GerenciadorSemaforos;

/* ***************************************************************
* Classe: ThreadCarro
* Funcao: Executa o protocolo concorrente de movimento de um carro.
*************************************************************** */
public class ThreadCarro extends Thread {

    private final Carro carro;
    private final GerenciadorSemaforos semaforos;
    private final MovimentoCarro movimento;

    private volatile String estadoDiagnostico = "INICIANDO";
    private volatile long movimentosConcluidos;
    private volatile long janelasConcluidas;
    private volatile long ultimoProgressoNanos = System.nanoTime();
    private volatile long maiorEsperaReservaMs;

    /* ***************************************************************
    * Metodo: ThreadCarro
    * Funcao: Inicializa uma nova instancia de ThreadCarro.
    * Parametros: carro parametro carro; semaforos parametro semaforos; movimento parametro movimento
    * Retorno: sem retorno
    *************************************************************** */
    public ThreadCarro(Carro carro, GerenciadorSemaforos semaforos, MovimentoCarro movimento) {
        super("ThreadCarro-" + carro.getNumero());
        this.carro = carro;
        this.semaforos = semaforos;
        this.movimento = movimento;
        setDaemon(true);
    }

    /* ***************************************************************
    * Metodo: run
    * Funcao: Executa continuamente o protocolo de movimento do carro.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
   @Override
    public void run() {
        try {
            while (carro.isAtivo() && !isInterrupted()) {
                boolean pontoSeguro = semaforos.paradaSegura(
                    carro, carro.getIndiceAtual()
                );

                if (pontoSeguro) {
                    estadoDiagnostico = carro.isPausado()
                        ? "PAUSADO_EM_" + carro.getTrechoAtual().getNome()
                        : "PRONTO_EM_" + carro.getTrechoAtual().getNome();
                    carro.aguardarSePausado();
                }

                if (!carro.isAtivo() || isInterrupted()) {
                    break;
                }

                Janela janela = calcularProximaJanela();
                reservarJanela(janela);

                for (int posicao = 0; posicao < janela.indicesDestino.size(); posicao++) {
                    if (!carro.isAtivo() || isInterrupted()) {
                        throw new InterruptedException();
                    }

                    carro.prepararMovimento();
                    estadoDiagnostico = "MOVENDO_" + carro.getTrechoAtual().getNome()
                        + "_PARA_" + carro.getProximoTrecho().getNome();
                    movimento.mover(carro, carro.getDuracaoPassoMs());
                    carro.avancarIndice();
                    movimentosConcluidos++;
                    ultimoProgressoNanos = System.nanoTime();
                    semaforos.registrarMovimentoConcluido(carro);
                    semaforos.consolidarParadaIntermediaria(carro);

                    if (posicao + 1 < janela.indicesDestino.size()) {
                        List<Integer> restantes = new ArrayList<>(
                            janela.indicesDestino.subList(
                                posicao + 1, janela.indicesDestino.size()
                            )
                        );
                        semaforos.avancarNaJanela(carro, restantes.get(0));
                    }
                }

                semaforos.finalizarJanela(carro, false);
                janelasConcluidas++;
            }
        } catch (InterruptedException e) {
            interrupt();
        } finally {
            estadoDiagnostico = "FINALIZANDO";
            semaforos.liberarCarro(carro.getNumero());
            estadoDiagnostico = "FINALIZADA";
        }
    }

    /* ***************************************************************
    * Metodo: calcularProximaJanela
    * Funcao: Calcula proxima janela.
    * Parametros: emCorredor parametro emCorredor
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private Janela calcularProximaJanela() {
        int quantidade = carro.getPercurso().getQuantidadeTrechos();
        List<Integer> destinos = new ArrayList<>();
        int indice = normalizar(carro.getIndiceAtual() + 1, quantidade);

        for (int passos = 0; passos < quantidade; passos++) {
            destinos.add(indice);
            if (semaforos.paradaSegura(carro, indice)) {
                return new Janela(destinos);
            }
            indice = normalizar(indice + 1, quantidade);
        }

        throw new IllegalStateException(
            "Carro " + carro.getNumero() + " nao encontrou ponto seguro."
        );
    }

    /* ***************************************************************
    * Metodo: reservarJanela
    * Funcao: Reserva janela.
    * Parametros: janela parametro janela
    * Retorno: sem retorno
    *************************************************************** */
    private void reservarJanela(Janela janela) throws InterruptedException {
        estadoDiagnostico = "AGUARDANDO_RESERVA_" + janela.indicesDestino;

        long inicioEspera = System.nanoTime();
        semaforos.reservarJanelaDeCorredor(carro, janela.indicesDestino);
        maiorEsperaReservaMs = Math.max(
            maiorEsperaReservaMs,
            (System.nanoTime() - inicioEspera) / 1_000_000L
        );
    }

    /* ***************************************************************
    * Metodo: normalizar
    * Funcao: Normaliza .
    * Parametros: indice parametro indice; quantidade parametro quantidade
    * Retorno: valor calculado
    *************************************************************** */
    private int normalizar(int indice, int quantidade) {
        int resultado = indice % quantidade;
        return resultado < 0 ? resultado + quantidade : resultado;
    }

    /* ***************************************************************
    * Metodo: getMovimentosConcluidos
    * Funcao: Retorna movimentos concluidos.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public long getMovimentosConcluidos() { return movimentosConcluidos; }
    /* ***************************************************************
    * Metodo: getJanelasConcluidas
    * Funcao: Retorna janelas concluidas.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public long getJanelasConcluidas() { return janelasConcluidas; }
    /* ***************************************************************
    * Metodo: getUltimoProgressoNanos
    * Funcao: Retorna ultimo progresso nanos.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public long getUltimoProgressoNanos() { return ultimoProgressoNanos; }
    /* ***************************************************************
    * Metodo: getEstadoDiagnostico
    * Funcao: Retorna estado diagnostico.
    * Parametros: nenhum
    * Retorno: texto resultante
    *************************************************************** */
    public String getEstadoDiagnostico() { return estadoDiagnostico; }
    /* ***************************************************************
    * Metodo: getMaiorEsperaReservaMs
    * Funcao: Retorna maior espera reserva ms.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public long getMaiorEsperaReservaMs() { return maiorEsperaReservaMs; }

    /* ***************************************************************
    * Classe: Janela
    * Funcao: Agrupa os destinos e o uso de portaria de uma janela.
    *************************************************************** */
    private static final class Janela {
        final List<Integer> indicesDestino;

        /* ***************************************************************
        * Metodo: Janela
        * Funcao: Agrupa os trechos percorridos ate o proximo ponto seguro.
        * Parametros: indicesDestino indices dos trechos de destino
        * Retorno: sem retorno
        *************************************************************** */
        Janela(List<Integer> indicesDestino) {
            this.indicesDestino = indicesDestino;
        }
    }
}
