/* ***************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 26/07/2026
* Ultima alteracao.: 12/07/2026
* Nome.............: GerenciadorSemaforos.java
* Funcao...........: Controla as reservas das zonas compartilhadas pelos carros.
************************************************************************ */
package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import model.Aresta;
import model.Carro;
import model.Grid;
import model.Percurso;
import model.Vertice;

/* ***************************************************************
* Classe: GerenciadorSemaforos
* Funcao: Controla as reservas das zonas compartilhadas pelos carros.
*************************************************************** */
public class GerenciadorSemaforos {

    private static final int CARRO_8 = 8;
    private static final String PREFIXO_SEGMENTO = "SEG-";
    private static final String PREFIXO_CRUZAMENTO = "CRUZ-";
    private static final String PREFIXO_CORREDOR = "COR-";

    private final Set<String> zonasCriticas;
    private final Set<String> zonasConflitoCarro8;
    private final Set<String> segmentosOpostosTodos;
    private final Map<Integer, Set<String>> segmentosInseguros;
    private final Map<Integer, Map<String, Set<String>>> portariasPorSegmento;
    private final Set<String> portariasCorredores;

    private final Map<String, Semaphore> semaforos = new LinkedHashMap<>();
    private final Semaphore portariaReservas = new Semaphore(1, true);
    private final Map<Integer, LinkedHashSet<String>> reservasPorCarro = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, List<String>> pedidosPendentes = new LinkedHashMap<>();
    private final Map<Integer, Carro> carrosRegistrados = new LinkedHashMap<>();
    private final Map<Integer, Integer> indicesEstaveis = new LinkedHashMap<>();
    private final Map<Integer, Integer> destinosPlanejados = new LinkedHashMap<>();

    private volatile long reservasConcedidas;
    private volatile long tentativasNegadas;

    /* ***************************************************************
    * Metodo: GerenciadorSemaforos
    * Funcao: Inicializa uma nova instancia de GerenciadorSemaforos.
    * Parametros: grid parametro grid
    * Retorno: sem retorno
    *************************************************************** */
    public GerenciadorSemaforos(Grid grid) {
        Percurso[] todos = criarPercursos(grid, Constantes.N_CARROS);

        this.zonasCriticas = Collections.unmodifiableSet(calcularZonasCriticas(todos));
        this.zonasConflitoCarro8 = Collections.unmodifiableSet(
            calcularZonasConflitoComCarro8(todos)
        );
        this.segmentosInseguros = calcularSegmentosInseguros(todos);
        this.portariasPorSegmento = calcularPortariasPorSegmento(todos);

        Set<String> todasPortarias = new LinkedHashSet<>();
        for (Map<String, Set<String>> porSegmento : portariasPorSegmento.values()) {
            for (Set<String> portarias : porSegmento.values()) {
                todasPortarias.addAll(portarias);
            }
        }
        this.portariasCorredores = Collections.unmodifiableSet(todasPortarias);

        Set<String> uniao = new LinkedHashSet<>();
        for (Set<String> segmentos : segmentosInseguros.values()) {
            uniao.addAll(segmentos);
        }
        this.segmentosOpostosTodos = Collections.unmodifiableSet(uniao);

        for (String zona : zonasCriticas) {
            semaforos.put(zona, new Semaphore(1, true));
        }
        for (String portaria : portariasCorredores) {
            semaforos.put(portaria, new Semaphore(1, true));
        }
    }

    /* ***************************************************************
    * Metodo: iniciarVolta
    * Funcao: Inicia volta.
    * Parametros: carro parametro carro
    * Retorno: sem retorno
    *************************************************************** */
    public void iniciarVolta(Carro carro) throws InterruptedException {
        // Nenhuma acao.
    }

    /* ***************************************************************
    * Metodo: finalizarVolta
    * Funcao: Finaliza volta.
    * Parametros: carro parametro carro
    * Retorno: sem retorno
    *************************************************************** */
    public void finalizarVolta(Carro carro) {
        // Nenhuma acao.
    }

    /* ***************************************************************
    * Metodo: registrarMovimentoConcluido
    * Funcao: Registra movimento concluido.
    * Parametros: carro parametro carro
    * Retorno: sem retorno
    *************************************************************** */
    public void registrarMovimentoConcluido(Carro carro) {
        portariaReservas.acquireUninterruptibly();
        try {
            indicesEstaveis.put(carro.getNumero(), carro.getIndiceAtual());
            destinosPlanejados.remove(carro.getNumero());
        } finally {
            portariaReservas.release();
        }
    }

    /* ***************************************************************
    * Metodo: registrarPosicaoInicial
    * Funcao: Reserva o segmento fisico onde o carro inicia parado.
    * Parametros: carro parametro carro
    * Retorno: sem retorno
    *************************************************************** */
    public void registrarPosicaoInicial(Carro carro) throws InterruptedException {
        portariaReservas.acquire();
        try {
            carrosRegistrados.put(carro.getNumero(), carro);
            indicesEstaveis.put(carro.getNumero(), carro.getIndiceAtual());
            destinosPlanejados.remove(carro.getNumero());
        } finally {
            portariaReservas.release();
        }
        atualizarReservaAteConseguir(
            carro.getNumero(), zonasDaParada(carro.getTrechoAtual())
        );
    }

