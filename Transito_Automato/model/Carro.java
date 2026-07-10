package model;

import util.Constantes;

/**
 * Estado de um carro.
 *
 * O carro fica parado um pouco A FRENTE do meio de um trecho, nunca sobre um
 * cruzamento. Cada movimento vai do ponto seguro do trecho atual ate o ponto
 * seguro do proximo trecho, passando pelo vertice que liga os dois.
 */
public class Carro {

    private final int numero;
    private final Percurso percurso;
    private final int indiceInicial;
    private final Object travaPausa = new Object();

    /** Indice do trecho em cujo ponto seguro o carro esta parado. */
    private volatile int indiceAtual;
    private volatile double velocidade = Constantes.VELOCIDADE_PADRAO;
    private volatile boolean pausado;
    private volatile boolean percursoVisivel;
    private volatile boolean ativo = true;

    private volatile double xOrigem;
    private volatile double yOrigem;
    private volatile double xIntersecao;
    private volatile double yIntersecao;
    private volatile double xDestino;
    private volatile double yDestino;
    private volatile double anguloPrimeiraMetade;
    private volatile double anguloSegundaMetade;

    public Carro(int numero, Percurso percurso, int indiceInicial) {
        this.numero = numero;
        this.percurso = percurso;
        this.indiceInicial = normalizar(indiceInicial);
        this.indiceAtual = this.indiceInicial;

        Vertice inicioTrecho = percurso.getVertice(indiceAtual);
        Vertice fimTrecho = percurso.getVertice(indiceAtual + 1);
        double[] parada = calcularPontoParada(inicioTrecho, fimTrecho);

        xOrigem = parada[0];
        yOrigem = parada[1];
        xIntersecao = parada[0];
        yIntersecao = parada[1];
        xDestino = parada[0];
        yDestino = parada[1];
        anguloPrimeiraMetade = angulo(
            parada[0], parada[1], fimTrecho.getX(), fimTrecho.getY()
        );
        anguloSegundaMetade = anguloPrimeiraMetade;
    }

    public int getNumero() {
        return numero;
    }

    public Percurso getPercurso() {
        return percurso;
    }

    public int getIndiceAtual() {
        return indiceAtual;
    }

    /** Trecho em cujo ponto seguro o carro se encontra. */
    public Aresta getTrechoAtual() {
        return percurso.getAresta(indiceAtual);
    }

    /** Trecho para o qual o carro ira no proximo movimento. */
    public Aresta getProximoTrecho() {
        return percurso.getAresta(indiceAtual + 1);
    }

    /**
     * Prepara um movimento em duas partes:
     * 1) ponto seguro do trecho atual -> cruzamento;
     * 2) cruzamento -> ponto seguro do proximo trecho.
     */
    public void prepararMovimento() {
        Vertice inicioAtual = percurso.getVertice(indiceAtual);
        Vertice cruzamento = percurso.getVertice(indiceAtual + 1);
        Vertice fimProximo = percurso.getVertice(indiceAtual + 2);

        double[] origem = calcularPontoParada(inicioAtual, cruzamento);
        double[] destino = calcularPontoParada(cruzamento, fimProximo);

        xOrigem = origem[0];
        yOrigem = origem[1];
        xIntersecao = cruzamento.getX();
        yIntersecao = cruzamento.getY();
        xDestino = destino[0];
        yDestino = destino[1];

        anguloPrimeiraMetade = angulo(xOrigem, yOrigem, xIntersecao, yIntersecao);
        anguloSegundaMetade = angulo(xIntersecao, yIntersecao, xDestino, yDestino);
    }

    public void avancarIndice() {
        indiceAtual = normalizar(indiceAtual + 1);
    }

    public double getXOrigem() {
        return xOrigem;
    }

    public double getYOrigem() {
        return yOrigem;
    }

    public double getXIntersecao() {
        return xIntersecao;
    }

    public double getYIntersecao() {
        return yIntersecao;
    }

    public double getXDestino() {
        return xDestino;
    }

    public double getYDestino() {
        return yDestino;
    }

    public double getAnguloPrimeiraMetade() {
        return anguloPrimeiraMetade;
    }

    public double getAnguloSegundaMetade() {
        return anguloSegundaMetade;
    }

    /** Mantido para compatibilidade com codigo antigo. */
    public double getAngulo() {
        return anguloSegundaMetade;
    }

    public long getDuracaoPassoMs() {
        return Math.max(2L, (long) (Constantes.PASSO_BASE_MS / velocidade));
    }

    public void setVelocidade(double velocidade) {
        this.velocidade = Math.max(
            Constantes.VELOCIDADE_MIN,
            Math.min(Constantes.VELOCIDADE_MAX, velocidade)
        );
    }

    public boolean isPausado() {
        return pausado;
    }

    public void alternarPausa() {
        synchronized (travaPausa) {
            pausado = !pausado;
            if (!pausado) {
                travaPausa.notifyAll();
            }
        }
    }

    public void aguardarSePausado() throws InterruptedException {
        synchronized (travaPausa) {
            while (pausado && ativo) {
                travaPausa.wait();
            }
        }
    }

    public boolean isPercursoVisivel() {
        return percursoVisivel;
    }

    public void alternarPercursoVisivel() {
        percursoVisivel = !percursoVisivel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void desativar() {
        ativo = false;
        synchronized (travaPausa) {
            travaPausa.notifyAll();
        }
    }

    /**
     * Retorna um ponto um pouco a frente do meio do trecho, no sentido do
     * movimento. Assim o carro para mais perto do proximo cruzamento sem
     * ocupar a intersecao.
     */
    private double[] calcularPontoParada(Vertice inicio, Vertice fim) {
        double meioX = (inicio.getX() + fim.getX()) / 2.0;
        double meioY = (inicio.getY() + fim.getY()) / 2.0;
        double dx = fim.getX() - inicio.getX();
        double dy = fim.getY() - inicio.getY();
        double comprimento = Math.hypot(dx, dy);

        if (comprimento == 0.0) {
            return new double[] {meioX, meioY};
        }

        double avanco = Constantes.AVANCO_PONTO_PARADA_PX;
        return new double[] {
            meioX + (dx / comprimento) * avanco,
            meioY + (dy / comprimento) * avanco
        };
    }

    private double angulo(double x1, double y1, double x2, double y2) {
        return Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
    }

    private int normalizar(int indice) {
        int tamanho = percurso.getQuantidadeTrechos();
        int resultado = indice % tamanho;
        return resultado < 0 ? resultado + tamanho : resultado;
    }
}
