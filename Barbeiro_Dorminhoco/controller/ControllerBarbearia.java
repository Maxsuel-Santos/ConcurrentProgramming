/* *********************************************************************
* Autor............: Arthur Leite Felix
* Matricula........: 202511527
* Inicio...........: 12/06/2026
* Ultima alteracao.: 20/06/2026
* Nome.............: ControllerBarbearia.java
* Funcao...........: Controlar a interface grafica da barbearia, gerenciando o posicionamento e animacoes dos elementos visuais
************************************************************************ */

package controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import model.Barbearia;

import java.util.Random;

public class ControllerBarbearia {

    @FXML private AnchorPane painelPrincipal;
    @FXML private ImageView imgBarbeiro;
    @FXML private ImageView imgBarbearia;

    private Barbearia barbearia;
    private AnchorPane painelReal;

    private final Image imgDormindo = new Image(getClass().getResource("/img/Dormindo.png").toString());
    private final Image imgCortando = new Image(getClass().getResource("/img/MrCutt.png").toString());
    private final Image imgTriste   = new Image(getClass().getResource("/img/triste.png").toString());

    private final String[] estilosCabelo = {"Azul", "Palhaco", "Loiro", "Surfista", "Moicano"};

    private final double[] esperaX = {433, 515, 593, 670, 768};
    private final double[] esperaY = {201, 201, 201, 201, 201};

    private final double atendimentoX  = 318;
    private final double atendimentoY  = 251;

    private final double portaEntradaX = 854;
    private final double portaEntradaY = 300;

    private final double pontoChecagemX = 768;
    private final double pontoChecagemY = 300;

    private final double tristeSpawnX  = 275;
    private final double tristeSpawnY  = 300;
    private final double tristeSaidaX  = 873;

    /* *********************************************************************
    * Nome.............: initialize
    * Funcao...........: Inicializa os componentes graficos basicos e define a imagem inicial do barbeiro dormindo
    ************************************************************************ */
    @FXML
    public void initialize() {
        painelReal = (painelPrincipal != null)
            ? painelPrincipal
            : (AnchorPane) imgBarbeiro.getParent();

        if (painelReal == null) {
            return;
        }

        imgBarbeiro.setImage(imgDormindo);
        imgBarbeiro.setLayoutX(275);
        imgBarbeiro.setLayoutY(217);
        imgBarbeiro.setFitWidth(191);
        imgBarbeiro.setFitHeight(218);
        imgBarbeiro.toFront();
    }

    /* *********************************************************************
    * Nome.............: setBarbearia
    * Funcao...........: Define a referencia do modelo logico da barbearia no controlador
    ************************************************************************ */
    public void setBarbearia(Barbearia barbearia) {
        this.barbearia = barbearia;
    }

    /* *********************************************************************
    * Nome.............: resetarTela
    * Funcao...........: Limpa os clientes graficos da tela e restaura o estado inicial do barbeiro
    ************************************************************************ */
    public void resetarTela() {
        Platform.runLater(() -> {
            painelReal.getChildren().removeIf(
                node -> node instanceof ImageView 
                    && node != imgBarbeiro 
                    && node != imgBarbearia
            );
            imgBarbeiro.setImage(imgDormindo);
            imgBarbeiro.setLayoutX(275);
            imgBarbeiro.setLayoutY(217);
            imgBarbeiro.setFitWidth(191);
            imgBarbeiro.setFitHeight(218);
            imgBarbeiro.toFront();
        });
    }

    /* *********************************************************************
    * Nome.............: setBarbeiroDormindo
    * Funcao...........: Altera visualmente o estado do barbeiro entre dormindo e trabalhando, ajustando dimensoes
    ************************************************************************ */
    public void setBarbeiroDormindo(boolean dormindo) {
        Platform.runLater(() -> {
            if (dormindo) {
                imgBarbeiro.setImage(imgDormindo);
                imgBarbeiro.setLayoutX(275);
                imgBarbeiro.setLayoutY(217);
                imgBarbeiro.setFitWidth(191);
                imgBarbeiro.setFitHeight(218);
            } else {
                imgBarbeiro.setImage(imgCortando);
                imgBarbeiro.setLayoutX(345);
                imgBarbeiro.setLayoutY(177);
                imgBarbeiro.setFitWidth(260);
                imgBarbeiro.setFitHeight(289);
                imgBarbeiro.toFront();
            }
        });
    }

