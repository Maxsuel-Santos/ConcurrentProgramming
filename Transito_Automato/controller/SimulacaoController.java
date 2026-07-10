package controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import model.Carro;
import model.Grid;
import model.Percurso;
import threads.ThreadCarro;
import util.Constantes;
import util.GerenciadorSemaforos;

/** Controller da etapa final com os carros 1 a 8. */
public class SimulacaoController implements Initializable {

    @FXML private ImageView quadraCarro1;
    @FXML private ImageView imgCarro1;
    @FXML private Slider sliderCarro1;
    @FXML private Button btnPauseCar1;
    @FXML private Button btnShowRouteCar1;
    @FXML private ImageView semaforoCarro1;

    @FXML private ImageView quadraCarro2;
    @FXML private ImageView imgCarro2;
    @FXML private Slider sliderCarro2;
    @FXML private Button btnPauseCar2;
    @FXML private Button btnShowRouteCar2;
    @FXML private ImageView semaforoCarro2;

    @FXML private ImageView quadraCarro3;
    @FXML private ImageView imgCarro3;
    @FXML private Slider sliderCarro3;
    @FXML private Button btnPauseCar3;
    @FXML private Button btnShowRouteCar3;
    @FXML private ImageView semaforoCarro3;

    @FXML private ImageView quadraCarro4;
    @FXML private ImageView imgCarro4;
    @FXML private Slider sliderCarro4;
    @FXML private Button btnPauseCar4;
    @FXML private Button btnShowRouteCar4;
    @FXML private ImageView semaforoCarro4;

    @FXML private ImageView quadraCarro5;
    @FXML private ImageView imgCarro5;
    @FXML private Slider sliderCarro5;
    @FXML private Button btnPauseCar5;
    @FXML private Button btnShowRouteCar5;
    @FXML private ImageView semaforoCarro5;

    @FXML private ImageView quadraCarro6;
    @FXML private ImageView imgCarro6;
    @FXML private Slider sliderCarro6;
    @FXML private Button btnPauseCar6;
    @FXML private Button btnShowRouteCar6;
    @FXML private ImageView semaforoCarro6;

    @FXML private ImageView quadraCarro7;
    @FXML private ImageView imgCarro7;
    @FXML private Slider sliderCarro7;
    @FXML private Button btnPauseCar7;
    @FXML private Button btnShowRouteCar7;
    @FXML private ImageView semaforoCarro7;

    @FXML private ImageView quadraCarro8;
    @FXML private ImageView imgCarro8;
    @FXML private Slider sliderCarro8;
    @FXML private Button btnPauseCar8;
    @FXML private Button btnShowRouteCar8;
    @FXML private ImageView semaforoCarro8;

    @FXML private Button btnReset;

    private static final double AJUSTE_ANGULO_SPRITE = 90.0;
    private static final String CLASSE_PAUSE = "btn-pause-car-pause";
    private static final String CLASSE_PLAY = "btn-pause-car-play";
    private static final String CLASSE_ROTA_ON = "btn-route-on";
    private static final String CLASSE_ROTA_OFF = "btn-route-off";

    private final List<SlotCarro> slots = new ArrayList<>();
    private GerenciadorSemaforos gerenciadorSemaforos;
    private Image imagemSemaforoLivre;
    private Image imagemSemaforoBloqueado;

    private static final class SlotCarro {
        final int numero;
        final ImageView quadra;
        final ImageView sprite;
        final Slider slider;
        final Button botaoPausa;
        final Button botaoRota;
        final ImageView semaforo;

        Carro carro;
        ThreadCarro thread;
        Timeline animacao;

        SlotCarro(int numero, ImageView quadra, ImageView sprite, Slider slider,
                  Button botaoPausa, Button botaoRota, ImageView semaforo) {
            this.numero = numero;
            this.quadra = quadra;
            this.sprite = sprite;
            this.slider = slider;
            this.botaoPausa = botaoPausa;
            this.botaoRota = botaoRota;
            this.semaforo = semaforo;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        carregarImagensSemaforo();

        slots.add(new SlotCarro(
            1, quadraCarro1, imgCarro1, sliderCarro1,
            btnPauseCar1, btnShowRouteCar1, semaforoCarro1
        ));
        slots.add(new SlotCarro(
            2, quadraCarro2, imgCarro2, sliderCarro2,
            btnPauseCar2, btnShowRouteCar2, semaforoCarro2
        ));
        slots.add(new SlotCarro(
            3, quadraCarro3, imgCarro3, sliderCarro3,
            btnPauseCar3, btnShowRouteCar3, semaforoCarro3
        ));
        slots.add(new SlotCarro(
            4, quadraCarro4, imgCarro4, sliderCarro4,
            btnPauseCar4, btnShowRouteCar4, semaforoCarro4
        ));
        slots.add(new SlotCarro(
            5, quadraCarro5, imgCarro5, sliderCarro5,
            btnPauseCar5, btnShowRouteCar5, semaforoCarro5
        ));
        slots.add(new SlotCarro(
            6, quadraCarro6, imgCarro6, sliderCarro6,
            btnPauseCar6, btnShowRouteCar6, semaforoCarro6
        ));
        slots.add(new SlotCarro(
            7, quadraCarro7, imgCarro7, sliderCarro7,
            btnPauseCar7, btnShowRouteCar7, semaforoCarro7
        ));
        slots.add(new SlotCarro(
            8, quadraCarro8, imgCarro8, sliderCarro8,
            btnPauseCar8, btnShowRouteCar8, semaforoCarro8
        ));

        configurarControles();
        btnReset.setOnAction(evento -> reiniciarSimulacao());
        iniciarSimulacao();
    }