    /* ***************************************************************
    * Metodo: reservarJanela
    * Funcao: Reserva atomicamente somente as zonas fisicas da janela.
    * Parametros: carro parametro carro; indicesDestino parametro indicesDestino
    * Retorno: sem retorno
    *************************************************************** */
    public void reservarJanela(Carro carro, List<Integer> indicesDestino)
            throws InterruptedException {
        if (indicesDestino == null || indicesDestino.isEmpty()) {
            return;
        }
        atualizarMovimentoAteConseguir(
            carro,
            indicesDestino.get(0),
            zonasDaJanela(carro, Collections.singletonList(indicesDestino.get(0)))
        );
    }

    /* ***************************************************************
    * Metodo: reservarJanelaDeCorredor
    * Funcao: Mantido por compatibilidade; usa a reserva local da janela.
    * Parametros: carro parametro carro; indicesDestino parametro indicesDestino
    * Retorno: sem retorno
    *************************************************************** */
    public void reservarJanelaDeCorredor(Carro carro, List<Integer> indicesDestino)
            throws InterruptedException {
        if (indicesDestino == null || indicesDestino.isEmpty()) {
            return;
        }
        List<String> desejadas = new ArrayList<>(
            zonasDaJanela(carro, Collections.singletonList(indicesDestino.get(0)))
        );
        desejadas.addAll(portariasDaJanela(carro, indicesDestino));
        atualizarMovimentoAteConseguir(carro, indicesDestino.get(0), desejadas);
    }

    /* ***************************************************************
    * Metodo: avancarNaJanela
    * Funcao: Reserva apenas o proximo movimento e conserva as portarias locais.
    * Parametros: carro carro movimentado; indiceDestino proximo trecho
    * Retorno: sem retorno
    *************************************************************** */
    public void avancarNaJanela(Carro carro, int indiceDestino)
            throws InterruptedException {
        List<String> desejadas = new ArrayList<>(
            zonasDaJanela(carro, Collections.singletonList(indiceDestino))
        );
        desejadas.addAll(portariasReservadas(carro.getNumero()));
        atualizarMovimentoAteConseguir(carro, indiceDestino, desejadas);
    }

    /* ***************************************************************
    * Metodo: tentarReservarJanela
    * Funcao: Tenta reservar atomicamente as zonas fisicas da janela.
    * Parametros: carro parametro carro; indicesDestino parametro indicesDestino
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean tentarReservarJanela(Carro carro, List<Integer> indicesDestino)
            throws InterruptedException {
        if (indicesDestino == null || indicesDestino.isEmpty()) {
            return true;
        }
        return tentarAtualizarMovimento(
            carro,
            indicesDestino.get(0),
            zonasDaJanela(carro, Collections.singletonList(indicesDestino.get(0)))
        );
    }

    /* ***************************************************************
    * Metodo: finalizarJanela
    * Funcao: Libera a janela e mantem apenas o segmento onde o carro parou.
    * Parametros: carro parametro carro; manterPortaria parametro ignorado
    * Retorno: sem retorno
    *************************************************************** */
    public void finalizarJanela(Carro carro, boolean manterPortaria) {
        atualizarSomenteLiberando(
            carro.getNumero(), zonasDaParada(carro.getTrechoAtual())
        );
    }

    /* ***************************************************************
    * Metodo: consolidarParadaIntermediaria
    * Funcao: Libera o trecho e o cruzamento ja ultrapassados, mantendo
    *         apenas o trecho atual e as portarias locais do corredor.
    * Parametros: carro carro que concluiu um movimento
    * Retorno: sem retorno
    *************************************************************** */
    public void consolidarParadaIntermediaria(Carro carro) {
        List<String> desejadas = new ArrayList<>(
            zonasDaParada(carro.getTrechoAtual())
        );
        desejadas.addAll(portariasReservadas(carro.getNumero()));
        atualizarSomenteLiberando(carro.getNumero(), desejadas);
    }

    /* ***************************************************************
    * Metodo: liberarCarro
    * Funcao: Libera todas as zonas ainda reservadas pelo carro.
    * Parametros: numeroCarro parametro numeroCarro
    * Retorno: sem retorno
    *************************************************************** */
    public void liberarCarro(int numeroCarro) {
        portariaReservas.acquireUninterruptibly();
        try {
            LinkedHashSet<String> atuais = reservasPorCarro.remove(numeroCarro);
            pedidosPendentes.remove(numeroCarro);
            carrosRegistrados.remove(numeroCarro);
            indicesEstaveis.remove(numeroCarro);
            destinosPlanejados.remove(numeroCarro);
            if (atuais != null) {
                liberar(new ArrayList<>(atuais));
            }
        } finally {
            portariaReservas.release();
        }
    }

