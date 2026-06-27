/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 24/06/2026
* Nome.............: Carro.java
* Funcao...........: Representa o estado de um carro na simulacao: seu
*                    percurso, posicao atual no ciclo, velocidade,
*                    pausa/retomada e visibilidade da sua quadra. Esta
*                    classe NAO contem a thread de movimento (essa fica
*                    em threads.ThreadCarro) nem a animacao em si (essa
*                    fica no Controller, que e' quem pode tocar em nodos
*                    JavaFX); aqui ficam apenas os dados e os controles
*                    individuais exigidos pelo trabalho: VELOCIDADE,
*                    PAUSA/RETOMADA e EXIBICAO DA QUADRA.
*
*                    Para permitir uma animacao LINEAR (em vez de o
*                    carro "pular" de vertice a vertice), o Carro guarda
*                    tanto o ponto de PARTIDA (xOrigem,yOrigem) quanto o
*                    de CHEGADA (xDestino,yDestino) do trecho atual. O
*                    Controller interpola entre esses dois pontos ao
*                    longo da duracao do passo (ver getTempoPassoMs()).
*                    O angulo de rotacao do sprite (para o carro "virar"
*                    nas curvas) e' calculado a partir dessa mesma
*                    direcao (origem -> destino), em graus, no sistema
*                    de coordenadas do JavaFX (0 = direita, sentido
*                    horario positivo, igual ao usado por Node.setRotate).
************************************************************************ */

package model;

import util.Constantes;

public class Carro {

    private final int numero;            // 1..8
    private final Percurso percurso;
    private final int indiceCicloInicial; // ponto de partida do ciclo (usado pelo RESET)

    private volatile int indiceCicloAtual = 0;   // posicao (indice da aresta) dentro do ciclo

    // Ponto de PARTIDA do trecho atual (onde o carro estava antes deste passo)
    private volatile double xOrigem;
    private volatile double yOrigem;

    // Ponto de CHEGADA do trecho atual (para onde o carro esta' indo neste passo)
    private volatile double xAtual;
    private volatile double yAtual;

    // Angulo (em graus) da direcao origem -> destino do trecho atual,
    // pronto para uso direto em ImageView.setRotate(). 0 = apontando
    // para a direita, 90 = para baixo, 180 = para a esquerda, 270/-90 =
    // para cima (sistema de coordenadas de tela, eixo Y crescendo para
    // baixo - igual ao usado pelo JavaFX).
    private volatile double anguloAtual = 0.0;

    private volatile double velocidade = Constantes.VELOCIDADE_PADRAO;
    private volatile boolean pausado = false;
    private volatile boolean quadraVisivel = true;
    private volatile boolean ativo = true; // controla o encerramento da ThreadCarro no RESET/fechamento

    private final Object travaPausa = new Object();

    /* ***************************************************************
    * Construtor: Carro (inicio no comeco do ciclo)
    * Funcao: Cria o carro posicionado no vertice de indice 0 do seu
    *         Percurso (comportamento padrao - usado quando o carro
    *         comeca do primeiro trecho da lista, ex: Carro 1).
    *************************************************************** */
    public Carro(int numero, Percurso percurso) {
        this(numero, percurso, 0);
    }

    /* ***************************************************************
    * Construtor: Carro (inicio em ponto especifico do ciclo)
    * Funcao: Cria o carro ja' posicionado no vertice correspondente ao
    *         indice informado dentro do ciclo do seu Percurso, em vez
    *         de sempre comecar do primeiro trecho da lista. Usado, por
    *         exemplo, quando o discente decide que um carro deve
    *         "nascer" no meio do seu proprio percurso (ex: Carro 2
    *         comecando no trecho RV18 em vez do primeiro RV05 da
    *         lista).
    * Parametros: @param numero numero do carro (1..8)
    *             @param percurso ciclo fechado que o carro vai seguir
    *             @param indiceCicloInicial posicao inicial dentro do
    *             ciclo (0 = primeiro trecho da lista de Constantes)
    *************************************************************** */
    public Carro(int numero, Percurso percurso, int indiceCicloInicial) {
        this.numero = numero;
        this.percurso = percurso;
        this.indiceCicloInicial = indiceCicloInicial % percurso.getQuantidadeTrechos();
        this.indiceCicloAtual = this.indiceCicloInicial;

        Vertice inicio = percurso.getVertice(this.indiceCicloAtual);
        this.xOrigem = inicio.getX();
        this.yOrigem = inicio.getY();
        this.xAtual = inicio.getX();
        this.yAtual = inicio.getY();

        recalcularAngulo();
    }

