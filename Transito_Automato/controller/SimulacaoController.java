/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 23/06/2026
* Nome.............: SimulacaoController.java
* Funcao...........: Controller da tela (fxml) Simulacao. Monta o Grid,
*                    o GerenciadorSemaforos, os 8 Percursos e os 8
*                    Carros, e dispara uma ThreadCarro para cada um.
*                    Liga os controles individuais (slider de
*                    velocidade, botao de pausa/retomada e botao de
*                    exibicao da quadra) aos respectivos Carros, e
*                    implementa o RESET geral da simulacao.
*
*                    Estimativa de geometria: cada imagem de quadra
*                    (P05_SA.png, P03_SA.png, ...) tem 723x786 pixels e
*                    representa uma malha logica de 6x6 vertices (5x5
*                    quadras). MARGEM_X/MARGEM_Y definem a borda da
*                    imagem onde a malha realmente comeca/termina; ajuste
*                    esses dois valores se os carros nao baterem
*                    exatamente com o desenho das ruas.
************************************************************************ */

package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;

import model.Carro;
import model.Grid;
import model.Percurso;
import threads.ThreadCarro;
import util.Constantes;
import util.GerenciadorSemaforos;

public class SimulacaoController implements Initializable {

    // ----------------------------------------------------------------
    // Geometria estimada da malha dentro de cada imagem 723x786.
    // Ajuste estes 4 valores para alinhar os carros ao desenho das
    // ruas nas imagens reais.
    // ----------------------------------------------------------------
    private static final double MARGEM_X = 60.0;
    private static final double MARGEM_Y = 60.0;
    private static final double LARGURA_IMAGEM = 723.0;
    private static final double ALTURA_IMAGEM = 786.0;

    // ----------------------------------------------------------------
    // Injecoes FXML: quadras (pista de cada percurso)
    // ----------------------------------------------------------------
    @FXML private ImageView quadraCarro1;
    @FXML private ImageView quadraCarro2;
    @FXML private ImageView quadraCarro3;
    @FXML private ImageView quadraCarro4;
    @FXML private ImageView quadraCarro5;
    @FXML private ImageView quadraCarro6;
    @FXML private ImageView quadraCarro7;
    @FXML private ImageView quadraCarro8;

    // ----------------------------------------------------------------
    // Injecoes FXML: sprites dos carros
    // ----------------------------------------------------------------
    @FXML private ImageView imgCarro1;
    @FXML private ImageView imgCarro2;
    @FXML private ImageView imgCarro3;
    @FXML private ImageView imgCarro4;
    @FXML private ImageView imgCarro5;
    @FXML private ImageView imgCarro6;
    @FXML private ImageView imgCarro7;
    @FXML private ImageView imgCarro8;

    // ----------------------------------------------------------------
    // Injecoes FXML: sliders de velocidade
    // ----------------------------------------------------------------
    @FXML private Slider sliderCarro1;
    @FXML private Slider sliderCarro2;
    @FXML private Slider sliderCarro3;
    @FXML private Slider sliderCarro4;
    @FXML private Slider sliderCarro5;
    @FXML private Slider sliderCarro6;
    @FXML private Slider sliderCarro7;
    @FXML private Slider sliderCarro8;

    // ----------------------------------------------------------------
    // Injecoes FXML: botoes de pausa/retomada
    // ----------------------------------------------------------------
    @FXML private Button btnPauseCar1;
    @FXML private Button btnPauseCar2;
    @FXML private Button btnPauseCar3;
    @FXML private Button btnPauseCar4;
    @FXML private Button btnPauseCar5;
    @FXML private Button btnPauseCar6;
    @FXML private Button btnPauseCar7;
    @FXML private Button btnPauseCar8;

    // ----------------------------------------------------------------
    // Injecoes FXML: botoes de exibicao da quadra
    // ----------------------------------------------------------------
    @FXML private Button btnShowRouteCar1;
    @FXML private Button btnShowRouteCar2;
    @FXML private Button btnShowRouteCar3;
    @FXML private Button btnShowRouteCar4;
    @FXML private Button btnShowRouteCar5;
    @FXML private Button btnShowRouteCar6;
    @FXML private Button btnShowRouteCar7;
    @FXML private Button btnShowRouteCar8;

    @FXML private Button btnReset;

    // ----------------------------------------------------------------
    // Estado da simulacao (recriado inteiramente a cada RESET)
    // ----------------------------------------------------------------
    private GerenciadorSemaforos gerenciadorSemaforos;
    private Grid grid;
    private Carro[] carros;          // indice 0..7 = carro 1..8
    private ThreadCarro[] threads;   // indice 0..7 = carro 1..8