    /* ***************************************************************
    * Metodo: paradaSegura
    * Funcao: Executa a operacao parada segura.
    * Parametros: carro parametro carro; indiceTrecho parametro indiceTrecho
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean paradaSegura(Carro carro, int indiceTrecho) {
        return !estaEmCorredorDeSentidoOposto(carro, indiceTrecho);
    }

    /* ***************************************************************
    * Metodo: estaEmCorredorDeSentidoOposto
    * Funcao: Executa a operacao esta em corredor de sentido oposto.
    * Parametros: carro parametro carro; indiceTrecho parametro indiceTrecho
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean estaEmCorredorDeSentidoOposto(Carro carro, int indiceTrecho) {
        String zona = nomeSegmento(carro.getPercurso().getAresta(indiceTrecho));
        Set<String> inseguros = segmentosInseguros.get(carro.getNumero());
        return inseguros != null && inseguros.contains(zona);
    }

    /* ***************************************************************
    * Metodo: trechoPrivadoDoCarro8
    * Funcao: Executa a operacao trecho privado do carro8.
    * Parametros: carro parametro carro; indiceTrecho parametro indiceTrecho
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean trechoPrivadoDoCarro8(Carro carro, int indiceTrecho) {
        if (carro.getNumero() != CARRO_8) {
            return false;
        }
        String zona = nomeSegmento(carro.getPercurso().getAresta(indiceTrecho));
        return !zonasConflitoCarro8.contains(zona);
    }

    /* ***************************************************************
    * Metodo: zonasDoMeioTrecho
    * Funcao: Executa a operacao zonas do meio trecho.
    * Parametros: trecho parametro trecho
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public List<String> zonasDoMeioTrecho(Aresta trecho) {
        String zona = nomeSegmento(trecho);
        if (!zonasCriticas.contains(zona)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(zona);
    }

    /* ***************************************************************
    * Metodo: zonasDaTravessia
    * Funcao: Executa a operacao zonas da travessia.
    * Parametros: percurso parametro percurso; indiceAtual parametro indiceAtual
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public List<String> zonasDaTravessia(Percurso percurso, int indiceAtual) {
        List<String> zonas = new ArrayList<>();
        adicionarSeCritica(zonas, nomeSegmento(percurso.getAresta(indiceAtual)), zonasCriticas);
        adicionarSeCritica(zonas, nomeCruzamento(percurso.getVertice(indiceAtual + 1)), zonasCriticas);
        adicionarSeCritica(zonas, nomeSegmento(percurso.getAresta(indiceAtual + 1)), zonasCriticas);
        return ordenarSemRepetir(zonas);
    }

    /* ***************************************************************
    * Metodo: getZonasCriticas
    * Funcao: Retorna zonas criticas.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Set<String> getZonasCriticas() { return zonasCriticas; }

    /* ***************************************************************
    * Metodo: getSegmentosCriticos
    * Funcao: Retorna segmentos criticos.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Set<String> getSegmentosCriticos() {
        Set<String> resultado = new LinkedHashSet<>();
        for (String zona : zonasCriticas) {
            if (zona.startsWith(PREFIXO_SEGMENTO)) {
                resultado.add(zona);
            }
        }
        return Collections.unmodifiableSet(resultado);
    }

    /* ***************************************************************
    * Metodo: getCruzamentosCriticos
    * Funcao: Retorna cruzamentos criticos.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Set<String> getCruzamentosCriticos() {
        Set<String> resultado = new LinkedHashSet<>();
        for (String zona : zonasCriticas) {
            if (zona.startsWith(PREFIXO_CRUZAMENTO)) {
                resultado.add(zona);
            }
        }
        return Collections.unmodifiableSet(resultado);
    }

    /* ***************************************************************
    * Metodo: getSegmentosSentidoOposto
    * Funcao: Retorna segmentos sentido oposto.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Set<String> getSegmentosSentidoOposto() { return segmentosOpostosTodos; }

    /* ***************************************************************
    * Metodo: getPortariasCorredores
    * Funcao: Retorna as portarias locais usadas nos corredores opostos.
    * Parametros: nenhum
    * Retorno: conjunto imutavel com os nomes das portarias
    *************************************************************** */
    public Set<String> getPortariasCorredores() { return portariasCorredores; }
    /* ***************************************************************
    * Metodo: getZonasConflitoCarro8
    * Funcao: Retorna zonas conflito carro8.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Set<String> getZonasConflitoCarro8() { return zonasConflitoCarro8; }

    /* ***************************************************************
    * Metodo: todosLivres
    * Funcao: Indica se todos os semaforos e reservas estao livres.
    * Parametros: nenhum
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean todosLivres() {
        portariaReservas.acquireUninterruptibly();
        try {
            if (!reservasPorCarro.isEmpty() || !pedidosPendentes.isEmpty()
                    || !carrosRegistrados.isEmpty() || !indicesEstaveis.isEmpty()
                    || !destinosPlanejados.isEmpty()) {
                return false;
            }
            for (Semaphore semaforo : semaforos.values()) {
                if (semaforo.availablePermits() != 1) {
                    return false;
                }
            }
            return true;
        } finally {
            portariaReservas.release();
        }
    }

    /* ***************************************************************
    * Metodo: getReservasConcedidas
    * Funcao: Retorna reservas concedidas.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public long getReservasConcedidas() { return reservasConcedidas; }
    /* ***************************************************************
    * Metodo: getTentativasNegadas
    * Funcao: Retorna tentativas negadas.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public long getTentativasNegadas() { return tentativasNegadas; }

    /* ***************************************************************
    * Metodo: descreverEstado
    * Funcao: Gera uma descricao textual do estado das reservas.
    * Parametros: nenhum
    * Retorno: texto resultante
    *************************************************************** */
    public String descreverEstado() {
        boolean adquiriu = false;
        try {
            adquiriu = portariaReservas.tryAcquire(200L, TimeUnit.MILLISECONDS);
            if (!adquiriu) {
                return "portariaReservas=OCUPADA; estado interno temporariamente indisponivel"
                    + " reservasConcedidas=" + reservasConcedidas
                    + " tentativasNegadas=" + tentativasNegadas;
            }
            return "reservas=" + reservasPorCarro
                + " pendentes=" + pedidosPendentes
                + " estaveis=" + indicesEstaveis
                + " planejados=" + destinosPlanejados;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "diagnostico interrompido; reservasConcedidas=" + reservasConcedidas
                + " tentativasNegadas=" + tentativasNegadas;
        } finally {
            if (adquiriu) {
                portariaReservas.release();
            }
        }
    }

