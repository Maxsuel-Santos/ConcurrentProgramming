/* *********************************************************************
* Autor............: Arthur Leite Felix
* Matricula........: 202511527
* Inicio...........: 12/06/2026
* Ultima alteracao.: 20/06/2026
* Nome.............: Barbearia.java
* Funcao...........: Gerenciar o monitor de sincronizacao da barbearia controlando vagas, exclusao mutua e interacao cliente-barbeiro
************************************************************************ */

package model;
 
import controller.ControllerBarbearia;
import javafx.scene.image.ImageView;
 
public class Barbearia {
    private final int NUM_CADEIRAS = 5;
    private int cadeirasLivres = NUM_CADEIRAS;
    private final boolean[] cadeirasOcupadas = new boolean[NUM_CADEIRAS];
 
    private int clientesTotais = 0;
    private final int MAX_CLIENTES = 7;
 
    private final Semaforo mutex;
    private final Semaforo filaClientes;
    private final Semaforo barbeiros;
 
    private final ControllerBarbearia controller;
 
    private ImageView clienteEmAtendimento = null;
    private String estiloClienteEmAtendimento = "";
 
    /* *********************************************************************
    * Nome.............: Barbearia (Construtor)
    * Funcao...........: Aloca o controlador visual e instancia os semaforos de controle de concorrencia com seus valores iniciais
    ************************************************************************ */
    public Barbearia(ControllerBarbearia controller) {
        this.controller = controller;
        this.mutex = new Semaforo(1);
        this.filaClientes = new Semaforo(0);
        this.barbeiros = new Semaforo(0);
    }
 
    /* *********************************************************************
    * Nome.............: estaCheia
    * Funcao...........: Avalia de forma sincronizada se o limite maximo absoluto de clientes no estabelecimento foi atingido
    ************************************************************************ */
    public synchronized boolean estaCheia() {
        return clientesTotais >= MAX_CLIENTES;
    }
 
    /* *********************************************************************
    * Nome.............: entrarNaBarbearia
    * Funcao...........: Gerencia a entrada do cliente na regiao critica, alocando cadeiras livres ou comandando sua evasao imediata
    ************************************************************************ */
    public boolean entrarNaBarbearia(Cliente threadCliente) throws InterruptedException {
        mutex.Down();
 
        if (cadeirasLivres > 0) {
            cadeirasLivres--;
            clientesTotais++;
 
            int idCadeira = -1;
            for (int i = 0; i < NUM_CADEIRAS; i++) {
                if (!cadeirasOcupadas[i]) {
                    cadeirasOcupadas[i] = true;
                    idCadeira = i;
                    break;
                }
            }
            threadCliente.setIdCadeiraEspera(idCadeira);
 
            controller.moverParaEspera(threadCliente.getImageView(), idCadeira, threadCliente.getEstiloCabelo());
 
            filaClientes.Up();  
            mutex.Up();
 
            barbeiros.Down();
 
            Thread.sleep(500);
 
            mutex.Down();
            cadeirasOcupadas[threadCliente.getIdCadeiraEspera()] = false;
            cadeirasLivres++;
            this.clienteEmAtendimento = threadCliente.getImageView();
            this.estiloClienteEmAtendimento = threadCliente.getEstiloCabelo();
            mutex.Up();
 
            controller.moverParaAtendimento(clienteEmAtendimento, estiloClienteEmAtendimento);
            return true;
 
        } else {
            mutex.Up();
            controller.clienteDesistir(threadCliente.getImageView());
            return false;
        }
    }
 
    /* *********************************************************************
    * Nome.............: chamarProximoCliente
    * Funcao...........: Bloqueia a thread do barbeiro caso nao existam clientes em espera e libera o atendimento do proximo da fila
    ************************************************************************ */
    public void chamarProximoCliente() throws InterruptedException {
        if (filaClientes.getValor() == 0) {
            controller.setBarbeiroDormindo(true);
        }
 
        filaClientes.Down();
        controller.setBarbeiroDormindo(false);
        barbeiros.Up();
 
        Thread.sleep(1400);
    }
 
    /* *********************************************************************
    * Nome.............: finalizarCorte
    * Funcao...........: Libera a cadeira de atendimento, comanda graficamente a evasao do cliente atendido e decrementa contadores
    ************************************************************************ */
    public void finalizarCorte() {
        if (clienteEmAtendimento != null) {
            controller.clienteIrEmboraTriste(clienteEmAtendimento);
            clienteEmAtendimento = null;
            estiloClienteEmAtendimento = "";

            try {
                mutex.Down();
                clientesTotais--;
                mutex.Up();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
 
    /* *********************************************************************
    * Nome.............: getClientesTotais
    * Funcao...........: Retorna a quantidade consolidada atualizada de clientes sob a gerencia do monitor
    ************************************************************************ */
    public synchronized int getClientesTotais() {
        return clientesTotais;
    }
}