/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 24/06/2026
* Nome.............: SimulacaoController.java
* Funcao...........: Controller da tela (fxml) Simulacao.
*
*                    ETAPA ATUAL: somente o Carro 1 (P05_SA) esta'
*                    implementado de fato. Os sliders e botoes dos
*                    Carros 2 a 8 ja existem na FXML (paineis visuais
*                    completos) mas ainda nao tem Carro/Thread por tras
*                    - por isso ficam desabilitados, so' para nao travar
*                    a aplicacao com erro de referencia nula. Conforme
*                    cada carro for migrado para o novo formato de
*                    Constantes (ordem de trechos ja' na sequencia real
*                    de movimento), ele entra no controller do mesmo
*                    jeito que o Carro 1 esta' aqui.
*
*                    Geometria do percurso (REPOSICIONAR AQUI): a imagem
*                    P05_SA.png tem 723x786 pixels e representa uma
*                    malha logica de 6x6 vertices (5x5 quadras).
*                    MARGEM_X/MARGEM_Y/LARGURA_IMAGEM/ALTURA_IMAGEM, mais
*                    abaixo, definem onde a malha comeca/termina dentro
*                    da imagem - ajuste esses 4 valores para alinhar o
*                    carro ao desenho das ruas.
*
*                    Animacao: o movimento entre dois cruzamentos e'
*                    interpolado de forma LINEAR e continua por uma
*                    Timeline (em vez do carro "pular" instantaneamente
*                    de ponto a ponto). A mesma Timeline tambem gira o
*                    sprite (ImageView.setRotate) para a direcao do
*                    deslocamento, simulando o carro "virando" nas
*                    curvas. Ver animarMovimento().
************************************************************************ */

package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import model.Carro;
import model.Grid;
import model.Percurso;
import threads.ThreadCarro;
import util.Constantes;
import util.GerenciadorSemaforos;

public class SimulacaoController implements Initializable {

    // ==================================================================
    // >>> REPOSICIONAMENTO DO PERCURSO (ajuste aqui) <<<
    // ------------------------------------------------------------------
    // Geometria estimada da malha dentro da imagem 723x786 do Carro 1.
    // A malha logica e' sempre 6x6 vertices (Constantes.N_VERTICES),
    // igualmente espacados dentro do retangulo definido por estes 4
    // valores. Se o carro nao estiver andando exatamente sobre o
    // desenho das ruas em P05_SA.png, ajuste:
    //   - MARGEM_X / MARGEM_Y: deslocam toda a malha para a direita/
    //     baixo (aumente para mover a malha para dentro da imagem;
    //     diminua para mover para fora/cima-esquerda).
    //   - LARGURA_IMAGEM / ALTURA_IMAGEM: tamanho real da regiao onde a
    //     malha se encaixa (normalmente o tamanho da imagem PNG usada
    //     de fundo); mudar isso afeta o espacamento entre os vertices.
    // ==================================================================
    private static final double MARGEM_X = 22.0;
    private static final double MARGEM_Y = 13.0;
    private static final double LARGURA_IMAGEM = 767.0;
    private static final double ALTURA_IMAGEM = 710.0;

    // ----------------------------------------------------------------
    // Duracao da animacao de UM trecho (vertice a vertice). E' sempre
    // igual ao tempo de passo atual do carro (Carro.getTempoPassoMs()),
    // recalculado a cada trecho para refletir mudancas de velocidade
    // feitas durante o movimento.
    // ----------------------------------------------------------------
    private Timeline animacaoCarro1;

    // ----------------------------------------------------------------
    // >>> CALIBRACAO DA ROTACAO DO SPRITE (ajuste aqui) <<<
    // Carro.getAnguloAtual() devolve 0 graus quando o carro esta'
    // andando para a DIREITA (eixo X positivo), 90 para BAIXO, 180/-180
    // para a ESQUERDA e -90/270 para CIMA - convencao padrao de
    // ImageView.setRotate(). Se a imagem carro1.png foi desenhada
    // apontando para a direita, deixe este valor em 0. Se ela foi
    // desenhada apontando para CIMA (comum em sprites vistos de cima,
    // estilo GTA), troque para 90 (a rotacao "de cima" passa a
    // corresponder a 0 graus do angulo calculado). Ajuste em
    // incrementos de 90 ate' o carro virar para o lado certo em cada
    // trecho da pista.
    // ----------------------------------------------------------------
    private static final double AJUSTE_ANGULO_SPRITE_CARRO1 = 90.0;