    private ImageView[] imgsQuadra;
    private ImageView[] imgsCarro;
    private Slider[] sliders;
    private Button[] btnsPause;
    private Button[] btnsShowRoute;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        agruparReferenciasFXML();
        ligarBotoesEControles();
        iniciarSimulacao();
    }

    /* ***************************************************************
    * Metodo: agruparReferenciasFXML
    * Funcao: Organiza as 8 referencias individuais injetadas pelo FXML
    *         em arrays, para que o restante do codigo possa iterar
    *         de 0 a 7 em vez de repetir 8 blocos quase identicos.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void agruparReferenciasFXML() {
        imgsQuadra = new ImageView[] {
            quadraCarro1, quadraCarro2, quadraCarro3, quadraCarro4,
            quadraCarro5, quadraCarro6, quadraCarro7, quadraCarro8
        };
        imgsCarro = new ImageView[] {
            imgCarro1, imgCarro2, imgCarro3, imgCarro4,
            imgCarro5, imgCarro6, imgCarro7, imgCarro8
        };
        sliders = new Slider[] {
            sliderCarro1, sliderCarro2, sliderCarro3, sliderCarro4,
            sliderCarro5, sliderCarro6, sliderCarro7, sliderCarro8
        };
        btnsPause = new Button[] {
            btnPauseCar1, btnPauseCar2, btnPauseCar3, btnPauseCar4,
            btnPauseCar5, btnPauseCar6, btnPauseCar7, btnPauseCar8
        };
        btnsShowRoute = new Button[] {
            btnShowRouteCar1, btnShowRouteCar2, btnShowRouteCar3, btnShowRouteCar4,
            btnShowRouteCar5, btnShowRouteCar6, btnShowRouteCar7, btnShowRouteCar8
        };
    }

    /* ***************************************************************
    * Metodo: iniciarSimulacao
    * Funcao: Monta o Grid, o GerenciadorSemaforos, os 8 Percursos e os
    *         8 Carros a partir das Constantes, posiciona os sprites na
    *         tela e dispara as 8 ThreadCarro.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void iniciarSimulacao() {
        gerenciadorSemaforos = new GerenciadorSemaforos();

        double tamanhoQuadraX = (LARGURA_IMAGEM - 2 * MARGEM_X) / Constantes.TAMANHO_MALHA;
        double tamanhoQuadraY = (ALTURA_IMAGEM - 2 * MARGEM_Y) / Constantes.TAMANHO_MALHA;
        // usamos a media para manter os vertices em um grid uniforme mesmo
        // que a imagem nao seja perfeitamente quadrada
        double tamanhoQuadra = (tamanhoQuadraX + tamanhoQuadraY) / 2.0;

        grid = new Grid(gerenciadorSemaforos, MARGEM_X, MARGEM_Y, tamanhoQuadra);

        carros = new Carro[Constantes.N_CARROS];
        threads = new ThreadCarro[Constantes.N_CARROS];

        for (int i = 0; i < Constantes.N_CARROS; i++) {
            int numeroCarro = i + 1;

            Percurso percurso = new Percurso(
                grid,
                Constantes.CARRO_PERCURSO_NOME[i],
                Constantes.CARRO_SENTIDO[i],
                Constantes.CARRO_TRECHOS[i]
            );

            Carro carro = new Carro(numeroCarro, percurso);
            carros[i] = carro;

            posicionarSpriteInicial(i, carro);

            ThreadCarro threadCarro = new ThreadCarro(carro, this::atualizarPosicaoNaTela);
            threads[i] = threadCarro;
            threadCarro.start();
        }
    }

    /* ***************************************************************
    * Metodo: atualizarPosicaoNaTela
    * Funcao: Callback chamado pela ThreadCarro a cada passo. Como o
    *         JavaFX so' permite atualizar nodos da cena pela JavaFX
    *         Application Thread, o reposicionamento e' delegado a
    *         Platform.runLater.
    * Parametros: @param carro carro que se moveu
    * Retorno: sem retorno
    *************************************************************** */
    private void atualizarPosicaoNaTela(Carro carro) {
        Platform.runLater(() -> {
            int indice = carro.getNumero() - 1;
            ImageView sprite = imgsCarro[indice];
            // centraliza o sprite no ponto (x,y) do vertice atual
            sprite.setLayoutX(carro.getXAtual() - sprite.getFitWidth() / 2.0);
            sprite.setLayoutY(carro.getYAtual() - sprite.getFitHeight() / 2.0);
        });
    }

    private void posicionarSpriteInicial(int indice, Carro carro) {
        ImageView sprite = imgsCarro[indice];
        sprite.setLayoutX(carro.getXAtual() - sprite.getFitWidth() / 2.0);
        sprite.setLayoutY(carro.getYAtual() - sprite.getFitHeight() / 2.0);
    }

    /* ***************************************************************
    * Metodo: ligarBotoesEControles
    * Funcao: Associa cada Slider/Button da tela ao Carro correspondente
    *         (controles individuais de velocidade, pausa/retomada e
    *         exibicao da quadra), alem do botao geral de RESET.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void ligarBotoesEControles() {
        for (int i = 0; i < Constantes.N_CARROS; i++) {
            final int indice = i;

            sliders[i].valueProperty().addListener((obs, antigo, novo) -> {
                if (carros != null && carros[indice] != null) {
                    carros[indice].setVelocidade(novo.doubleValue());
                }
            });

            btnsPause[i].setOnAction(evento -> {
                if (carros != null && carros[indice] != null) {
                    carros[indice].alternarPausa();
                }
            });

            btnsShowRoute[i].setOnAction(evento -> {
                if (carros != null && carros[indice] != null) {
                    carros[indice].alternarVisibilidadeQuadra();
                    imgsQuadra[indice].setVisible(carros[indice].isQuadraVisivel());
                }
            });
        }

        btnReset.setOnAction(evento -> reiniciarSimulacao());
    }

    /* ***************************************************************
    * Metodo: reiniciarSimulacao
    * Funcao: Implementa o controle RESET: desativa e interrompe todas
    *         as ThreadCarro em execucao, zera todos os semaforos
    *         (garantindo que nenhum fique "preso" de uma execucao
    *         anterior) e recria toda a simulacao do zero.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void reiniciarSimulacao() {
        if (threads != null) {
            for (int i = 0; i < threads.length; i++) {
                if (carros[i] != null) {
                    carros[i].desativar();
                }
                if (threads[i] != null) {
                    threads[i].interrupt();
                }
            }
        }

        if (gerenciadorSemaforos != null) {
            gerenciadorSemaforos.reiniciarTodos();
        }

        for (int i = 0; i < Constantes.N_CARROS; i++) {
            imgsQuadra[i].setVisible(true);
            sliders[i].setValue(Constantes.VELOCIDADE_PADRAO);
        }

        iniciarSimulacao();
    }
}