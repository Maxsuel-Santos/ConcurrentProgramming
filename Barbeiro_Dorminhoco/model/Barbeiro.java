/* *********************************************************************
* Autor............: Arthur Leite Felix
* Matricula........: 202511527
* Inicio...........: 12/06/2026
* Ultima alteracao.: 16/06/2026
* Nome.............: Barbeiro.java
* Funcao...........: Executar em background o ciclo continuo de trabalho do barbeiro (dormir, acordar, cortar e liberar clientes)
************************************************************************ */

package model;

import javafx.scene.control.Slider;

public class Barbeiro extends Thread {
    private final Barbearia barbearia;
    private static final int TEMPO_CORTE_BASE_MS = 5000;

    private volatile boolean pausado = false;
    private Slider sliderVelocidade;

    /* *********************************************************************
    * Nome.............: Barbeiro (Construtor)
    * Funcao...........: Associa o barbeiro ao estabelecimento de controle estrutural e o configura sob a diretiva Daemon
    ************************************************************************ */
    public Barbeiro(Barbearia barbearia) {
        this.barbearia = barbearia;
        this.setDaemon(true);
    }

    /* *********************************************************************
    * Nome.............: setSliderVelocidade
    * Funcao...........: Atribui o componente Slider utilizado para reajustar o passo do ciclo operacional da Thread
    ************************************************************************ */
    public void setSliderVelocidade(Slider slider) {
        this.sliderVelocidade = slider;
    }

    /* *********************************************************************
    * Nome.............: setPausado
    * Funcao...........: Atualiza o gatilho booleano de pausa interna da execucao, notificando a thread para retorno imediato
    ************************************************************************ */
    public synchronized void setPausado(boolean pausado) {
        this.pausado = pausado;
        if (!pausado) notifyAll();
    }

    /* *********************************************************************
    * Nome.............: aguardarSeNecessario
    * Funcao...........: Bloqueia a thread em loop enquanto a flag de pausa permanecer assinalada como verdadeira
    ************************************************************************ */
    private synchronized void aguardarSeNecessario() throws InterruptedException {
        while (pausado) {
            wait();
        }
    }

    /* *********************************************************************
    * Nome.............: getTempoCorte
    * Funcao...........: Computa o intervalo real do corte com base no valor instantaneo extraido do Slider de controle
    ************************************************************************ */
    private long getTempoCorte() {
        if (sliderVelocidade == null) return TEMPO_CORTE_BASE_MS;
        double vel = sliderVelocidade.getValue();
        if (vel <= 0) vel = 0.5;
        return (long) (TEMPO_CORTE_BASE_MS / vel);
    }

    /* *********************************************************************
    * Nome.............: run
    * Funcao...........: Ponto de execucao concorrente que realiza repetidamente o fluxo sequencial de operacao do barbeiro
    ************************************************************************ */
    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                aguardarSeNecessario();
                barbearia.chamarProximoCliente();

                aguardarSeNecessario();
                long tempoCorte = getTempoCorte();
                Thread.sleep(tempoCorte);

                aguardarSeNecessario();
                barbearia.finalizarCorte();
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}