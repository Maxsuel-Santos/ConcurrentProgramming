/* *********************************************************************
* Autor............: Arthur Leite Felix
* Matricula........: 202511527
* Inicio...........: 12/06/2026
* Ultima alteracao.: 16/06/2026
* Nome.............: Cliente.java
* Funcao...........: Representar cada cliente de maneira individualizada por meio de uma thread com comportamento independente
************************************************************************ */

package model;

import controller.ControllerBarbearia;
import javafx.scene.image.ImageView;

public class Cliente extends Thread {
    private final Barbearia barbearia;
    private ImageView imageView;
    private String estiloCabelo;
    private int idCadeiraEspera = -1;

    /* *********************************************************************
    * Nome.............: Cliente (Construtor)
    * Funcao...........: Dispara a criacao visual do cliente, mapeia o estilo sorteado e associa a estrutura da barbearia
    ************************************************************************ */
    public Cliente(Barbearia barbearia, ControllerBarbearia controller) {
        this.barbearia = barbearia;
        String[] estiloOut = new String[1];
        this.imageView = controller.criarClienteGrafico(estiloOut);
        this.estiloCabelo = estiloOut[0];
    }

    /* *********************************************************************
    * Nome.............: run
    * Funcao...........: Controla a logica de ciclo de vida do cliente tentando acessar o estabelecimento e aguardando corte
    ************************************************************************ */
    @Override
    public void run() {
        try {
            Thread.sleep(300);
            boolean entrou = barbearia.entrarNaBarbearia(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* *********************************************************************
    * Nome.............: getImageView
    * Funcao...........: Retorna o componente grafico ImageView associado a representacao desse cliente na tela
    ************************************************************************ */
    public ImageView getImageView() { return imageView; }

    /* *********************************************************************
    * Nome.............: getEstiloCabelo
    * Funcao...........: Retorna a string que indica a variacao visual de cabelo sorteada para este cliente especifico
    ************************************************************************ */
    public String getEstiloCabelo() { return estiloCabelo; }

    /* *********************************************************************
    * Nome.............: getIdCadeiraEspera
    * Funcao...........: Retorna o indice numerico identificador da cadeira ocupada pelo cliente na sala de espera
    ************************************************************************ */
    public int getIdCadeiraEspera() { return idCadeiraEspera; }

    /* *********************************************************************
    * Nome.............: setIdCadeiraEspera
    * Funcao...........: Atualiza o ID do assento alocado dinamicamente no monitor para controle do fluxo de animacao
    ************************************************************************ */
    public void setIdCadeiraEspera(int id) { this.idCadeiraEspera = id; }
}