    // ----------------------------------------------------------------
    // Injecoes FXML - Carro 1 (unico com Carro/Thread real nesta etapa)
    // ----------------------------------------------------------------
    @FXML private ImageView quadraCarro1;
    @FXML private ImageView imgCarro1;
    @FXML private Slider sliderCarro1;
    @FXML private Button btnPauseCar1;
    @FXML private Button btnShowRouteCar1;

    // ----------------------------------------------------------------
    // Injecoes FXML - Carros 2 a 8 (paineis ja' existem na tela, mas
    // ainda sem Carro/Thread correspondente; ficam desabilitados)
    // ----------------------------------------------------------------
    @FXML private Slider sliderCarro2;
    @FXML private Slider sliderCarro3;
    @FXML private Slider sliderCarro4;
    @FXML private Slider sliderCarro5;
    @FXML private Slider sliderCarro6;
    @FXML private Slider sliderCarro7;
    @FXML private Slider sliderCarro8;

    @FXML private Button btnPauseCar2;
    @FXML private Button btnPauseCar3;
    @FXML private Button btnPauseCar4;
    @FXML private Button btnPauseCar5;
    @FXML private Button btnPauseCar6;
    @FXML private Button btnPauseCar7;
    @FXML private Button btnPauseCar8;

    @FXML private Button btnShowRouteCar2;
    @FXML private Button btnShowRouteCar3;
    @FXML private Button btnShowRouteCar4;
    @FXML private Button btnShowRouteCar5;
    @FXML private Button btnShowRouteCar6;
    @FXML private Button btnShowRouteCar7;
    @FXML private Button btnShowRouteCar8;

    @FXML private Button btnReset;