    /* ***************************************************************
    * Metodo: atualizarReservaAteConseguir
    * Funcao: Atualiza reserva ate conseguir.
    * Parametros: carro parametro carro; desejadas parametro desejadas
    * Retorno: sem retorno
    *************************************************************** */
    private void atualizarMovimentoAteConseguir(
            Carro carro, int indiceDestino, List<String> desejadas)
            throws InterruptedException {
        long espera = 1L;
        while (!tentarAtualizarMovimento(carro, indiceDestino, desejadas)) {
            Thread.sleep(espera);
            if (espera < 10L) {
                espera++;
            }
        }
    }

    private boolean tentarAtualizarMovimento(
            Carro carro, int indiceDestino, List<String> desejadas)
            throws InterruptedException {
        portariaReservas.acquire();
        try {
            if (!estadoSeguroAposMovimento(carro, indiceDestino, desejadas)) {
                pedidosPendentes.put(
                    carro.getNumero(), ordenarSemRepetir(desejadas)
                );
                tentativasNegadas++;
                return false;
            }
            boolean concedida = tentarAtualizarReservaComPortariaAdquirida(
                carro.getNumero(), desejadas
            );
            if (concedida) {
                destinosPlanejados.put(carro.getNumero(), indiceDestino);
            }
            return concedida;
        } finally {
            portariaReservas.release();
        }
    }

    private void atualizarReservaAteConseguir(int carro, List<String> desejadas)
            throws InterruptedException {
        long espera = 1L;
        while (!tentarAtualizarReserva(carro, desejadas)) {
            Thread.sleep(espera);
            if (espera < 10L) {
                espera++;
            }
        }
    }

    /* ***************************************************************
    * Metodo: tentarAtualizarReserva
    * Funcao: Executa a operacao tentar atualizar reserva.
    * Parametros: carro parametro carro; desejadas parametro desejadas
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    private boolean tentarAtualizarReserva(int carro, List<String> desejadas)
            throws InterruptedException {
        portariaReservas.acquire();
        try {
            return tentarAtualizarReservaComPortariaAdquirida(carro, desejadas);
        } finally {
            portariaReservas.release();
        }
    }

    private boolean tentarAtualizarReservaComPortariaAdquirida(
            int carro, List<String> desejadas) {
        List<String> ordenadas = ordenarSemRepetir(desejadas);
        LinkedHashSet<String> atuais = reservasPorCarro.get(carro);
        if (atuais == null) {
            atuais = new LinkedHashSet<>();
        }

        pedidosPendentes.put(carro, ordenadas);

        if (existePedidoAnteriorPronto(carro, ordenadas)) {
            tentativasNegadas++;
            return false;
        }

        List<String> novas = diferenca(ordenadas, atuais);
        List<String> abandonar = diferenca(
            new ArrayList<>(atuais), new LinkedHashSet<>(ordenadas)
        );

        List<String> adquiridas = new ArrayList<>();
        for (String zona : novas) {
            Semaphore semaforo = semaforos.get(zona);
            if (semaforo == null) {
                continue;
            }
            if (!semaforo.tryAcquire()) {
                liberar(adquiridas);
                tentativasNegadas++;
                return false;
            }
            adquiridas.add(zona);
        }

        liberar(abandonar);
        if (ordenadas.isEmpty()) {
            reservasPorCarro.remove(carro);
        } else {
            reservasPorCarro.put(carro, new LinkedHashSet<>(ordenadas));
        }
        pedidosPendentes.remove(carro);
        reservasConcedidas++;
        return true;
    }

    /* ***************************************************************
    * Metodo: atualizarSomenteLiberando
    * Funcao: Atualiza somente liberando.
    * Parametros: carro parametro carro; desejadas parametro desejadas
    * Retorno: sem retorno
    *************************************************************** */
    private void atualizarSomenteLiberando(int carro, List<String> desejadas) {
        portariaReservas.acquireUninterruptibly();
        try {
            List<String> ordenadas = ordenarSemRepetir(desejadas);
            LinkedHashSet<String> atuais = reservasPorCarro.get(carro);
            if (atuais == null) {
                if (!ordenadas.isEmpty()) {
                    throw new IllegalStateException(
                        "Carro " + carro + " terminou sem possuir " + ordenadas
                    );
                }
                return;
            }

            for (String zona : ordenadas) {
                if (!atuais.contains(zona)) {
                    throw new IllegalStateException(
                        "Carro " + carro + " nao reservou a zona final " + zona
                    );
                }
            }

            List<String> abandonar = diferenca(
                new ArrayList<>(atuais), new LinkedHashSet<>(ordenadas)
            );
            liberar(abandonar);
            if (ordenadas.isEmpty()) {
                reservasPorCarro.remove(carro);
            } else {
                reservasPorCarro.put(carro, new LinkedHashSet<>(ordenadas));
            }
            pedidosPendentes.remove(carro);
        } finally {
            portariaReservas.release();
        }
    }

