/* ***************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 10/07/2026
* Nome.............: Carro.java
* Funcao...........: Mantem o estado e os dados de movimento de um carro.
************************************************************************ */
package model;

import util.Constantes;

/* ***************************************************************
* Classe: Carro
* Funcao: Mantem o estado e os dados de movimento de um carro.
*************************************************************** */
public class Carro {

    private final int numero;
    private final Percurso percurso;
    private final int indiceInicial;
    private final Object travaPausa = new Object();

    // Indice do trecho em cujo ponto seguro o carro esta parado.
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

    /* ***************************************************************
    * Metodo: Carro
    * Funcao: Inicializa uma nova instancia de Carro.
    * Parametros: numero parametro numero; percurso parametro percurso; indiceInicial parametro indiceInicial
    * Retorno: sem retorno
    *************************************************************** */
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

    /* ***************************************************************
    * Metodo: getNumero
    * Funcao: Retorna numero.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public int getNumero() {
        return numero;
    }

    /* ***************************************************************
    * Metodo: getPercurso
    * Funcao: Retorna percurso.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Percurso getPercurso() {
        return percurso;
    }

    /* ***************************************************************
    * Metodo: getIndiceAtual
    * Funcao: Retorna indice atual.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public int getIndiceAtual() {
        return indiceAtual;
    }

    /* ***************************************************************
    * Metodo: getTrechoAtual
    * Funcao: Retorna trecho atual.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Aresta getTrechoAtual() {
        return percurso.getAresta(indiceAtual);
    }

    /* ***************************************************************
    * Metodo: getProximoTrecho
    * Funcao: Retorna proximo trecho.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Aresta getProximoTrecho() {
        return percurso.getAresta(indiceAtual + 1);
    }

    /* ***************************************************************
    * Metodo: prepararMovimento
    * Funcao: Calcula os pontos e angulos do proximo movimento.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
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

    /* ***************************************************************
    * Metodo: avancarIndice
    * Funcao: Executa a operacao avancar indice.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    public void avancarIndice() {
        indiceAtual = normalizar(indiceAtual + 1);
    }

    /* ***************************************************************
    * Metodo: getXOrigem
    * Funcao: Retorna xorigem.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getXOrigem() {
        return xOrigem;
    }

    /* ***************************************************************
    * Metodo: getYOrigem
    * Funcao: Retorna yorigem.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getYOrigem() {
        return yOrigem;
    }

    /* ***************************************************************
    * Metodo: getXIntersecao
    * Funcao: Retorna xintersecao.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getXIntersecao() {
        return xIntersecao;
    }

    /* ***************************************************************
    * Metodo: getYIntersecao
    * Funcao: Retorna yintersecao.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getYIntersecao() {
        return yIntersecao;
    }

    /* ***************************************************************
    * Metodo: getXDestino
    * Funcao: Retorna xdestino.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getXDestino() {
        return xDestino;
    }

    /* ***************************************************************
    * Metodo: getYDestino
    * Funcao: Retorna ydestino.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getYDestino() {
        return yDestino;
    }

    /* ***************************************************************
    * Metodo: getAnguloPrimeiraMetade
    * Funcao: Retorna angulo primeira metade.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getAnguloPrimeiraMetade() {
        return anguloPrimeiraMetade;
    }

    /* ***************************************************************
    * Metodo: getAnguloSegundaMetade
    * Funcao: Retorna angulo segunda metade.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getAnguloSegundaMetade() {
        return anguloSegundaMetade;
    }

    /* ***************************************************************
    * Metodo: getAngulo
    * Funcao: Retorna angulo.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getAngulo() {
        return anguloSegundaMetade;
    }

    /* ***************************************************************
    * Metodo: getDuracaoPassoMs
    * Funcao: Retorna duracao passo ms.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public long getDuracaoPassoMs() {
        return Math.max(2L, (long) (Constantes.PASSO_BASE_MS / velocidade));
    }

    /* ***************************************************************
    * Metodo: setVelocidade
    * Funcao: Define velocidade.
    * Parametros: velocidade parametro velocidade
    * Retorno: sem retorno
    *************************************************************** */
    public void setVelocidade(double velocidade) {
        this.velocidade = Math.max(
            Constantes.VELOCIDADE_MIN,
            Math.min(Constantes.VELOCIDADE_MAX, velocidade)
        );
    }

    /* ***************************************************************
    * Metodo: isPausado
    * Funcao: Indica pausado.
    * Parametros: nenhum
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean isPausado() {
        return pausado;
    }

    /* ***************************************************************
    * Metodo: alternarPausa
    * Funcao: Alterna pausa.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    public void alternarPausa() {
        synchronized (travaPausa) {
            pausado = !pausado;
            if (!pausado) {
                travaPausa.notifyAll();
            }
        }
    }

    /* ***************************************************************
    * Metodo: aguardarSePausado
    * Funcao: Bloqueia a thread enquanto o carro estiver pausado.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    public void aguardarSePausado() throws InterruptedException {
        synchronized (travaPausa) {
            while (pausado && ativo) {
                travaPausa.wait();
            }
        }
    }

    /* ***************************************************************
    * Metodo: isPercursoVisivel
    * Funcao: Indica percurso visivel.
    * Parametros: nenhum
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean isPercursoVisivel() {
        return percursoVisivel;
    }

    /* ***************************************************************
    * Metodo: alternarPercursoVisivel
    * Funcao: Alterna percurso visivel.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    public void alternarPercursoVisivel() {
        percursoVisivel = !percursoVisivel;
    }

    /* ***************************************************************
    * Metodo: isAtivo
    * Funcao: Indica ativo.
    * Parametros: nenhum
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean isAtivo() {
        return ativo;
    }

    /* ***************************************************************
    * Metodo: desativar
    * Funcao: Executa a operacao desativar.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    public void desativar() {
        ativo = false;
        synchronized (travaPausa) {
            travaPausa.notifyAll();
        }
    }

    /* ***************************************************************
    * Metodo: calcularPontoParada
    * Funcao: Calcula ponto parada.
    * Parametros: inicio parametro inicio; fim parametro fim
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
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

    /* ***************************************************************
    * Metodo: angulo
    * Funcao: Executa a operacao angulo.
    * Parametros: x1 parametro x1; y1 parametro y1; x2 parametro x2; y2 parametro y2
    * Retorno: valor calculado
    *************************************************************** */
    private double angulo(double x1, double y1, double x2, double y2) {
        return Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
    }

    /* ***************************************************************
    * Metodo: normalizar
    * Funcao: Normaliza .
    * Parametros: indice parametro indice
    * Retorno: valor calculado
    *************************************************************** */
    private int normalizar(int indice) {
        int tamanho = percurso.getQuantidadeTrechos();
        int resultado = indice % tamanho;
        return resultado < 0 ? resultado + tamanho : resultado;
    }
}
