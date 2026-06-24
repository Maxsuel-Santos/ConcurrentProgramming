/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 23/06/2026
* Nome.............: Carro.java
* Funcao...........: Representa o estado de um carro na simulacao: seu
*                    percurso, posicao atual no ciclo, velocidade,
*                    pausa/retomada e visibilidade da sua quadra. Esta
*                    classe NAO contem a thread de movimento (essa fica
*                    em threads.ThreadCarro); aqui ficam apenas os dados
*                    e os controles individuais exigidos pelo trabalho:
*                    VELOCIDADE, PAUSA/RETOMADA e EXIBICAO DA QUADRA.
*
*                    Os campos de posicao (xAtual, yAtual) sao lidos
*                    pela thread de UI (JavaFX Application Thread) a
*                    cada frame, por isso sao 'volatile': a ThreadCarro
*                    escreve, a UI le, sem necessidade de lock pesado
*                    para um simples valor numerico.
************************************************************************ */

package model;

import util.Constantes;

public class Carro {

    private final int numero;            // 1..8
    private final Percurso percurso;

    private volatile int indiceCicloAtual = 0;   // posicao (indice da aresta) dentro do ciclo
    private volatile double xAtual;
    private volatile double yAtual;

    private volatile double velocidade = Constantes.VELOCIDADE_PADRAO;
    private volatile boolean pausado = false;
    private volatile boolean quadraVisivel = true;
    private volatile boolean ativo = true; // controla o encerramento da ThreadCarro no RESET/fechamento

    private final Object travaPausa = new Object();

    public Carro(int numero, Percurso percurso) {
        this.numero = numero;
        this.percurso = percurso;

        Vertice inicio = percurso.getVertice(0);
        this.xAtual = inicio.getX();
        this.yAtual = inicio.getY();
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

    public double getXAtual() {
        return xAtual;
    }

    public double getYAtual() {
        return yAtual;
    }

    public void setPosicaoAtual(double x, double y) {
        this.xAtual = x;
        this.yAtual = y;
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
        indiceCicloAtual = 0;
        Vertice inicio = percurso.getVertice(0);
        xAtual = inicio.getX();
        yAtual = inicio.getY();
        velocidade = Constantes.VELOCIDADE_PADRAO;
        pausado = false;
        quadraVisivel = true;
        ativo = true;
    }
}