    /* ***************************************************************
    * Metodo: zonasDaParada
    * Funcao: Executa a operacao zonas da parada base.
    * Parametros: trecho parametro trecho
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private boolean estadoSeguroAposMovimento(
            Carro solicitante, int indiceDestino, List<String> desejadasSolicitante) {
        Map<Integer, Integer> posicoes = new LinkedHashMap<>();
        for (Map.Entry<Integer, Carro> entrada : carrosRegistrados.entrySet()) {
            int numero = entrada.getKey();
            Integer planejado = destinosPlanejados.get(numero);
            Integer estavel = indicesEstaveis.get(numero);
            posicoes.put(numero, planejado != null
                ? planejado : (estavel != null ? estavel : entrada.getValue().getIndiceAtual()));
        }
        posicoes.put(solicitante.getNumero(), indiceDestino);

        Map<String, Integer> ocupantes = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entrada : posicoes.entrySet()) {
            Carro carro = carrosRegistrados.get(entrada.getKey());
            if (carro == null) {
                continue;
            }
            String segmento = nomeSegmento(
                carro.getPercurso().getAresta(entrada.getValue())
            );
            if (zonasCriticas.contains(segmento)) {
                ocupantes.put(segmento, entrada.getKey());
            }
        }

        for (Map.Entry<Integer, LinkedHashSet<String>> entrada : reservasPorCarro.entrySet()) {
            Integer posicao = posicoes.get(entrada.getKey());
            Carro carro = carrosRegistrados.get(entrada.getKey());
            if (posicao == null || carro == null || paradaSegura(carro, posicao)) {
                continue;
            }
            for (String zona : entrada.getValue()) {
                if (zona.startsWith(PREFIXO_CORREDOR)) {
                    ocupantes.put(zona, entrada.getKey());
                }
            }
        }
        if (!paradaSegura(solicitante, indiceDestino)) {
            for (String zona : desejadasSolicitante) {
                if (zona.startsWith(PREFIXO_CORREDOR)) {
                    ocupantes.put(zona, solicitante.getNumero());
                }
            }
        }

        Map<Integer, Set<Integer>> esperas = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entrada : posicoes.entrySet()) {
            int numero = entrada.getKey();
            Carro carro = carrosRegistrados.get(numero);
            if (carro == null) {
                continue;
            }
            Set<Integer> bloqueadores = new LinkedHashSet<>();
            for (String zona : recursosDoProximoMovimento(carro, entrada.getValue())) {
                Integer ocupante = ocupantes.get(zona);
                if (ocupante != null && ocupante != numero) {
                    bloqueadores.add(ocupante);
                }
            }
            esperas.put(numero, bloqueadores);
        }
        return !possuiCicloDeEspera(esperas);
    }

    private List<String> recursosDoProximoMovimento(Carro carro, int indiceAtual) {
        Percurso percurso = carro.getPercurso();
        int quantidade = percurso.getQuantidadeTrechos();
        int proximo = normalizarIndice(indiceAtual + 1, quantidade);
        List<String> recursos = new ArrayList<>();
        adicionarSeGerenciada(recursos, nomeSegmento(percurso.getAresta(proximo)));

        if (paradaSegura(carro, indiceAtual)
                && !paradaSegura(carro, proximo)) {
            List<Integer> janela = indicesAteParadaSegura(carro, indiceAtual);
            recursos.addAll(portariasDaJanela(carro, indiceAtual, janela));
        } else if (!paradaSegura(carro, indiceAtual)) {
            LinkedHashSet<String> atuais = reservasPorCarro.get(carro.getNumero());
            if (atuais != null) {
                for (String zona : atuais) {
                    if (zona.startsWith(PREFIXO_CORREDOR)) {
                        recursos.add(zona);
                    }
                }
            }
        }
        return ordenarSemRepetir(recursos);
    }

    private List<Integer> indicesAteParadaSegura(Carro carro, int indiceAtual) {
        int quantidade = carro.getPercurso().getQuantidadeTrechos();
        List<Integer> resultado = new ArrayList<>();
        int indice = normalizarIndice(indiceAtual + 1, quantidade);
        for (int passos = 0; passos < quantidade; passos++) {
            resultado.add(indice);
            if (paradaSegura(carro, indice)) {
                break;
            }
            indice = normalizarIndice(indice + 1, quantidade);
        }
        return resultado;
    }

    private boolean possuiCicloDeEspera(Map<Integer, Set<Integer>> esperas) {
        Set<Integer> visitados = new HashSet<>();
        Set<Integer> pilha = new HashSet<>();
        for (Integer carro : esperas.keySet()) {
            if (visitarEspera(carro, esperas, visitados, pilha)) {
                return true;
            }
        }
        return false;
    }

    private boolean visitarEspera(
            Integer carro,
            Map<Integer, Set<Integer>> esperas,
            Set<Integer> visitados,
            Set<Integer> pilha) {
        if (pilha.contains(carro)) {
            return true;
        }
        if (!visitados.add(carro)) {
            return false;
        }
        pilha.add(carro);
        Set<Integer> proximos = esperas.get(carro);
        if (proximos != null) {
            for (Integer proximo : proximos) {
                if (visitarEspera(proximo, esperas, visitados, pilha)) {
                    return true;
                }
            }
        }
        pilha.remove(carro);
        return false;
    }

    private int normalizarIndice(int indice, int quantidade) {
        int resultado = indice % quantidade;
        return resultado < 0 ? resultado + quantidade : resultado;
    }

    private List<String> portariasReservadas(int carro) {
        portariaReservas.acquireUninterruptibly();
        try {
            LinkedHashSet<String> atuais = reservasPorCarro.get(carro);
            List<String> resultado = new ArrayList<>();
            if (atuais != null) {
                for (String zona : atuais) {
                    if (zona.startsWith(PREFIXO_CORREDOR)) {
                        resultado.add(zona);
                    }
                }
            }
            return resultado;
        } finally {
            portariaReservas.release();
        }
    }

    private List<String> portariasDaJanela(Carro carro, List<Integer> indicesDestino) {
        return portariasDaJanela(carro, carro.getIndiceAtual(), indicesDestino);
    }

    private List<String> portariasDaJanela(
            Carro carro, int indiceAtual, List<Integer> indicesDestino) {
        Set<String> resultado = new LinkedHashSet<>();
        Map<String, Set<String>> porSegmento = portariasPorSegmento.get(carro.getNumero());
        if (porSegmento == null) {
            return Collections.emptyList();
        }

        String atual = nomeSegmento(carro.getPercurso().getAresta(indiceAtual));
        Set<String> portariasAtuais = porSegmento.get(atual);
        if (portariasAtuais != null) {
            resultado.addAll(portariasAtuais);
        }
        for (int indice : indicesDestino) {
            String segmento = nomeSegmento(carro.getPercurso().getAresta(indice));
            Set<String> portarias = porSegmento.get(segmento);
            if (portarias != null) {
                resultado.addAll(portarias);
            }
        }
        List<String> ordenadas = new ArrayList<>(resultado);
        Collections.sort(ordenadas);
        return ordenadas;
    }

    private Map<Integer, Map<String, Set<String>>> calcularPortariasPorSegmento(
            Percurso[] percursos) {
        Map<Integer, Map<String, Set<String>>> resultado = new LinkedHashMap<>();
        for (int i = 0; i < percursos.length; i++) {
            resultado.put(i + 1, new LinkedHashMap<>());
        }

        for (int i = 0; i < percursos.length; i++) {
            for (int j = i + 1; j < percursos.length; j++) {
                Set<String> opostos = segmentosOpostosDoPar(percursos[i], percursos[j]);
                List<Set<String>> componentes = componentesConexos(opostos, percursos[i]);
                int numeroComponente = 1;
                for (Set<String> componente : componentes) {
                    String portaria = PREFIXO_CORREDOR + "C" + (i + 1)
                        + "-C" + (j + 1) + "-" + numeroComponente++;
                    registrarPortaria(resultado.get(i + 1), componente, portaria);
                    registrarPortaria(resultado.get(j + 1), componente, portaria);
                }
            }
        }
        return resultado;
    }

    private Set<String> segmentosOpostosDoPar(Percurso a, Percurso b) {
        Map<String, String> direcoesA = direcoesDoPercurso(a);
        Map<String, String> direcoesB = direcoesDoPercurso(b);
        Set<String> resultado = new LinkedHashSet<>();
        for (Map.Entry<String, String> entrada : direcoesA.entrySet()) {
            String direcaoB = direcoesB.get(entrada.getKey());
            if (direcaoB != null && !direcaoB.equals(entrada.getValue())) {
                resultado.add(entrada.getKey());
            }
        }
        return resultado;
    }

    private Map<String, String> direcoesDoPercurso(Percurso percurso) {
        Map<String, String> resultado = new LinkedHashMap<>();
        for (int i = 0; i < percurso.getQuantidadeTrechos(); i++) {
            String segmento = nomeSegmento(percurso.getAresta(i));
            resultado.put(segmento,
                percurso.getVertice(i).chave() + ">" + percurso.getVertice(i + 1).chave());
        }
        return resultado;
    }

    private List<Set<String>> componentesConexos(Set<String> segmentos, Percurso referencia) {
        List<Set<String>> resultado = new ArrayList<>();
        Set<String> restantes = new LinkedHashSet<>(segmentos);
        while (!restantes.isEmpty()) {
            String primeiro = restantes.iterator().next();
            restantes.remove(primeiro);
            Set<String> componente = new LinkedHashSet<>();
            List<String> fila = new ArrayList<>();
            fila.add(primeiro);
            for (int posicao = 0; posicao < fila.size(); posicao++) {
                String atual = fila.get(posicao);
                componente.add(atual);
                List<String> adicionar = new ArrayList<>();
                for (String candidato : restantes) {
                    if (segmentosAdjacentes(atual, candidato, referencia)) {
                        adicionar.add(candidato);
                    }
                }
                restantes.removeAll(adicionar);
                fila.addAll(adicionar);
            }
            resultado.add(componente);
        }
        return resultado;
    }

    private boolean segmentosAdjacentes(String nomeA, String nomeB, Percurso referencia) {
        Aresta a = encontrarAresta(nomeA, referencia);
        Aresta b = encontrarAresta(nomeB, referencia);
        return a.getOrigem().equals(b.getOrigem())
            || a.getOrigem().equals(b.getDestino())
            || a.getDestino().equals(b.getOrigem())
            || a.getDestino().equals(b.getDestino());
    }

    private Aresta encontrarAresta(String nomeZona, Percurso referencia) {
        String nome = nomeZona.startsWith(PREFIXO_SEGMENTO)
            ? nomeZona.substring(PREFIXO_SEGMENTO.length()) : nomeZona;
        for (int i = 0; i < referencia.getQuantidadeTrechos(); i++) {
            Aresta aresta = referencia.getAresta(i);
            if (aresta.getNome().equals(nome)) {
                return aresta;
            }
        }
        throw new IllegalArgumentException("Trecho ausente no percurso: " + nomeZona);
    }

    private void registrarPortaria(Map<String, Set<String>> mapa,
            Set<String> segmentos, String portaria) {
        for (String segmento : segmentos) {
            mapa.computeIfAbsent(segmento, chave -> new LinkedHashSet<>()).add(portaria);
        }
    }

    private List<String> zonasDaParada(Aresta trecho) {
        List<String> zonas = new ArrayList<>();
        String zona = nomeSegmento(trecho);
        if (zonasCriticas.contains(zona)) {
            zonas.add(zona);
        }
        return zonas;
    }

    /* ***************************************************************
    * Metodo: zonasDaJanela
    * Funcao: Executa a operacao zonas da janela base.
    * Parametros: carro parametro carro; indicesDestino parametro indicesDestino
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private List<String> zonasDaJanela(Carro carro, List<Integer> indicesDestino) {
        List<String> zonas = new ArrayList<>();
        Percurso percurso = carro.getPercurso();
        int atual = carro.getIndiceAtual();

        adicionarSeGerenciada(zonas, nomeSegmento(percurso.getAresta(atual)));
        for (int destino : indicesDestino) {
            adicionarSeGerenciada(zonas, nomeCruzamento(percurso.getVertice(atual + 1)));
            adicionarSeGerenciada(zonas, nomeSegmento(percurso.getAresta(destino)));
            atual = destino;
        }
        return ordenarSemRepetir(zonas);
    }

    /* ***************************************************************
    * Metodo: zonasDaJanelaCompleta
    * Funcao: Executa a operacao zonas da janela completa.
    * Parametros: carro parametro carro; indicesDestino parametro indicesDestino
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private List<String> zonasDaJanelaCompleta(Carro carro, List<Integer> indicesDestino) {
        List<String> zonas = new ArrayList<>();
        Percurso percurso = carro.getPercurso();
        int atual = carro.getIndiceAtual();
        adicionarSeCritica(zonas, nomeSegmento(percurso.getAresta(atual)), zonasCriticas);
        for (int destino : indicesDestino) {
            adicionarSeCritica(zonas, nomeCruzamento(percurso.getVertice(atual + 1)), zonasCriticas);
            adicionarSeCritica(zonas, nomeSegmento(percurso.getAresta(destino)), zonasCriticas);
            atual = destino;
        }
        return ordenarSemRepetir(zonas);
    }

    /* ***************************************************************
    * Metodo: adicionarSeGerenciada
    * Funcao: Adiciona se gerenciada.
    * Parametros: zonas parametro zonas; zona parametro zona
    * Retorno: sem retorno
    *************************************************************** */
    private void adicionarSeGerenciada(List<String> zonas, String zona) {
        if (zonasCriticas.contains(zona)) {
            zonas.add(zona);
        }
    }

