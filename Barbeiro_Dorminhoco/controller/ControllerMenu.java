/* *********************************************************************
* Autor............: Arthur Leite Felix
* Matricula........: 202511527
* Inicio...........: 19/05/2026
* Ultima alteracao.: 20/06/2026
* Nome.............: ControllerMenu.java
* Funcao...........: Controlar o painel de configuracao da simulacao, monitorando velocidades de execucao, pausando e reiniciando a aplicacao
************************************************************************ */

package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import model.Barbeiro;
import model.GeradorClientes;
import model.Barbearia;

public class ControllerMenu {

    @FXML private Slider sliderBarbeiro;
    @FXML private Slider sliderClientes;
    @FXML private Label labelVelBarbeiro;
    @FXML private Label labelVelClientes;

    @FXML private Button btnPausaBarbeiro;
    @FXML private Button btnPausaClientes;
    @FXML private Button btnReset;

    private Barbeiro barbeiro;
    private GeradorClientes geradorClientes;
    private Barbearia barbearia;
    private ControllerBarbearia controllerBarbearia;

    private boolean barbeiroPausado = false;
    private boolean clientesPausados = false;

    /* *********************************************************************
    * Nome.............: initialize
    * Funcao...........: Adiciona listeners aos controles Slider para atualizar em tempo real os multiplicadores visuais
    ************************************************************************ */
    @FXML
    public void initialize() {
        sliderBarbeiro.valueProperty().addListener((obs, oldVal, newVal) -> {
            labelVelBarbeiro.setText(String.format("%.1fx", newVal.doubleValue()));
        });

        sliderClientes.valueProperty().addListener((obs, oldVal, newVal) -> {
            labelVelClientes.setText(String.format("%.1fx", newVal.doubleValue()));
        });

        labelVelBarbeiro.setText("1.0x");
        labelVelClientes.setText("1.0x");
    }

    /* *********************************************************************
    * Nome.............: setRefs
    * Funcao...........: Mapeia as referencias cruzadas de threads e componentes e associa os sliders aos multiplicadores logicos
    ************************************************************************ */
    public void setRefs(Barbearia barbearia, Barbeiro barbeiro,
                        GeradorClientes geradorClientes,
                        ControllerBarbearia controllerBarbearia) {
        this.barbearia = barbearia;
        this.barbeiro = barbeiro;
        this.geradorClientes = geradorClientes;
        this.controllerBarbearia = controllerBarbearia;

        barbeiro.setSliderVelocidade(sliderBarbeiro);
        geradorClientes.setSliderVelocidade(sliderClientes);
    }

    /* *********************************************************************
    * Nome.............: onPausaBarbeiro
    * Funcao...........: Inverte o estado de execucao do barbeiro e altera dinamicamente o texto do botao de controle
    ************************************************************************ */
    @FXML
    private void onPausaBarbeiro() {
        barbeiroPausado = !barbeiroPausado;
        barbeiro.setPausado(barbeiroPausado);
        btnPausaBarbeiro.setText(barbeiroPausado ? "▶ Retomar Barbeiro" : "⏸ Pausar Barbeiro");
    }

    /* *********************************************************************
    * Nome.............: onPausaClientes
    * Funcao...........: Interrompe ou resume a rotina de geracao automatica de novos clientes em resposta ao clique
    ************************************************************************ */
    @FXML
    private void onPausaClientes() {
        clientesPausados = !clientesPausados;
        geradorClientes.setPausado(clientesPausados);
        btnPausaClientes.setText(clientesPausados ? "▶ Retomar Clientes" : "⏸ Pausar Clientes");
    }

    /* *********************************************************************
    * Nome.............: onReset
    * Funcao...........: Interrompe as threads, limpa a tela e reconstroi toda a logica estrutural da simulação do zero
    ************************************************************************ */
    @FXML
    private void onReset() {
        barbeiro.interrupt();
        geradorClientes.interrupt();

        controllerBarbearia.resetarTela();

        barbeiroPausado = false;
        clientesPausados = false;
        btnPausaBarbeiro.setText("⏸ Pausar Barbeiro");
        btnPausaClientes.setText("⏸ Pausar Clientes");
        sliderBarbeiro.setValue(1.0);
        sliderClientes.setValue(1.0);

        Platform.runLater(() -> {
            Barbearia novaBarbearia = new Barbearia(controllerBarbearia);
            controllerBarbearia.setBarbearia(novaBarbearia);

            Barbeiro novoBarbeiro = new Barbeiro(novaBarbearia);
            GeradorClientes novoGerador = new GeradorClientes(novaBarbearia, controllerBarbearia, sliderClientes);

            novoBarbeiro.setSliderVelocidade(sliderBarbeiro);

            this.barbeiro = novoBarbeiro;
            this.geradorClientes = novoGerador;
            this.barbearia = novaBarbearia;

            novoBarbeiro.start();
            novoGerador.start();
        });
    }

    /* *********************************************************************
    * Nome.............: getSliderClientes
    * Funcao...........: Retorna o controle do controle deslizante do gerador de clientes para vinculacao externa
    ************************************************************************ */
    public Slider getSliderClientes() { return sliderClientes; }
}