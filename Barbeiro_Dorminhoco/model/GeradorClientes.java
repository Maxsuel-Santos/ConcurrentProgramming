/* *********************************************************************
* Autor............: Arthur Leite Felix
* Matricula........: 202511527
* Inicio...........: 12/06/2026
* Ultima alteracao.: 16/06/2026
* Nome.............: GeradorClientes.java
* Funcao...........: Produzir de maneira automatica e ciclica novas instancias independentes de Threads de Clientes em tempos variaveis
************************************************************************ */

package model;

import controller.ControllerBarbearia;
import javafx.scene.control.Slider;

public class GeradorClientes extends Thread {
    private final Barbearia barbearia;
    private final ControllerBarbearia controller;
    private Slider sliderVelocidade;

    private volatile boolean pausado = false;

    /* *********************************************************************
    * Nome.............: GeradorClientes (Construtor)
    * Funcao...........: Atribui os controladores e o slider, definindo o comportamento do loop como processo Daemon background
    ************************************************************************ */
    public GeradorClientes(Barbearia barbearia, ControllerBarbearia controller, Slider sliderVelocidade) {
        this.barbearia = barbearia;
        this.controller = controller;
        this.sliderVelocidade = sliderVelocidade;
        this.setDaemon(true);
    }

    /* *********************************************************************
    * Nome.............: setSliderVelocidade
    * Funcao...........: Associa o Slider de controle de velocidade de fabricacao de clientes a classe geradora
    ************************************************************************ */
    public void setSliderVelocidade(Slider slider) {
        this.sliderVelocidade = slider;
    }

    /* *********************************************************************
    * Nome.............: setPausado
    * Funcao...........: Modifica o estado do sinalizador de pausa, desbloqueando o monitor de espera caso necessario
    ************************************************************************ */
    public synchronized void setPausado(boolean pausado) {
        this.pausado = pausado;
        if (!pausado) notifyAll();
    }

    /* *********************************************************************
    * Nome.............: aguardarSeNecessario
    * Funcao...........: Suspende a atividade do gerador em loop enquanto a variavel de verificacao de pausa for verdadeira
    ************************************************************************ */
    private synchronized void aguardarSeNecessario() throws InterruptedException {
        while (pausado) {
            wait();
        }
    }

    /* *********************************************************************
    * Nome.............: getTempoEspera
    * Funcao...........: Retorna o atraso em milissegundos ajustado proporcionalmente pela taxa de velocidade do Slider
    ************************************************************************ */
    private long getTempoEspera() {
        if (sliderVelocidade == null) return 6000;
        double vel = sliderVelocidade.getValue();
        if (vel <= 0) vel = 0.5;
        return (long) (6000 / vel);
    }

    /* *********************************************************************
    * Nome.............: run
    * Funcao...........: Laco principal que aguarda o intervalo calculado e instancia novas threads de clientes caso haja espaço livre
    ************************************************************************ */
    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                aguardarSeNecessario();

                long tempoEspera = getTempoEspera();
                Thread.sleep(tempoEspera);

                aguardarSeNecessario();

                if (!barbearia.estaCheia()) {
                    Cliente cliente = new Cliente(barbearia, controller);
                    cliente.start();
                }
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}