    /* *********************************************************************
    * Nome.............: criarClienteGrafico
    * Funcao...........: Sorteia um estilo visual de cabelo, cria um ImageView na porta de entrada e adiciona ao painel
    ************************************************************************ */
    public ImageView criarClienteGrafico(String[] estiloSorteadoOut) {
        Random rand = new Random();
        String estilo = estilosCabelo[rand.nextInt(estilosCabelo.length)];
        estiloSorteadoOut[0] = estilo;

        Image imagemCliente;
        try {
            String caminho = "/img/" + estilo + ".png";
            imagemCliente = new Image(getClass().getResource(caminho).toString());
            if (imagemCliente.isError()) return null;
        } catch (Exception e) {
            return null;
        }

        ImageView novoCliente = new ImageView(imagemCliente);
        novoCliente.setFitWidth(110);
        novoCliente.setFitHeight(160);
        novoCliente.setLayoutX(portaEntradaX);
        novoCliente.setLayoutY(portaEntradaY);
        novoCliente.setScaleX(-1);

        Platform.runLater(() -> painelReal.getChildren().add(novoCliente));
        return novoCliente;
    }

    /* *********************************************************************
    * Nome.............: carregarSentado
    * Funcao...........: Tenta carregar o asset visual do cliente sentado, utilizando a versao em pe como alternativa
    ************************************************************************ */
    private Image carregarSentado(String estilo) {
        try {
            String caminho = "/img/" + estilo + "Sentado.png";
            Image img = new Image(getClass().getResource(caminho).toString());
            if (!img.isError()) return img;
        } catch (Exception ignored) {}
        try {
            return new Image(getClass().getResource("/img/" + estilo + ".png").toString());
        } catch (Exception e) {
            return null;
        }
    }

    /* *********************************************************************
    * Nome.............: moverParaEspera
    * Funcao...........: Executa a animacao em duas etapas do cliente indo ate o ponto de checagem e sentando na cadeira designada
    ************************************************************************ */
    public void moverParaEspera(ImageView cliente, int idCadeira, String estilo) {
        if (cliente == null) return;
        Image imgSentado = carregarSentado(estilo);

        Platform.runLater(() -> {
            TranslateTransition irAteChecagem = new TranslateTransition(Duration.millis(600), cliente);
            irAteChecagem.setToX(pontoChecagemX - portaEntradaX);
            irAteChecagem.setToY(pontoChecagemY - portaEntradaY);

            irAteChecagem.setOnFinished(e -> {
                if (imgSentado != null) cliente.setImage(imgSentado);
                cliente.setScaleX(1);

                TranslateTransition irParaCadeira = new TranslateTransition(Duration.millis(600), cliente);
                irParaCadeira.setToX(esperaX[idCadeira] - portaEntradaX);
                irParaCadeira.setToY(esperaY[idCadeira] - portaEntradaY);
                irParaCadeira.play();
            });

            irAteChecagem.play();
        });
    }

    /* *********************************************************************
    * Nome.............: moverParaAtendimento
    * Funcao...........: Anima o deslocamento do cliente da cadeira de espera ate a cadeira do barbeiro
    ************************************************************************ */
    public void moverParaAtendimento(ImageView cliente, String estilo) {
        if (cliente == null) return;
        Image imgSentado = carregarSentado(estilo);

        Platform.runLater(() -> {
            if (imgSentado != null) cliente.setImage(imgSentado);
            cliente.setScaleX(1);

            TranslateTransition anim = new TranslateTransition(Duration.millis(800), cliente);
            anim.setToX(atendimentoX - portaEntradaX);
            anim.setToY(atendimentoY - portaEntradaY);
            anim.play();
        });
    }

    /* *********************************************************************
    * Nome.............: clienteIrEmboraTriste
    * Funcao...........: Aplica visual triste ao cliente cortado e faz sua animacao de saida da tela antes de remove-lo
    ************************************************************************ */
    public void clienteIrEmboraTriste(ImageView cliente) {
        if (cliente == null) return;
        Platform.runLater(() -> {
            cliente.setImage(imgTriste);
            cliente.setLayoutX(tristeSpawnX);
            cliente.setLayoutY(tristeSpawnY);
            cliente.setTranslateX(0);
            cliente.setTranslateY(0);

            TranslateTransition anim = new TranslateTransition(Duration.millis(900), cliente);
            anim.setToX(tristeSaidaX - tristeSpawnX);
            anim.setToY(0);
            anim.setOnFinished(e -> painelReal.getChildren().remove(cliente));
            anim.play();
        });
    }

    /* *********************************************************************
    * Nome.............: clienteDesistir
    * Funcao...........: Faz o cliente dar meia volta instantaneamente e sair pela direita ao encontrar o local cheio
    ************************************************************************ */
    public void clienteDesistir(ImageView cliente) {
        if (cliente == null) return;
        Platform.runLater(() -> {
            TranslateTransition anim = new TranslateTransition(Duration.millis(600), cliente);
            anim.setToX(100);
            anim.setToY(0);
            anim.setOnFinished(e -> painelReal.getChildren().remove(cliente));
            anim.play();
        });
    }
}