    // ----------------------------------------------------------------
    // Estado da simulacao (recriado a cada RESET)
    // ----------------------------------------------------------------
    private GerenciadorSemaforos gerenciadorSemaforos;
    private Grid grid;
    private Carro carro1;
    private ThreadCarro threadCarro1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ligarControlesCarro1();
        ligarBotaoReset();
        iniciarSimulacao();
    }

    /* ***************************************************************
    * Metodo: iniciarSimulacao
    * Funcao: Monta o Grid, o GerenciadorSemaforos e o Percurso/Carro 1
    *         a partir das Constantes, posiciona o sprite na tela e
    *         dispara a ThreadCarro.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void iniciarSimulacao() {
        gerenciadorSemaforos = new GerenciadorSemaforos();

        double tamanhoQuadraX = (LARGURA_IMAGEM - 2 * MARGEM_X) / Constantes.TAMANHO_MALHA;
        double tamanhoQuadraY = (ALTURA_IMAGEM - 2 * MARGEM_Y) / Constantes.TAMANHO_MALHA;
        double tamanhoQuadra = (tamanhoQuadraX + tamanhoQuadraY) / 2.0;

        grid = new Grid(gerenciadorSemaforos, MARGEM_X, MARGEM_Y, tamanhoQuadra);

        // CARRO_1_TRECHOS ja' esta' na ordem real de deslocamento
        // (sentido SA validado geometricamente) - por isso usamos o
        // construtor de Percurso que NAO faz inversao.
        Percurso percurso1 = new Percurso(
            grid,
            Constantes.CARRO_PERCURSO_NOME[0],
            Constantes.CARRO_SENTIDO[0],
            Constantes.CARRO_1_TRECHOS
        );

        carro1 = new Carro(1, percurso1);

        posicionarSpriteInicial(imgCarro1, carro1);

        threadCarro1 = new ThreadCarro(carro1, this::atualizarPosicaoNaTela);
        threadCarro1.start();
    }

    /* ***************************************************************
    * Metodo: atualizarPosicaoNaTela
    * Funcao: Callback chamado pela ThreadCarro a cada passo, assim que
    *         ela decide o PROXIMO trecho (origem/destino ja' definidos
    *         no Carro). Dispara a animacao linear correspondente na
    *         JavaFX Application Thread.
    * Parametros: @param carro carro que vai se mover agora
    * Retorno: sem retorno
    *************************************************************** */
    private void atualizarPosicaoNaTela(Carro carro) {
        Platform.runLater(() -> animarMovimento(carro));
    }

    /* ***************************************************************
    * Metodo: animarMovimento
    * Funcao: Cria e dispara uma Timeline que interpola LINEARMENTE a
    *         posicao do sprite do carro entre o ponto de origem e o de
    *         destino do trecho atual (em vez de "pular" direto para o
    *         destino), e gira o sprite (setRotate) para o angulo da
    *         direcao do movimento - e' isso que faz o carro "virar"
    *         visualmente nas curvas.
    *
    *         Se uma animacao anterior ainda estiver rodando (ex: a
    *         velocidade foi alterada bem no limite entre dois trechos),
    *         ela e' parada antes de iniciar a nova, para nao haver duas
    *         Timelines concorrendo pelo mesmo sprite.
    * Parametros: @param carro carro cujo proximo trecho sera' animado
    * Retorno: sem retorno
    *************************************************************** */
    private void animarMovimento(Carro carro) {
        if (animacaoCarro1 != null) {
            animacaoCarro1.stop();
        }

        double xOrigem = carro.getXOrigem() - imgCarro1.getFitWidth() / 2.0;
        double yOrigem = carro.getYOrigem() - imgCarro1.getFitHeight() / 2.0;
        double xDestino = carro.getXAtual() - imgCarro1.getFitWidth() / 2.0;
        double yDestino = carro.getYAtual() - imgCarro1.getFitHeight() / 2.0;

        // garante que a animacao comeca exatamente do ponto de partida,
        // mesmo que o sprite esteja, por qualquer razao, fora de posicao
        imgCarro1.setLayoutX(xOrigem);
        imgCarro1.setLayoutY(yOrigem);
        imgCarro1.setRotate(carro.getAnguloAtual() + AJUSTE_ANGULO_SPRITE_CARRO1);

        Duration duracaoPasso = Duration.millis(carro.getTempoPassoMs());

        KeyValue avancoX = new KeyValue(imgCarro1.layoutXProperty(), xDestino, Interpolator.LINEAR);
        KeyValue avancoY = new KeyValue(imgCarro1.layoutYProperty(), yDestino, Interpolator.LINEAR);

        KeyFrame quadroFinal = new KeyFrame(duracaoPasso, avancoX, avancoY);

        animacaoCarro1 = new Timeline(quadroFinal);
        animacaoCarro1.play();
    }

    private void posicionarSpriteInicial(ImageView sprite, Carro carro) {
        sprite.setLayoutX(carro.getXAtual() - sprite.getFitWidth() / 2.0);
        sprite.setLayoutY(carro.getYAtual() - sprite.getFitHeight() / 2.0);
        sprite.setRotate(carro.getAnguloAtual() + AJUSTE_ANGULO_SPRITE_CARRO1);
    }

    /* ***************************************************************
    * Metodo: ligarControlesCarro1
    * Funcao: Associa o Slider de velocidade e os botoes de
    *         pausa/retomada e exibicao da quadra do Carro 1 ao objeto
    *         Carro correspondente.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void ligarControlesCarro1() {
        sliderCarro1.valueProperty().addListener((obs, antigo, novo) -> {
            if (carro1 != null) {
                carro1.setVelocidade(novo.doubleValue());
            }
        });

        btnPauseCar1.setOnAction(evento -> {
            if (carro1 != null) {
                carro1.alternarPausa();
            }
        });

        btnShowRouteCar1.setOnAction(evento -> {
            if (carro1 != null) {
                carro1.alternarVisibilidadeQuadra();
                quadraCarro1.setVisible(carro1.isQuadraVisivel());
            }
        });
    }

   

    /* ***************************************************************
    * Metodo: ligarBotaoReset
    * Funcao: Liga o botao geral de RESET a' rotina de reinicio da
    *         simulacao.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void ligarBotaoReset() {
        btnReset.setOnAction(evento -> reiniciarSimulacao());
    }

    /* ***************************************************************
    * Metodo: reiniciarSimulacao
    * Funcao: Implementa o controle RESET: desativa e interrompe a
    *         ThreadCarro em execucao, zera os semaforos (garantindo
    *         que nenhum fique "preso" de uma execucao anterior) e
    *         recria a simulacao do zero.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void reiniciarSimulacao() {
        if (carro1 != null) {
            carro1.desativar();
        }
        if (threadCarro1 != null) {
            threadCarro1.interrupt();
        }
        if (animacaoCarro1 != null) {
            animacaoCarro1.stop();
        }
        if (gerenciadorSemaforos != null) {
            gerenciadorSemaforos.reiniciarTodos();
        }

        quadraCarro1.setVisible(true);
        sliderCarro1.setValue(Constantes.VELOCIDADE_PADRAO);
        imgCarro1.setRotate(0);

        iniciarSimulacao();
    }
}