    /* ***************************************************************
    * Metodo: existePedidoAnteriorPronto
    * Funcao: Executa a operacao existe pedido anterior pronto.
    * Parametros: carroAtual parametro carroAtual
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    private boolean existePedidoAnteriorPronto(
            int carroAtual, List<String> pedidoAtual) {
        Set<String> zonasAtuais = new LinkedHashSet<>(pedidoAtual);
        for (Map.Entry<Integer, List<String>> entrada : pedidosPendentes.entrySet()) {
            if (entrada.getKey() == carroAtual) {
                return false;
            }
            Set<String> zonasAnteriores = new LinkedHashSet<>(entrada.getValue());
            if (!compartilha(zonasAtuais, zonasAnteriores)) {
                continue;
            }
            if (pedidoPronto(entrada.getKey(), entrada.getValue())) {
                return true;
            }
        }
        return false;
    }

    /* ***************************************************************
    * Metodo: pedidoPronto
    * Funcao: Executa a operacao pedido pronto.
    * Parametros: carro parametro carro; desejadas parametro desejadas
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    private boolean pedidoPronto(int carro, List<String> desejadas) {
        Set<String> atuais = reservasPorCarro.get(carro);
        if (atuais == null) {
            atuais = Collections.emptySet();
        }
        for (String zona : desejadas) {
            if (atuais.contains(zona)) {
                continue;
            }
            Semaphore semaforo = semaforos.get(zona);
            if (semaforo != null && semaforo.availablePermits() == 0) {
                return false;
            }
        }
        return true;
    }

    /* ***************************************************************
    * Metodo: compartilha
    * Funcao: Executa a operacao compartilha.
    * Parametros: a parametro a; b parametro b
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    private boolean compartilha(Set<String> a, Set<String> b) {
        for (String zona : a) {
            if (b.contains(zona)) {
                return true;
            }
        }
        return false;
    }

    /* ***************************************************************
    * Metodo: liberar
    * Funcao: Libera .
    * Parametros: zonas parametro zonas
    * Retorno: sem retorno
    *************************************************************** */
    private void liberar(List<String> zonas) {
        List<String> reversa = new ArrayList<>(zonas);
        Collections.reverse(reversa);
        for (String zona : reversa) {
            Semaphore semaforo = semaforos.get(zona);
            if (semaforo != null) {
                semaforo.release();
            }
        }
    }

