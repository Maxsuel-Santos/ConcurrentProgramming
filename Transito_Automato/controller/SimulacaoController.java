/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 24/06/2026
* Nome.............: SimulacaoController.java
* Funcao...........: Controller da tela (fxml) Simulacao.
*
*                    ETAPA ATUAL: Carro 1 (P05_SA), Carro 2 (P03_SA) e
*                    Carro 3 (P07_SH) estao implementados de fato,
*                    incluindo todos os trechos compartilhados entre
*                    eles (regioes criticas/semaforos). Os sliders e
*                    botoes dos Carros 4 a 8 ja existem na FXML
*                    (paineis visuais completos) mas ainda nao tem
*                    Carro/Thread por tras - por isso ficam
*                    desabilitados, so' para nao travar a aplicacao com
*                    erro de referencia nula. Conforme cada carro for
*                    migrado para o novo formato de Constantes (ordem
*                    de trechos ja' na sequencia real de movimento),
*                    basta adiciona-lo ao array DEFINICOES (e mapear
*                    seus campos @FXML em montarSlots()) - todo o resto
*                    (animacao, rotacao, pausa, velocidade, exibicao de
*                    quadra, RESET) e' generico e funciona
*                    automaticamente para qualquer carro presente na
*                    lista.
*
*                    Geometria do percurso (REPOSICIONAR AQUI): cada
*                    carro tem sua PROPRIA imagem de fundo (quadra), e
*                    cada imagem pode ter sua propria malha calibrada
*                    independentemente (MARGEM_X/MARGEM_Y/LARGURA_
*                    IMAGEM/ALTURA_IMAGEM dentro de cada DefinicaoCarro,
*                    mais abaixo). Ajuste os 4 valores do carro
*                    correspondente para alinhar o sprite ao desenho das
*                    ruas daquela quadra especifica.
*
*                    Posicionamento no ciclo: Constantes.
*                    CARRO_INDICE_CICLO_INICIAL define em qual trecho de
*                    CARRO_x_TRECHOS cada carro comeca a se mover (0 =
*                    primeiro trecho da lista). O Carro 2 comeca no
*                    trecho RV18 (indice 16); o Carro 3 comeca no
*                    trecho RH12 (indice 10).
*
*                    SINCRONIZACAO: a partir desta versao, as regioes
*                    criticas sao protegidas por ZONA (sequencia
*                    contigua de 1+ trechos sempre usada pelo mesmo
*                    conjunto de carros), nao mais por trecho individual
*                    - ver util.Constantes (CARRO_x_ZONAS) e
*                    threads.ThreadCarro. Essa mudanca elimina o
*                    deadlock que ocorria com 3+ carros se cruzando na
*                    versao anterior (1 semaforo por trecho).
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
import java.util.ArrayList;
import java.util.List;
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

    /* ***************************************************************
    * Classe interna: DefinicaoCarro
    * Funcao: Agrupa, para um unico carro, tudo que e' fixo/conhecido
    *         de antemao: numero, geometria da malha dentro da SUA
    *         imagem de quadra, e o ajuste de angulo do seu sprite.
    *         Existe uma instancia por carro ATIVO nesta etapa (ver
    *         array DEFINICOES, mais abaixo).
    *************************************************************** */
    private static class DefinicaoCarro {
        final int numero; // 1..8

        // >>> REPOSICIONAMENTO DO PERCURSO (ajuste aqui) <<<
        // Geometria estimada da malha dentro da imagem de quadra deste
        // carro especifico. A malha logica e' sempre 6x6 vertices
        // (Constantes.N_VERTICES), igualmente espacados dentro do
        // retangulo definido por estes 4 valores.
        //   - margemX / margemY: deslocam toda a malha para a direita/
        //     baixo (aumente para mover a malha para dentro da imagem;
        //     diminua para mover para fora/cima-esquerda).
        //   - larguraImagem / alturaImagem: tamanho real da regiao onde
        //     a malha se encaixa; mudar isso afeta o espacamento entre
        //     os vertices.
        final double margemX;
        final double margemY;
        final double larguraImagem;
        final double alturaImagem;

        // >>> CALIBRACAO DA ROTACAO DO SPRITE (ajuste aqui) <<<
        // Carro.getAnguloAtual() devolve 0 graus quando o carro esta'
        // andando para a DIREITA (eixo X positivo), 90 para BAIXO,
        // 180/-180 para a ESQUERDA e -90/270 para CIMA (convencao de
        // ImageView.setRotate()). Se a imagem carroN.png foi desenhada
        // apontando para a direita, deixe este valor em 0. Se foi
        // desenhada apontando para CIMA (comum em sprites vistos de
        // cima, estilo GTA), use 90. Ajuste em incrementos de 90 ate' o
        // carro virar para o lado certo em cada trecho da pista.
        final double ajusteAnguloSprite;

        DefinicaoCarro(int numero, double margemX, double margemY,
                       double larguraImagem, double alturaImagem,
                       double ajusteAnguloSprite) {
            this.numero = numero;
            this.margemX = margemX;
            this.margemY = margemY;
            this.larguraImagem = larguraImagem;
            this.alturaImagem = alturaImagem;
            this.ajusteAnguloSprite = ajusteAnguloSprite;
        }
    }

    // ==================================================================
    // >>> CARROS ATIVOS NESTA ETAPA (adicione novos carros aqui) <<<
    // Cada carro listado aqui tera' Carro/Thread/animacao reais
    // criados em iniciarSimulacao(). Os campos @FXML correspondentes
    // (quadraCarroN, imgCarroN, sliderCarroN, btnPauseCarN,
    // btnShowRouteCarN) precisam existir na Simulacao.fxml.
    // ==================================================================
    private static final DefinicaoCarro[] DEFINICOES = {
        // numero, margemX, margemY, larguraImagem, alturaImagem, ajusteAnguloSprite
        new DefinicaoCarro(1, 22.0, 13.0, 767.0, 710.0, 90.0),
        new DefinicaoCarro(2, 20.0, 13.0, 767.0, 710.0, 90.0),
        new DefinicaoCarro(3, 20.0, 13.0, 735.0, 735.0, 90.0),
    };

    // ----------------------------------------------------------------
    // Injecoes FXML - Carro 1
    // ----------------------------------------------------------------
    @FXML private ImageView quadraCarro1;
    @FXML private ImageView imgCarro1;
    @FXML private Slider sliderCarro1;
    @FXML private Button btnPauseCar1;
    @FXML private Button btnShowRouteCar1;

    // ----------------------------------------------------------------
    // Injecoes FXML - Carro 2
    // ----------------------------------------------------------------
    @FXML private ImageView quadraCarro2;
    @FXML private ImageView imgCarro2;
    @FXML private Slider sliderCarro2;
    @FXML private Button btnPauseCar2;
    @FXML private Button btnShowRouteCar2;

    // ----------------------------------------------------------------
    // Injecoes FXML - Carro 3
    // ----------------------------------------------------------------
    @FXML private ImageView quadraCarro3;
    @FXML private ImageView imgCarro3;
    @FXML private Slider sliderCarro3;
    @FXML private Button btnPauseCar3;
    @FXML private Button btnShowRouteCar3;

    // ----------------------------------------------------------------
    // Injecoes FXML - Carros 4 a 8 (paineis ja' existem na tela, mas
    // ainda sem Carro/Thread correspondente; ficam desabilitados)
    // ----------------------------------------------------------------
    @FXML private Slider sliderCarro4;
    @FXML private Slider sliderCarro5;
    @FXML private Slider sliderCarro6;
    @FXML private Slider sliderCarro7;
    @FXML private Slider sliderCarro8;

    @FXML private Button btnPauseCar4;
    @FXML private Button btnPauseCar5;
    @FXML private Button btnPauseCar6;
    @FXML private Button btnPauseCar7;
    @FXML private Button btnPauseCar8;

    @FXML private Button btnShowRouteCar4;
    @FXML private Button btnShowRouteCar5;
    @FXML private Button btnShowRouteCar6;
    @FXML private Button btnShowRouteCar7;
    @FXML private Button btnShowRouteCar8;

    @FXML private Button btnReset;

    /* ***************************************************************
    * Classe interna: SlotCarro
    * Funcao: Agrupa, em tempo de execucao, o Carro/ThreadCarro/Timeline
    *         de UM carro ativo, junto com as referencias aos seus nodos
    *         JavaFX (sprite, quadra, slider, botoes) e a DefinicaoCarro
    *         correspondente. E' a unidade que iniciarSimulacao() cria
    *         para cada entrada de DEFINICOES, e que os metodos
    *         genericos de animacao/controle abaixo manipulam.
    *************************************************************** */
    private static class SlotCarro {
        final DefinicaoCarro definicao;
        final ImageView quadra;
        final ImageView sprite;
        final Slider slider;
        final Button btnPause;
        final Button btnShowRoute;

        Carro carro;
        ThreadCarro thread;
        Timeline animacao;

        SlotCarro(DefinicaoCarro definicao, ImageView quadra, ImageView sprite,
                  Slider slider, Button btnPause, Button btnShowRoute) {
            this.definicao = definicao;
            this.quadra = quadra;
            this.sprite = sprite;
            this.slider = slider;
            this.btnPause = btnPause;
            this.btnShowRoute = btnShowRoute;
        }
    }

    // ----------------------------------------------------------------
    // Estado da simulacao (recriado a cada RESET)
    // ----------------------------------------------------------------
    private GerenciadorSemaforos gerenciadorSemaforos;
    private List<SlotCarro> slots;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        montarSlots();
        ligarControlesDosCarrosAtivos();
        desabilitarControlesDosCarrosInativos();
        ligarBotaoReset();
        iniciarSimulacao();
    }

    /* ***************************************************************
    * Metodo: montarSlots
    * Funcao: Cria um SlotCarro para cada DefinicaoCarro em DEFINICOES,
    *         associando-o aos campos @FXML correspondentes ao numero
    *         do carro (quadraCarroN, imgCarroN, sliderCarroN, etc).
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void montarSlots() {
        slots = new ArrayList<>();

        for (DefinicaoCarro def : DEFINICOES) {
            ImageView quadra;
            ImageView sprite;
            Slider slider;
            Button btnPause;
            Button btnShowRoute;

            switch (def.numero) {
                case 1:
                    quadra = quadraCarro1;
                    sprite = imgCarro1;
                    slider = sliderCarro1;
                    btnPause = btnPauseCar1;
                    btnShowRoute = btnShowRouteCar1;
                    break;
                case 2:
                    quadra = quadraCarro2;
                    sprite = imgCarro2;
                    slider = sliderCarro2;
                    btnPause = btnPauseCar2;
                    btnShowRoute = btnShowRouteCar2;
                    break;
                case 3:
                    quadra = quadraCarro3;
                    sprite = imgCarro3;
                    slider = sliderCarro3;
                    btnPause = btnPauseCar3;
                    btnShowRoute = btnShowRouteCar3;
                    break;
                default:
                    throw new IllegalStateException(
                        "Carro " + def.numero + " esta em DEFINICOES mas nao tem "
                        + "campos @FXML mapeados em montarSlots() - adicione um "
                        + "novo 'case' quando esse carro for implementado."
                    );
            }

            slots.add(new SlotCarro(def, quadra, sprite, slider, btnPause, btnShowRoute));
        }
    }

    /* ***************************************************************
    * Metodo: iniciarSimulacao
    * Funcao: Monta o GerenciadorSemaforos (uma unica vez, compartilhado
    *         por todos os carros) e, para cada SlotCarro, monta o seu
    *         proprio Grid (com a geometria especifica daquele carro),
    *         o Percurso a partir das Constantes, o Carro (iniciando no
    *         indice de ciclo configurado) e a respectiva ThreadCarro.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void iniciarSimulacao() {
        gerenciadorSemaforos = new GerenciadorSemaforos();

        for (SlotCarro slot : slots) {
            int idx = slot.definicao.numero - 1; // indice 0-based nos arrays de Constantes

            double tamanhoQuadraX = (slot.definicao.larguraImagem - 2 * slot.definicao.margemX)
                / Constantes.TAMANHO_MALHA;
            double tamanhoQuadraY = (slot.definicao.alturaImagem - 2 * slot.definicao.margemY)
                / Constantes.TAMANHO_MALHA;
            double tamanhoQuadra = (tamanhoQuadraX + tamanhoQuadraY) / 2.0;

            // Cada carro tem sua propria imagem de fundo, logo seu
            // proprio Grid (mesma malha logica 6x6, mas calibrada para
            // a geometria daquela imagem especifica). O Grid agora e'
            // so' geometria - nao recebe mais o GerenciadorSemaforos,
            // pois o semaforo passou a ser por ZONA (ver Percurso),
            // nao por trecho/Aresta individual.
            Grid gridDoCarro = new Grid(
                slot.definicao.margemX,
                slot.definicao.margemY,
                tamanhoQuadra
            );

            // CARRO_x_TRECHOS ja' esta' na ordem real de deslocamento
            // (sentido SA/SH validado geometricamente). O Percurso usa
            // o numero do carro para encaixar as 57 RCs do arquivo de
            // regioes criticas e marcar os pontos de entrada/saida.
            Percurso percurso = new Percurso(
                gridDoCarro,
                Constantes.CARRO_PERCURSO_NOME[idx],
                Constantes.CARRO_SENTIDO[idx],
                Constantes.CARRO_TRECHOS[idx],
                slot.definicao.numero
            );

            int indiceInicial = Constantes.CARRO_INDICE_CICLO_INICIAL[idx];

            slot.carro = new Carro(slot.definicao.numero, percurso, indiceInicial);

            posicionarSpriteInicial(slot);

            // A ThreadCarro agora recebe o GerenciadorSemaforos (unico,
            // compartilhado por todos os carros) para poder adquirir/
            // liberar o semaforo da ZONA inteira ao entrar/saiir dela.
            slot.thread = new ThreadCarro(slot.carro, gerenciadorSemaforos, c -> atualizarPosicaoNaTela(slot));
            slot.thread.start();
        }
    }

    /* ***************************************************************
    * Metodo: atualizarPosicaoNaTela
    * Funcao: Callback chamado pela ThreadCarro do slot a cada passo,
    *         assim que ela decide o PROXIMO trecho (origem/destino ja'
    *         definidos no Carro). Dispara a animacao linear
    *         correspondente na JavaFX Application Thread.
    * Parametros: @param slot slot do carro que vai se mover agora
    * Retorno: sem retorno
    *************************************************************** */
    private void atualizarPosicaoNaTela(SlotCarro slot) {
        Platform.runLater(() -> animarMovimento(slot));
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
    *         Se uma animacao anterior do MESMO carro ainda estiver
    *         rodando (ex: a velocidade foi alterada bem no limite entre
    *         dois trechos), ela e' parada antes de iniciar a nova, para
    *         nao haver duas Timelines concorrendo pelo mesmo sprite.
    * Parametros: @param slot slot do carro cujo proximo trecho sera'
    *             animado
    * Retorno: sem retorno
    *************************************************************** */
    private void animarMovimento(SlotCarro slot) {
        if (slot.animacao != null) {
            slot.animacao.stop();
        }

        Carro carro = slot.carro;
        ImageView sprite = slot.sprite;

        double xOrigem = carro.getXOrigem() - sprite.getFitWidth() / 2.0;
        double yOrigem = carro.getYOrigem() - sprite.getFitHeight() / 2.0;
        double xDestino = carro.getXAtual() - sprite.getFitWidth() / 2.0;
        double yDestino = carro.getYAtual() - sprite.getFitHeight() / 2.0;

        // garante que a animacao comeca exatamente do ponto de partida,
        // mesmo que o sprite esteja, por qualquer razao, fora de posicao
        sprite.setLayoutX(xOrigem);
        sprite.setLayoutY(yOrigem);
        sprite.setRotate(carro.getAnguloAtual() + slot.definicao.ajusteAnguloSprite);

        Duration duracaoPasso = Duration.millis(carro.getTempoPassoMs());

        KeyValue avancoX = new KeyValue(sprite.layoutXProperty(), xDestino, Interpolator.LINEAR);
        KeyValue avancoY = new KeyValue(sprite.layoutYProperty(), yDestino, Interpolator.LINEAR);

        KeyFrame quadroFinal = new KeyFrame(duracaoPasso, avancoX, avancoY);

        slot.animacao = new Timeline(quadroFinal);
        slot.animacao.play();
    }

    private void posicionarSpriteInicial(SlotCarro slot) {
        Carro carro = slot.carro;
        ImageView sprite = slot.sprite;

        sprite.setLayoutX(carro.getXAtual() - sprite.getFitWidth() / 2.0);
        sprite.setLayoutY(carro.getYAtual() - sprite.getFitHeight() / 2.0);
        sprite.setRotate(carro.getAnguloAtual() + slot.definicao.ajusteAnguloSprite);
    }

    /* ***************************************************************
    * Metodo: ligarControlesDosCarrosAtivos
    * Funcao: Associa o Slider de velocidade e os botoes de
    *         pausa/retomada e exibicao da quadra de CADA carro ativo
    *         (slots) ao respectivo objeto Carro.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void ligarControlesDosCarrosAtivos() {
        for (SlotCarro slot : slots) {
            slot.slider.valueProperty().addListener((obs, antigo, novo) -> {
                if (slot.carro != null) {
                    slot.carro.setVelocidade(novo.doubleValue());
                }
            });

            slot.btnPause.setOnAction(evento -> {
                if (slot.carro != null) {
                    slot.carro.alternarPausa();
                }
            });

            slot.btnShowRoute.setOnAction(evento -> {
                if (slot.carro != null) {
                    slot.carro.alternarVisibilidadeQuadra();
                    slot.quadra.setVisible(slot.carro.isQuadraVisivel());
                }
            });
        }
    }

    /* ***************************************************************
    * Metodo: desabilitarControlesDosCarrosInativos
    * Funcao: Os paineis dos carros que ainda nao estao em DEFINICOES
    *         ja existem na tela, mas ainda nao tem Carro/Thread por
    *         tras nesta etapa. Sao desabilitados para deixar a
    *         interface coerente, sem lancar excecao de referencia nula
    *         e sem dar a falsa impressao de que ja estao funcionando.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void desabilitarControlesDosCarrosInativos() {
        Slider[] sliders = { sliderCarro4,
            sliderCarro5, sliderCarro6, sliderCarro7, sliderCarro8 };
        Button[] botoesPause = { btnPauseCar4,
            btnPauseCar5, btnPauseCar6, btnPauseCar7, btnPauseCar8 };
        Button[] botoesShowRoute = { btnShowRouteCar4,
            btnShowRouteCar5, btnShowRouteCar6, btnShowRouteCar7, btnShowRouteCar8 };

        for (Slider s : sliders) {
            if (s != null) {
                s.setDisable(true);
            }
        }
        for (Button b : botoesPause) {
            if (b != null) {
                b.setDisable(true);
            }
        }
        for (Button b : botoesShowRoute) {
            if (b != null) {
                b.setDisable(true);
            }
        }
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
    *         ThreadCarro de CADA carro ativo, para suas Timelines de
    *         animacao, zera os semaforos (garantindo que nenhum fique
    *         "preso" de uma execucao anterior) e recria toda a
    *         simulacao do zero.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void reiniciarSimulacao() {
        for (SlotCarro slot : slots) {
            if (slot.carro != null) {
                slot.carro.desativar();
            }
            if (slot.thread != null) {
                slot.thread.interrupt();
            }
            if (slot.animacao != null) {
                slot.animacao.stop();
            }

            slot.quadra.setVisible(true);
            slot.slider.setValue(Constantes.VELOCIDADE_PADRAO);
            slot.sprite.setRotate(0);
        }

        if (gerenciadorSemaforos != null) {
            gerenciadorSemaforos.reiniciarTodos();
        }

        iniciarSimulacao();
    }
}