    public int getNumero() {
        return numero;
    }

    public Percurso getPercurso() {
        return percurso;
    }

    public int getIndiceCicloAtual() {
        return indiceCicloAtual;
    }

    public double getXOrigem() {
        return xOrigem;
    }

    public double getYOrigem() {
        return yOrigem;
    }

    public double getXAtual() {
        return xAtual;
    }

    public double getYAtual() {
        return yAtual;
    }

    public double getAnguloAtual() {
        return anguloAtual;
    }

    /* ***************************************************************
    * Metodo: setPosicaoAtual
    * Funcao: Define o novo ponto de CHEGADA do carro (destino do
    *         trecho que ele esta' percorrendo agora). O ponto de
    *         PARTIDA (xOrigem,yOrigem) e' automaticamente atualizado
    *         para a posicao de chegada ANTERIOR, antes de sobrescreve-
    *         la - isso preserva o par (origem,destino) que o Controller
    *         usa para interpolar a animacao do trecho atual. O angulo
    *         de direcao e' recalculado a partir desse novo par.
    * Parametros: @param x coordenada x de chegada (pixel)
    *             @param y coordenada y de chegada (pixel)
    * Retorno: sem retorno
    *************************************************************** */
    public void setPosicaoAtual(double x, double y) {
        this.xOrigem = this.xAtual;
        this.yOrigem = this.yAtual;
        this.xAtual = x;
        this.yAtual = y;
        recalcularAngulo();
    }

    /* ***************************************************************
    * Metodo: recalcularAngulo
    * Funcao: Calcula o angulo (em graus) da direcao xOrigem,yOrigem ->
    *         xAtual,yAtual, no sistema de coordenadas do JavaFX (Y
    *         crescendo para baixo), pronto para uso em
    *         ImageView.setRotate(). Usa Math.atan2, que ja' devolve o
    *         angulo correto em qualquer um dos 4 quadrantes (incluindo
    *         os 4 casos retos: direita/baixo/esquerda/cima, que sao os
    *         unicos que ocorrem nesta malha, pois todo trecho RHxx/RVxx
    *         e' horizontal ou vertical).
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void recalcularAngulo() {
        double dx = xAtual - xOrigem;
        double dy = yAtual - yOrigem;

        if (dx == 0 && dy == 0) {
            return; // sem deslocamento (ex: posicao inicial) - mantem angulo anterior
        }

        anguloAtual = Math.toDegrees(Math.atan2(dy, dx));
    }

    /* ***************************************************************
    * Metodo: avancarUmTrecho
    * Funcao: Avanca o indice do ciclo em uma posicao (dando a volta
    *         automaticamente, pois o percurso e' circular).
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    public void avancarUmTrecho() {
        indiceCicloAtual = (indiceCicloAtual + 1) % percurso.getQuantidadeTrechos();
    }

    public Aresta getProximaAresta() {
        return percurso.getAresta(indiceCicloAtual);
    }

    public Vertice getVerticeAtual() {
        return percurso.getVertice(indiceCicloAtual);
    }

    public Vertice getVerticeDestino() {
        return percurso.getVertice(indiceCicloAtual + 1);
    }

    // -------------------------------------------------------------
    // Controle: VELOCIDADE (individual por carro)
    // -------------------------------------------------------------
    public double getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(double velocidade) {
        if (velocidade < Constantes.VELOCIDADE_MIN) {
            velocidade = Constantes.VELOCIDADE_MIN;
        }
        if (velocidade > Constantes.VELOCIDADE_MAX) {
            velocidade = Constantes.VELOCIDADE_MAX;
        }
        this.velocidade = velocidade;
    }

    /* ***************************************************************
    * Metodo: getTempoPassoMs
    * Funcao: Calcula quantos milissegundos a ThreadCarro deve aguardar
    *         entre um trecho e outro, considerando a velocidade atual
    *         (velocidade maior = passo mais rapido).
    * Parametros: nenhum
    * Retorno: @return long tempo de espera em milissegundos
    *************************************************************** */
    public long getTempoPassoMs() {
        return (long) (Constantes.PASSO_BASE_MS / velocidade);
    }

