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

    private static final int CARROS_BASE = 7;
    private static final int CARRO_8 = 8;
    private static final String PREFIXO_SEGMENTO = "SEG-";
    private static final String PREFIXO_CRUZAMENTO = "CRUZ-";
    private static final String PORTARIA_CORREDORES = "PORTARIA-CORREDORES";

    private final Set<String> zonasCriticas;
    private final Set<String> zonasAtivasBase;
    private final Set<String> zonasConflitoCarro8;
    private final Set<String> segmentosOpostosTodos;
    private final Map<Integer, Set<String>> segmentosInsegurosBase;

    private final Map<String, Semaphore> semaforos = new LinkedHashMap<>();
    private final Semaphore portariaReservas = new Semaphore(1, true);
    private final Map<Integer, LinkedHashSet<String>> reservasPorCarro = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, List<String>> pedidosPendentes = new LinkedHashMap<>();

    private Set<String> janelaCarro8Aguardando = Collections.emptySet();
    private Set<String> janelaCarro8Ativa = Collections.emptySet();

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
        Percurso[] base = criarPercursos(grid, CARROS_BASE);

        this.zonasCriticas = Collections.unmodifiableSet(calcularZonasCriticas(todos));
        this.zonasAtivasBase = Collections.unmodifiableSet(calcularZonasCriticas(base));
        this.zonasConflitoCarro8 = Collections.unmodifiableSet(
            calcularZonasConflitoComCarro8(todos)
        );
        this.segmentosInsegurosBase = calcularSegmentosInseguros(base);

        Map<Integer, Set<String>> opostosTodos = calcularSegmentosInseguros(todos);
        Set<String> uniao = new LinkedHashSet<>();
        for (Set<String> segmentos : opostosTodos.values()) {
            uniao.addAll(segmentos);
        }
        this.segmentosOpostosTodos = Collections.unmodifiableSet(uniao);

        Set<String> gerenciadas = new LinkedHashSet<>(zonasAtivasBase);
        gerenciadas.addAll(zonasConflitoCarro8);
        for (String zona : gerenciadas) {
            semaforos.put(zona, new Semaphore(1, true));
        }
        semaforos.put(PORTARIA_CORREDORES, new Semaphore(1, true));
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
        // Usado apenas como ponto de instrumentacao pelos testes.
    }

    /* ***************************************************************
    * Metodo: registrarPosicaoInicial
    * Funcao: Registra posicao inicial.
    * Parametros: carro parametro carro
    * Retorno: sem retorno
    *************************************************************** */
    public void registrarPosicaoInicial(Carro carro) throws InterruptedException {
        if (carro.getNumero() == CARRO_8) {
            return;
        }
        atualizarReservaAteConseguir(
            carro.getNumero(), zonasDaParadaBase(carro.getTrechoAtual())
        );
    }

    /* ***************************************************************
    * Metodo: reservarJanela
    * Funcao: Reserva janela.
    * Parametros: carro parametro carro; indicesDestino parametro indicesDestino
    * Retorno: sem retorno
    *************************************************************** */
    public void reservarJanela(Carro carro, List<Integer> indicesDestino)
            throws InterruptedException {
        if (carro.getNumero() == CARRO_8) {
            reservarJanelaDoCarro8(carro, indicesDestino);
            return;
        }
        atualizarReservaAteConseguir(
            carro.getNumero(), zonasDaJanelaBase(carro, indicesDestino)
        );
    }

    /* ***************************************************************
    * Metodo: reservarJanelaDeCorredor
    * Funcao: Reserva janela de corredor.
    * Parametros: carro parametro carro; indicesDestino parametro indicesDestino
    * Retorno: sem retorno
    *************************************************************** */
    public void reservarJanelaDeCorredor(Carro carro, List<Integer> indicesDestino)
            throws InterruptedException {
        if (carro.getNumero() == CARRO_8) {
            reservarJanelaDoCarro8(carro, indicesDestino);
            return;
        }
        List<String> desejadas = zonasDaJanelaBase(carro, indicesDestino);
        desejadas.add(PORTARIA_CORREDORES);
        atualizarReservaAteConseguir(carro.getNumero(), desejadas);
    }

    /* ***************************************************************
    * Metodo: tentarReservarJanela
    * Funcao: Executa a operacao tentar reservar janela.
    * Parametros: carro parametro carro; indicesDestino parametro indicesDestino
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean tentarReservarJanela(Carro carro, List<Integer> indicesDestino)
            throws InterruptedException {
        if (carro.getNumero() == CARRO_8) {
            return false;
        }
        return tentarAtualizarReserva(
            carro.getNumero(), zonasDaJanelaBase(carro, indicesDestino)
        );
    }

    /* ***************************************************************
    * Metodo: finalizarJanela
    * Funcao: Finaliza janela.
    * Parametros: carro parametro carro; manterPortaria parametro manterPortaria
    * Retorno: sem retorno
    *************************************************************** */
    public void finalizarJanela(Carro carro, boolean manterPortaria) {
        if (carro.getNumero() == CARRO_8) {
            liberarJanelaDoCarro8();
            return;
        }
        List<String> desejadas = new ArrayList<>(zonasDaParadaBase(carro.getTrechoAtual()));
        if (manterPortaria) {
            desejadas.add(PORTARIA_CORREDORES);
        }
        atualizarSomenteLiberando(carro.getNumero(), desejadas);
    }

    /* ***************************************************************
    * Metodo: reservarJanelaDoCarro8
    * Funcao: Reserva janela do carro8.
    * Parametros: carro parametro carro; indicesDestino parametro indicesDestino
    * Retorno: sem retorno
    *************************************************************** */
    private void reservarJanelaDoCarro8(Carro carro, List<Integer> indicesDestino)
            throws InterruptedException {
        List<String> zonasFisicas = zonasDaJanelaCompleta(carro, indicesDestino);
        List<String> desejadas = new ArrayList<>();
        for (String zona : zonasFisicas) {
            if (zonasConflitoCarro8.contains(zona)) {
                desejadas.add(zona);
            }
        }
        desejadas = ordenarSemRepetir(desejadas);

        portariaReservas.acquire();
        try {
            janelaCarro8Aguardando = new LinkedHashSet<>(desejadas);
        } finally {
            portariaReservas.release();
        }

        List<String> adquiridas = new ArrayList<>();
        try {
            long espera = 1L;
            long inicioCiclo = System.nanoTime();
            while (true) {
                portariaReservas.acquire();
                try {
                    adquiridas.clear();
                    boolean conseguiu = true;
                    for (String zona : desejadas) {
                        Semaphore semaforo = semaforos.get(zona);
                        if (semaforo != null && !semaforo.tryAcquire()) {
                            conseguiu = false;
                            break;
                        }
                        if (semaforo != null) {
                            adquiridas.add(zona);
                        }
                    }
                    if (conseguiu) {
                        janelaCarro8Ativa = new LinkedHashSet<>(desejadas);
                        janelaCarro8Aguardando = Collections.emptySet();
                        reservasConcedidas++;
                        return;
                    }
                    liberar(adquiridas);
                    adquiridas.clear();
                    tentativasNegadas++;
                } finally {
                    portariaReservas.release();
                }

                long aguardandoMs = (System.nanoTime() - inicioCiclo) / 1_000_000L;
                if (aguardandoMs >= 120L) {
                    portariaReservas.acquire();
                    try {
                        janelaCarro8Aguardando = Collections.emptySet();
                    } finally {
                        portariaReservas.release();
                    }
                    Thread.sleep(25L);
                    portariaReservas.acquire();
                    try {
                        janelaCarro8Aguardando = new LinkedHashSet<>(desejadas);
                    } finally {
                        portariaReservas.release();
                    }
                    inicioCiclo = System.nanoTime();
                    espera = 1L;
                } else {
                    Thread.sleep(espera);
                    if (espera < 10L) {
                        espera++;
                    }
                }
            }
        } catch (InterruptedException e) {
            portariaReservas.acquireUninterruptibly();
            try {
                if (!adquiridas.isEmpty()) {
                    liberar(adquiridas);
                }
                janelaCarro8Aguardando = Collections.emptySet();
            } finally {
                portariaReservas.release();
            }
            throw e;
        }
    }

    /* ***************************************************************
    * Metodo: liberarJanelaDoCarro8
    * Funcao: Libera janela do carro8.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void liberarJanelaDoCarro8() {
        portariaReservas.acquireUninterruptibly();
        try {
            if (!janelaCarro8Ativa.isEmpty()) {
                List<String> zonas = new ArrayList<>(janelaCarro8Ativa);
                Collections.sort(zonas);
                liberar(zonas);
            }
            janelaCarro8Ativa = Collections.emptySet();
            janelaCarro8Aguardando = Collections.emptySet();
        } finally {
            portariaReservas.release();
        }
    }

    /* ***************************************************************
    * Metodo: liberarCarro
    * Funcao: Libera carro.
    * Parametros: numeroCarro parametro numeroCarro
    * Retorno: sem retorno
    *************************************************************** */
    public void liberarCarro(int numeroCarro) {
        portariaReservas.acquireUninterruptibly();
        try {
            LinkedHashSet<String> atuais = reservasPorCarro.remove(numeroCarro);
            pedidosPendentes.remove(numeroCarro);
            if (atuais != null) {
                liberar(new ArrayList<>(atuais));
            }
        } finally {
            portariaReservas.release();
        }
        if (numeroCarro == CARRO_8) {
            liberarJanelaDoCarro8();
        }
    }

    /* ***************************************************************
    * Metodo: paradaSegura
    * Funcao: Executa a operacao parada segura.
    * Parametros: carro parametro carro; indiceTrecho parametro indiceTrecho
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean paradaSegura(Carro carro, int indiceTrecho) {
        if (carro.getNumero() == CARRO_8) {
            return zonasDoMeioTrecho(carro.getPercurso().getAresta(indiceTrecho)).isEmpty();
        }
        return !estaEmCorredorDeSentidoOposto(carro, indiceTrecho);
    }

    /* ***************************************************************
    * Metodo: estaEmCorredorDeSentidoOposto
    * Funcao: Executa a operacao esta em corredor de sentido oposto.
    * Parametros: carro parametro carro; indiceTrecho parametro indiceTrecho
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    public boolean estaEmCorredorDeSentidoOposto(Carro carro, int indiceTrecho) {
        if (carro.getNumero() == CARRO_8) {
            return false;
        }
        String zona = nomeSegmento(carro.getPercurso().getAresta(indiceTrecho));
        Set<String> inseguros = segmentosInsegurosBase.get(carro.getNumero());
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
                    || !janelaCarro8Aguardando.isEmpty() || !janelaCarro8Ativa.isEmpty()) {
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
                + " c8Esperando=" + janelaCarro8Aguardando
                + " c8Ativa=" + janelaCarro8Ativa;
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
            List<String> ordenadas = ordenarSemRepetir(desejadas);
            LinkedHashSet<String> atuais = reservasPorCarro.get(carro);
            if (atuais == null) {
                atuais = new LinkedHashSet<>();
            }

            Set<String> restritas = !janelaCarro8Ativa.isEmpty()
                ? janelaCarro8Ativa : janelaCarro8Aguardando;
            boolean dentroDaJanela = compartilha(atuais, restritas);
            boolean querEntrarNaJanela = false;
            for (String zona : ordenadas) {
                if (restritas.contains(zona) && !atuais.contains(zona)) {
                    querEntrarNaJanela = true;
                    break;
                }
            }

            pedidosPendentes.put(carro, ordenadas);
            if (!restritas.isEmpty() && !dentroDaJanela && querEntrarNaJanela) {
                tentativasNegadas++;
                return false;
            }

            if (existePedidoAnteriorPronto(carro)) {
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
                    adquiridas.clear();
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
        } finally {
            portariaReservas.release();
        }
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
    * Metodo: zonasDaParadaBase
    * Funcao: Executa a operacao zonas da parada base.
    * Parametros: trecho parametro trecho
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private List<String> zonasDaParadaBase(Aresta trecho) {
        List<String> zonas = new ArrayList<>();
        String zona = nomeSegmento(trecho);
        if (zonasAtivasBase.contains(zona) || zonasConflitoCarro8.contains(zona)) {
            zonas.add(zona);
        }
        return zonas;
    }

    /* ***************************************************************
    * Metodo: zonasDaJanelaBase
    * Funcao: Executa a operacao zonas da janela base.
    * Parametros: carro parametro carro; indicesDestino parametro indicesDestino
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private List<String> zonasDaJanelaBase(Carro carro, List<Integer> indicesDestino) {
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
        if (zonasAtivasBase.contains(zona) || zonasConflitoCarro8.contains(zona)) {
            zonas.add(zona);
        }
    }

    /* ***************************************************************
    * Metodo: existePedidoAnteriorPronto
    * Funcao: Executa a operacao existe pedido anterior pronto.
    * Parametros: carroAtual parametro carroAtual
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
    private boolean existePedidoAnteriorPronto(int carroAtual) {
        for (Map.Entry<Integer, List<String>> entrada : pedidosPendentes.entrySet()) {
            if (entrada.getKey() == carroAtual) {
                return false;
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
        Set<String> restritas = !janelaCarro8Ativa.isEmpty()
            ? janelaCarro8Ativa : janelaCarro8Aguardando;
        if (!restritas.isEmpty() && !compartilha(atuais, restritas)) {
            for (String zona : desejadas) {
                if (restritas.contains(zona) && !atuais.contains(zona)) {
                    return false;
                }
            }
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