    private void configurarControles() {
        for (SlotCarro slot : slots) {
            slot.slider.setMin(Constantes.VELOCIDADE_MIN);
            slot.slider.setMax(Constantes.VELOCIDADE_MAX);
            slot.slider.setValue(Constantes.VELOCIDADE_PADRAO);

            slot.quadra.setMouseTransparent(true);
            slot.sprite.setMouseTransparent(true);
            slot.semaforo.setMouseTransparent(true);

            slot.slider.valueProperty().addListener((obs, antigo, novo) -> {
                if (slot.carro != null) {
                    slot.carro.setVelocidade(novo.doubleValue());
                }
            });

            slot.botaoPausa.setOnAction(evento -> {
                if (slot.carro != null) {
                    slot.carro.alternarPausa();
                    atualizarBotaoPausa(slot);
                }
            });

            slot.botaoRota.setOnAction(evento -> {
                if (slot.carro != null) {
                    slot.carro.alternarPercursoVisivel();
                    slot.quadra.setVisible(slot.carro.isPercursoVisivel());
                    atualizarBotaoRota(slot);
                }
            });
        }
    }

    private void iniciarSimulacao() {
        Grid grid = new Grid(
            Constantes.ORIGEM_GRID_X,
            Constantes.ORIGEM_GRID_Y,
            Constantes.TAMANHO_QUADRA_PX
        );
        gerenciadorSemaforos = new GerenciadorSemaforos(grid);

        /* Primeiro cria e posiciona todos. Depois registra as posicoes
         * iniciais. Somente quando os oito pontos estao registrados as
         * threads sao iniciadas. */
        for (SlotCarro slot : slots) {
            int indice = slot.numero - 1;
            Percurso percurso = new Percurso(
                grid,
                Constantes.NOMES_PERCURSOS[indice],
                Constantes.SENTIDOS[indice],
                Constantes.TRECHOS_DOS_CARROS[indice]
            );

            slot.carro = new Carro(
                slot.numero,
                percurso,
                Constantes.INDICES_INICIAIS[indice]
            );
            slot.carro.setVelocidade(slot.slider.getValue());
            slot.quadra.setVisible(false);
            posicionarSprite(slot);
            atualizarBotaoPausa(slot);
            atualizarBotaoRota(slot);
            atualizarSemaforo(slot, false);
        }

        try {
            for (SlotCarro slot : slots) {
                gerenciadorSemaforos.registrarPosicaoInicial(slot.carro);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pararSimulacaoAtual();
            return;
        }

        for (SlotCarro slot : slots) {
            slot.thread = new ThreadCarro(
                slot.carro,
                gerenciadorSemaforos,
                (carro, duracaoMs) -> animarEEsperar(slot, carro, duracaoMs)
            );
            slot.thread.start();
        }
    }

    /**
     * O carro se move do ponto seguro de um trecho ao ponto seguro do proximo.
     * A Timeline possui duas metades e faz a curva no cruzamento. Como a
     * thread espera o termino real da animacao, o semaforo so e liberado
     * quando toda a imagem do carro ja alcancou um ponto seguro.
     */
    private void animarEEsperar(SlotCarro slot, Carro carro, long duracaoMs)
            throws InterruptedException {
        CountDownLatch terminou = new CountDownLatch(1);

        Platform.runLater(() -> {
            if (!carro.isAtivo() || slot.carro != carro) {
                terminou.countDown();
                return;
            }

            if (slot.animacao != null) {
                slot.animacao.stop();
            }

            double metadeLargura = metadeLarguraRenderizada(slot.sprite);
            double metadeAltura = metadeAlturaRenderizada(slot.sprite);
            double ajusteX = ajusteVisualX(slot);
            double ajusteY = ajusteVisualY(slot);

            double xOrigem = carro.getXOrigem() - metadeLargura + ajusteX;
            double yOrigem = carro.getYOrigem() - metadeAltura + ajusteY;
            double xIntersecao = carro.getXIntersecao() - metadeLargura + ajusteX;
            double yIntersecao = carro.getYIntersecao() - metadeAltura + ajusteY;
            double xDestino = carro.getXDestino() - metadeLargura + ajusteX;
            double yDestino = carro.getYDestino() - metadeAltura + ajusteY;

            slot.sprite.setLayoutX(xOrigem);
            slot.sprite.setLayoutY(yOrigem);
            slot.sprite.setRotate(
                carro.getAnguloPrimeiraMetade() + AJUSTE_ANGULO_SPRITE
            );

            double metadeDuracao = Math.max(1.0, duracaoMs / 2.0);

            KeyFrame cruzamento = new KeyFrame(
                Duration.millis(metadeDuracao),
                new KeyValue(slot.sprite.layoutXProperty(), xIntersecao, Interpolator.LINEAR),
                new KeyValue(slot.sprite.layoutYProperty(), yIntersecao, Interpolator.LINEAR),
                new KeyValue(
                    slot.sprite.rotateProperty(),
                    carro.getAnguloSegundaMetade() + AJUSTE_ANGULO_SPRITE,
                    Interpolator.DISCRETE
                )
            );

            KeyFrame pontoSeguro = new KeyFrame(
                Duration.millis(Math.max(2.0, duracaoMs)),
                new KeyValue(slot.sprite.layoutXProperty(), xDestino, Interpolator.LINEAR),
                new KeyValue(slot.sprite.layoutYProperty(), yDestino, Interpolator.LINEAR)
            );

            slot.animacao = new Timeline(cruzamento, pontoSeguro);
            slot.animacao.setOnFinished(evento -> {
                atualizarSemaforo(slot, false);
                terminou.countDown();
            });

            atualizarSemaforo(slot, true);
            slot.animacao.play();
        });

        try {
            terminou.await();
        } catch (InterruptedException e) {
            terminou.countDown();
            throw e;
        }
    }

    private void posicionarSprite(SlotCarro slot) {
        double metadeLargura = metadeLarguraRenderizada(slot.sprite);
        double metadeAltura = metadeAlturaRenderizada(slot.sprite);
        slot.sprite.setLayoutX(
            slot.carro.getXDestino() - metadeLargura + ajusteVisualX(slot)
        );
        slot.sprite.setLayoutY(
            slot.carro.getYDestino() - metadeAltura + ajusteVisualY(slot)
        );
        slot.sprite.setRotate(
            slot.carro.getAnguloPrimeiraMetade() + AJUSTE_ANGULO_SPRITE
        );
    }

    private double metadeLarguraRenderizada(ImageView sprite) {
        return sprite.getBoundsInLocal().getWidth() / 2.0;
    }

    private double metadeAlturaRenderizada(ImageView sprite) {
        return sprite.getBoundsInLocal().getHeight() / 2.0;
    }

    private double ajusteVisualX(SlotCarro slot) {
        return Constantes.AJUSTE_VISUAL_CARRO_X[slot.numero - 1];
    }

    private double ajusteVisualY(SlotCarro slot) {
        return Constantes.AJUSTE_VISUAL_CARRO_Y[slot.numero - 1];
    }

    private void reiniciarSimulacao() {
        pararSimulacaoAtual();

        for (SlotCarro slot : slots) {
            slot.slider.setValue(Constantes.VELOCIDADE_PADRAO);
            slot.quadra.setVisible(false);
            atualizarSemaforo(slot, false);
        }

        iniciarSimulacao();
    }

    private void pararSimulacaoAtual() {
        for (SlotCarro slot : slots) {
            if (slot.carro != null) {
                slot.carro.desativar();
            }
            if (slot.thread != null) {
                slot.thread.interrupt();
            }
            if (slot.animacao != null) {
                slot.animacao.stop();
                slot.animacao = null;
            }
        }

        for (SlotCarro slot : slots) {
            if (slot.thread != null) {
                try {
                    slot.thread.join(1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void carregarImagensSemaforo() {
        imagemSemaforoLivre = new Image(
            getClass().getResource(Constantes.CAMINHO_IMG + "semaforo_livre.png").toExternalForm()
        );
        imagemSemaforoBloqueado = new Image(
            getClass().getResource(Constantes.CAMINHO_IMG + "semaforo_bloqueado.png").toExternalForm()
        );
    }

    private void atualizarSemaforo(SlotCarro slot, boolean andando) {
        slot.semaforo.setImage(andando ? imagemSemaforoLivre : imagemSemaforoBloqueado);
    }

    private void atualizarBotaoPausa(SlotCarro slot) {
        String classe = slot.carro != null && slot.carro.isPausado()
            ? CLASSE_PLAY
            : CLASSE_PAUSE;
        trocarClasse(slot.botaoPausa, CLASSE_PLAY, CLASSE_PAUSE, classe);
    }

    private void atualizarBotaoRota(SlotCarro slot) {
        String classe = slot.carro != null && slot.carro.isPercursoVisivel()
            ? CLASSE_ROTA_OFF
            : CLASSE_ROTA_ON;
        trocarClasse(slot.botaoRota, CLASSE_ROTA_ON, CLASSE_ROTA_OFF, classe);
    }

    private void trocarClasse(Button botao, String classeA, String classeB, String ativa) {
        botao.getStyleClass().remove(classeA);
        botao.getStyleClass().remove(classeB);
        if (!botao.getStyleClass().contains(ativa)) {
            botao.getStyleClass().add(ativa);
        }
    }
}