    // -------------------------------------------------------------
    // Controle: PAUSA / RETOMADA (individual por carro)
    // -------------------------------------------------------------
    public boolean isPausado() {
        return pausado;
    }

    public void pausar() {
        synchronized (travaPausa) {
            pausado = true;
        }
    }

    public void retomar() {
        synchronized (travaPausa) {
            pausado = false;
            travaPausa.notifyAll();
        }
    }

    public void alternarPausa() {
        if (pausado) {
            retomar();
        } else {
            pausar();
        }
    }

    /* ***************************************************************
    * Metodo: dormirComPausa
    * Funcao: Aguarda o tempo de passo informado, mas de forma
    *         responsiva a pausa: se o carro for pausado durante a
    *         espera, o metodo bloqueia imediatamente (sem busy-wait)
    *         ate ser retomado, e so' entao retorna. Tambem encerra
    *         a espera antecipadamente se o carro for desativado
    *         (ativo=false), permitindo que a ThreadCarro finalize.
    * Parametros: @param tempoMs tempo desejado de espera, em ms
    * Retorno: sem retorno
    * Excecoes: InterruptedException se a thread for interrompida
    *************************************************************** */
    public void dormirComPausa(long tempoMs) throws InterruptedException {
        long restante = tempoMs;

        while (restante > 0 && ativo) {
            long inicio = System.currentTimeMillis();

            synchronized (travaPausa) {
                while (pausado && ativo) {
                    travaPausa.wait();
                }
            }

            if (!ativo) {
                return;
            }

            long fatia = Math.min(restante, 50L); // dorme em fatias curtas p/ reagir rapido a pausa/reset
            Thread.sleep(fatia);

            long decorrido = System.currentTimeMillis() - inicio;
            restante -= decorrido;
        }
    }

    // -------------------------------------------------------------
    // Controle: EXIBICAO DA QUADRA (individual por carro)
    // -------------------------------------------------------------
    public boolean isQuadraVisivel() {
        return quadraVisivel;
    }

    public void setQuadraVisivel(boolean quadraVisivel) {
        this.quadraVisivel = quadraVisivel;
    }

    public void alternarVisibilidadeQuadra() {
        this.quadraVisivel = !this.quadraVisivel;
    }

    // -------------------------------------------------------------
    // Ciclo de vida (usado pelo RESET e pelo fechamento da aplicacao)
    // -------------------------------------------------------------
    public boolean isAtivo() {
        return ativo;
    }

    /* ***************************************************************
    * Metodo: desativar
    * Funcao: Sinaliza para a ThreadCarro que ela deve parar de rodar
    *         (usado no RESET, antes de recriar tudo do zero, e no
    *         fechamento da janela).
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    public void desativar() {
        ativo = false;
        synchronized (travaPausa) {
            travaPausa.notifyAll(); // libera quem estiver dormindo pausado
        }
    }

    /* ***************************************************************
    * Metodo: reiniciar
    * Funcao: Restaura o carro ao estado inicial do seu percurso, sem
    *         precisar recriar o objeto (usado pelo RESET).
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    public void reiniciar() {
        indiceCicloAtual = indiceCicloInicial;
        Vertice inicio = percurso.getVertice(indiceCicloAtual);
        xOrigem = inicio.getX();
        yOrigem = inicio.getY();
        xAtual = inicio.getX();
        yAtual = inicio.getY();
        anguloAtual = 0.0;
        velocidade = Constantes.VELOCIDADE_PADRAO;
        pausado = false;
        quadraVisivel = true;
        ativo = true;
    }
}