    /* ***************************************************************
    * Metodo: adicionarSeCritica
    * Funcao: Adiciona se critica.
    * Parametros: zonas parametro zonas; zona parametro zona; conjunto parametro conjunto
    * Retorno: sem retorno
    *************************************************************** */
    private void adicionarSeCritica(List<String> zonas, String zona, Set<String> conjunto) {
        if (conjunto.contains(zona)) {
            zonas.add(zona);
        }
    }

    /* ***************************************************************
    * Metodo: calcularZonasCriticas
    * Funcao: Calcula zonas criticas.
    * Parametros: percursos parametro percursos
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private Set<String> calcularZonasCriticas(Percurso[] percursos) {
        Map<String, Set<Integer>> donos = calcularDonos(percursos);
        Set<String> resultado = new LinkedHashSet<>();
        for (Map.Entry<String, Set<Integer>> entrada : donos.entrySet()) {
            if (entrada.getValue().size() > 1) {
                resultado.add(entrada.getKey());
            }
        }
        return resultado;
    }

    /* ***************************************************************
    * Metodo: calcularZonasConflitoComCarro8
    * Funcao: Calcula zonas conflito com carro8.
    * Parametros: percursos parametro percursos
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private Set<String> calcularZonasConflitoComCarro8(Percurso[] percursos) {
        Map<String, Set<Integer>> donos = calcularDonos(percursos);
        Set<String> resultado = new LinkedHashSet<>();
        for (Map.Entry<String, Set<Integer>> entrada : donos.entrySet()) {
            Set<Integer> proprietarios = entrada.getValue();
            if (proprietarios.contains(CARRO_8) && proprietarios.size() > 1) {
                resultado.add(entrada.getKey());
            }
        }
        return resultado;
    }

    /* ***************************************************************
    * Metodo: calcularDonos
    * Funcao: Calcula donos.
    * Parametros: percursos parametro percursos
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private Map<String, Set<Integer>> calcularDonos(Percurso[] percursos) {
        Map<String, Set<Integer>> donos = new LinkedHashMap<>();
        for (int i = 0; i < percursos.length; i++) {
            Percurso percurso = percursos[i];
            for (int indice = 0; indice < percurso.getQuantidadeTrechos(); indice++) {
                registrarDono(donos, nomeSegmento(percurso.getAresta(indice)), i + 1);
                registrarDono(donos, nomeCruzamento(percurso.getVertice(indice + 1)), i + 1);
            }
        }
        return donos;
    }

    /* ***************************************************************
    * Metodo: calcularSegmentosInseguros
    * Funcao: Calcula segmentos inseguros.
    * Parametros: percursos parametro percursos
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private Map<Integer, Set<String>> calcularSegmentosInseguros(Percurso[] percursos) {
        Map<String, Map<Integer, String>> direcoes = new HashMap<>();
        for (int i = 0; i < percursos.length; i++) {
            Percurso percurso = percursos[i];
            for (int indice = 0; indice < percurso.getQuantidadeTrechos(); indice++) {
                Aresta aresta = percurso.getAresta(indice);
                Vertice origem = percurso.getVertice(indice);
                Vertice destino = percurso.getVertice(indice + 1);
                direcoes.computeIfAbsent(nomeSegmento(aresta), chave -> new LinkedHashMap<>())
                    .put(i + 1, origem.chave() + ">" + destino.chave());
            }
        }

        Map<Integer, Set<String>> resultado = new LinkedHashMap<>();
        for (Map.Entry<String, Map<Integer, String>> entrada : direcoes.entrySet()) {
            if (new HashSet<>(entrada.getValue().values()).size() <= 1) {
                continue;
            }
            for (Integer carro : entrada.getValue().keySet()) {
                resultado.computeIfAbsent(carro, chave -> new LinkedHashSet<>())
                    .add(entrada.getKey());
            }
        }
        return resultado;
    }

    /* ***************************************************************
    * Metodo: criarPercursos
    * Funcao: Cria percursos.
    * Parametros: grid parametro grid; quantidade parametro quantidade
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private Percurso[] criarPercursos(Grid grid, int quantidade) {
        Percurso[] percursos = new Percurso[quantidade];
        for (int i = 0; i < quantidade; i++) {
            percursos[i] = new Percurso(
                grid,
                Constantes.NOMES_PERCURSOS[i],
                Constantes.SENTIDOS[i],
                Constantes.TRECHOS_DOS_CARROS[i]
            );
        }
        return percursos;
    }

    /* ***************************************************************
    * Metodo: registrarDono
    * Funcao: Registra dono.
    * Parametros: donos parametro donos; zona parametro zona; carro parametro carro
    * Retorno: sem retorno
    *************************************************************** */
    private void registrarDono(Map<String, Set<Integer>> donos, String zona, int carro) {
        donos.computeIfAbsent(zona, chave -> new LinkedHashSet<>()).add(carro);
    }

