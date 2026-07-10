package threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Carro;
import util.GerenciadorSemaforos;

/**
 * Thread de um carro usando janelas de semaforos.
 *
 * Os carros 1 a 7 preservam o protocolo da etapa anterior. O carro 8 percorre
 * uma janela completa de P23_SA entre dois trechos privados, sem parar dentro
 * de uma zona compartilhada.
 */
public class ThreadCarro extends Thread {

    private static final int CARRO_8 = 8;

    private final Carro carro;
    private final GerenciadorSemaforos semaforos;
    private final MovimentoCarro movimento;

    private volatile String estadoDiagnostico = "INICIANDO";
    private volatile long movimentosConcluidos;
    private volatile long janelasConcluidas;
    private volatile long ultimoProgressoNanos = System.nanoTime();
    private volatile long maiorEsperaReservaMs;

    public ThreadCarro(Carro carro, GerenciadorSemaforos semaforos, MovimentoCarro movimento) {
        super("ThreadCarro-" + carro.getNumero());
        this.carro = carro;
        this.semaforos = semaforos;
        this.movimento = movimento;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (carro.isAtivo() && !isInterrupted()) {
                boolean emCorredor = semaforos.estaEmCorredorDeSentidoOposto(
                    carro, carro.getIndiceAtual()
                );
                boolean pontoPrivadoC8 = carro.getNumero() == CARRO_8
                    && semaforos.trechoPrivadoDoCarro8(carro, carro.getIndiceAtual());

                if ((carro.getNumero() != CARRO_8 && !emCorredor) || pontoPrivadoC8) {
                    estadoDiagnostico = carro.isPausado()
                        ? "PAUSADO_EM_" + carro.getTrechoAtual().getNome()
                        : "PRONTO_EM_" + carro.getTrechoAtual().getNome();
                    carro.aguardarSePausado();
                }

                if (!carro.isAtivo() || isInterrupted()) {
                    break;
                }

                Janela janela = calcularProximaJanela(emCorredor);
                reservarJanela(janela);

                for (int destino : janela.indicesDestino) {
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
                }

                boolean continuaNoCorredor = semaforos.estaEmCorredorDeSentidoOposto(
                    carro, carro.getIndiceAtual()
                );
                semaforos.finalizarJanela(carro, continuaNoCorredor);
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

    private Janela calcularProximaJanela(boolean emCorredor) {
        int quantidade = carro.getPercurso().getQuantidadeTrechos();
        int primeiro = normalizar(carro.getIndiceAtual() + 1, quantidade);

        if (carro.getNumero() == CARRO_8) {
            List<Integer> destinos = new ArrayList<>();
            int indice = primeiro;
            int limite = 0;
            while (limite++ < quantidade) {
                destinos.add(indice);
                if (semaforos.trechoPrivadoDoCarro8(carro, indice)) {
                    break;
                }
                indice = normalizar(indice + 1, quantidade);
            }
            if (destinos.isEmpty()
                    || !semaforos.trechoPrivadoDoCarro8(
                        carro, destinos.get(destinos.size() - 1))) {
                throw new IllegalStateException("C8 nao encontrou o proximo ponto privado.");
            }
            return new Janela(destinos, false);
        }

        boolean primeiroNoCorredor = semaforos.estaEmCorredorDeSentidoOposto(
            carro, primeiro
        );

        if (!emCorredor && !primeiroNoCorredor) {
            return new Janela(Collections.singletonList(primeiro), false);
        }

        List<Integer> destinos = new ArrayList<>();
        destinos.add(primeiro);
        if (primeiroNoCorredor) {
            int segundo = normalizar(primeiro + 1, quantidade);
            destinos.add(segundo);
        }
        return new Janela(destinos, true);
    }

    private void reservarJanela(Janela janela) throws InterruptedException {
        estadoDiagnostico = carro.getNumero() == CARRO_8
            ? "AGUARDANDO_JANELA_CARRO_8_" + janela.indicesDestino
            : janela.usaPortaria
                ? "AGUARDANDO_CORREDOR_" + janela.indicesDestino
                : "AGUARDANDO_RESERVA_" + janela.indicesDestino;

        long inicioEspera = System.nanoTime();
        if (janela.usaPortaria) {
            semaforos.reservarJanelaDeCorredor(carro, janela.indicesDestino);
        } else {
            semaforos.reservarJanela(carro, janela.indicesDestino);
        }
        maiorEsperaReservaMs = Math.max(
            maiorEsperaReservaMs,
            (System.nanoTime() - inicioEspera) / 1_000_000L
        );
    }

    private int normalizar(int indice, int quantidade) {
        int resultado = indice % quantidade;
        return resultado < 0 ? resultado + quantidade : resultado;
    }

    public long getMovimentosConcluidos() { return movimentosConcluidos; }
    public long getJanelasConcluidas() { return janelasConcluidas; }
    public long getUltimoProgressoNanos() { return ultimoProgressoNanos; }
    public String getEstadoDiagnostico() { return estadoDiagnostico; }
    public long getMaiorEsperaReservaMs() { return maiorEsperaReservaMs; }

    private static final class Janela {
        final List<Integer> indicesDestino;
        final boolean usaPortaria;

        Janela(List<Integer> indicesDestino, boolean usaPortaria) {
            this.indicesDestino = indicesDestino;
            this.usaPortaria = usaPortaria;
        }
    }
}