    /* ***************************************************************
    * Metodo: nomeSegmento
    * Funcao: Executa a operacao nome segmento.
    * Parametros: trecho parametro trecho
    * Retorno: texto resultante
    *************************************************************** */
    private String nomeSegmento(Aresta trecho) { 
        return PREFIXO_SEGMENTO + trecho.getNome(); 
    }

    /* ***************************************************************
    * Metodo: nomeCruzamento
    * Funcao: Executa a operacao nome cruzamento.
    * Parametros: vertice parametro vertice
    * Retorno: texto resultante
    *************************************************************** */
    private String nomeCruzamento(Vertice vertice) { 
        return PREFIXO_CRUZAMENTO + vertice.chave(); 
    }

    /* ***************************************************************
    * Metodo: ordenarSemRepetir
    * Funcao: Executa a operacao ordenar sem repetir.
    * Parametros: zonas parametro zonas
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private List<String> ordenarSemRepetir(List<String> zonas) {
        List<String> resultado = new ArrayList<>(new LinkedHashSet<>(zonas));
        Collections.sort(resultado);
        return resultado;
    }

    /* ***************************************************************
    * Metodo: diferenca
    * Funcao: Executa a operacao diferenca.
    * Parametros: origem parametro origem; remover parametro remover
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private List<String> diferenca(List<String> origem, Set<String> remover) {
        List<String> resultado = new ArrayList<>();
        for (String item : origem) {
            if (!remover.contains(item)) {
                resultado.add(item);
            }
        }
        return resultado;
    }
}
