import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Isomorfismo em Arvores com Codificacao Canonica.
 *
 * Determina se dois grafos nao direcionados representam arvores isomorfas,
 * ou seja, estruturalmente identicas independentemente da rotulacao dos vertices.
 *
 * O algoritmo segue tres etapas:
 *
 *   1. Validar se o grafo e uma arvore (conexo, V-1 arestas, sem ciclos).
 *   2. Encontrar o(s) centro(s) por remocao iterativa de folhas (peeling).
 *      Uma arvore tem 1 ou 2 centros.
 *   3. Enraizar a arvore em cada centro e calcular a codificacao canonica
 *      via DFS. Os codigos dos filhos sao ordenados lexicograficamente para
 *      que subarvores isomorfas sempre gerem a mesma string.
 *
 * Para o caso de dois centros, ambos sao testados contra o centro da outra
 * arvore — se qualquer par produzir codigos iguais, as arvores sao isomorfas.
 * Essa estrategia e identica a da implementacao de referencia (Fiset).
 *
 * Padrao de codificacao:
 *   - Folha         -> "()"
 *   - No c/filhos   -> "(" + codigos_filhos_ordenados + ")"
 *
 * Exemplo: raiz com dois filhos folha -> "(()())"
 *
 * Complexidade: O(V log V) — dominado pela ordenacao dos filhos em cada no.
 *
 * Compatibilidade: usa {@link Graph} do padrao algs4 como estrutura principal.
 * Uma estrutura auxiliar interna ({@link TreeNode}) e criada apenas durante
 * a etapa de codificacao, conforme permitido pelo enunciado.
 */
public class TreeIsomorphism {

    // -------------------------------------------------------------------------
    // Estrutura auxiliar interna: arvore enraizada para a codificacao
    // -------------------------------------------------------------------------

    /**
     * No de uma arvore enraizada, usado internamente durante a codificacao.
     * Nao substitui o Graph como representacao principal — serve apenas como
     * apoio ao algoritmo recursivo de encode().
     *
     * Equivalente ao TreeNode da implementacao de referencia (Fiset).
     */
    private static class TreeNode {
        private final int          id;
        private final TreeNode     parent;
        private final List<TreeNode> children;

        public TreeNode(int id, TreeNode parent) {
            this.id       = id;
            this.parent   = parent;
            this.children = new ArrayList<>();
        }

        public int id()                  { return id; }
        public TreeNode parent()         { return parent; }
        public List<TreeNode> children() { return children; }

        @Override
        public String toString() { return String.valueOf(id); }
    }

    // -------------------------------------------------------------------------
    // Estado da instancia
    // -------------------------------------------------------------------------

    private final Graph graph;

    // Cache da validacao — preenchido de forma lazy
    private Boolean validTree     = null;
    private String  validationMsg = null;

    // -------------------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------------------

    /**
     * Inicializa a analise sobre um grafo lido no formato algs4.
     *
     * @param graph grafo nao direcionado
     * @throws IllegalArgumentException se graph for nulo
     */
    public TreeIsomorphism(Graph graph) {
        if (graph == null)
            throw new IllegalArgumentException("graph nao pode ser nulo");
        this.graph = graph;
    }

    public Graph getGraph() { return graph; }

    // -------------------------------------------------------------------------
    // isTree / getValidationMessage
    // -------------------------------------------------------------------------

    /**
     * Retorna {@code true} se o grafo representa uma arvore valida.
     *
     * Uma arvore nao direcionada satisfaz simultaneamente:
     *   (a) exatamente E = V - 1 arestas;
     *   (b) grafo conexo (todos os vertices alcancaveis a partir do vertice 0).
     *
     * As duas condicoes juntas sao necessarias e suficientes.
     *
     * @return {@code true} se for uma arvore valida
     */
    public boolean isTree() {
        if (validTree == null) computeValidation();
        return validTree;
    }

    /**
     * Retorna uma mensagem descritiva sobre o resultado da validacao.
     *
     * @return descricao legivel do motivo de validade ou invalidade
     */
    public String getValidationMessage() {
        if (validationMsg == null) computeValidation();
        return validationMsg;
    }

    /**
     * Executa a validacao e armazena os resultados em cache.
     * Chamado de forma lazy pela primeira chamada a isTree() ou
     * a getValidationMessage().
     */
    private void computeValidation() {
        int V = graph.V();
        int E = graph.E();

        // Caso trivial: arvore vazia
        if (V == 0) {
            validTree     = true;
            validationMsg = "Arvore valida: grafo vazio (0 vertices).";
            return;
        }

        // Condicao (a): numero de arestas
        if (E != V - 1) {
            validTree     = false;
            validationMsg = String.format(
                "Entrada invalida: uma arvore com %d vertices deve ter %d aresta(s),"
                + " mas foram encontradas %d.", V, V - 1, E);
            return;
        }

        // Condicao (b): conectividade via BFS
        boolean[] visited = new boolean[V];
        bfs(0, visited);
        for (int v = 0; v < V; v++) {
            if (!visited[v]) {
                validTree     = false;
                validationMsg = String.format(
                    "Entrada invalida: grafo desconexo — vertice %d nao e"
                    + " alcancavel a partir do vertice 0.", v);
                return;
            }
        }

        validTree     = true;
        validationMsg = String.format(
            "Arvore valida: %d vertices, %d arestas, conexa e aciclica.", V, E);
    }

    /**
     * BFS simples a partir de {@code source}, marcando vertices visitados.
     * Usado para verificar conectividade em computeValidation().
     */
    private void bfs(int source, boolean[] visited) {
        Queue<Integer> q = new LinkedList<>();
        q.add(source);
        visited[source] = true;
        while (!q.isEmpty()) {
            int v = q.poll();
            for (int w : graph.adj(v)) {
                if (!visited[w]) {
                    visited[w] = true;
                    q.add(w);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // getCenters
    // -------------------------------------------------------------------------

    /**
     * Encontra o(s) centro(s) da arvore por remocao iterativa de folhas.
     *
     * Algoritmo (identico ao findTreeCenters() da referencia — Fiset):
     *   1. Inicializa degree[v] = grau do vertice v no Graph.
     *   2. Coloca em 'leaves' todo vertice com grau <= 1; zera seu degree.
     *   3. Para cada folha, decrementa o degree de seus vizinhos.
     *      Quando degree[vizinho] atinge 1, ele entra na proxima camada.
     *      Zera o degree da folha apos processar (evita decrementos duplos).
     *   4. Repete ate todos os V vertices terem sido processados.
     *   5. A ultima camada restante contem exatamente 1 ou 2 centros.
     *
     * @return {@code int[]} com 1 ou 2 indices de vertices centrais
     * @throws IllegalStateException se o grafo nao for uma arvore valida
     */
    public int[] getCenters() {
        if (!isTree())
            throw new IllegalStateException(
                "Nao e possivel calcular centros: " + getValidationMessage());

        int V = graph.V();

        // Casos triviais
        if (V == 1) return new int[]{ 0 };
        if (V == 2) return new int[]{ 0, 1 };

        // Copia dos graus para nao alterar o Graph original
        int[] degree = new int[V];
        List<Integer> leaves = new ArrayList<>();

        // Primeira camada de folhas — igual a referencia
        for (int v = 0; v < V; v++) {
            degree[v] = graph.degree(v);
            if (degree[v] <= 1) {
                leaves.add(v);
                degree[v] = 0;
            }
        }

        int processedLeaves = leaves.size();

        while (processedLeaves < V) {
            List<Integer> newLeaves = new ArrayList<>();
            for (int node : leaves) {
                for (int neighbor : graph.adj(node)) {
                    if (--degree[neighbor] == 1) {
                        newLeaves.add(neighbor);
                    }
                }
                degree[node] = 0;   // marca como processado — igual a referencia
            }
            processedLeaves += newLeaves.size();
            leaves = newLeaves;
        }

        // Converte lista para int[]
        int[] centers = new int[leaves.size()];
        for (int i = 0; i < leaves.size(); i++) centers[i] = leaves.get(i);
        return centers;
    }

    // -------------------------------------------------------------------------
    // getCanonicalEncoding
    // -------------------------------------------------------------------------

    /**
     * Gera a codificacao canonica da arvore.
     *
     * Estrategia identica a da referencia (Fiset):
     *   - 1 centro: enraiza nele e codifica.
     *   - 2 centros: gera o codigo para cada centro como raiz e retorna o
     *     menor lexicograficamente, garantindo resultado deterministico mesmo
     *     quando a arvore tem dois centros.
     *
     * Internamente constroi uma {@link TreeNode} auxiliar via rootTree() e
     * calcula o codigo com encode(). Essa estrutura auxiliar nao substitui
     * o Graph como representacao principal da entrada.
     *
     * @return string com a codificacao canonica
     * @throws IllegalStateException se o grafo nao for uma arvore valida
     */
    public String getCanonicalEncoding() {
        if (!isTree())
            throw new IllegalStateException(
                "Nao e possivel codificar: " + getValidationMessage());

        int V = graph.V();
        if (V == 1) return "()";

        int[] centers = getCenters();

        if (centers.length == 1) {
            return encode(rootTree(centers[0]));
        }

        // Dois centros: menor codigo entre os dois — garante determinismo
        String code0 = encode(rootTree(centers[0]));
        String code1 = encode(rootTree(centers[1]));
        return code0.compareTo(code1) <= 0 ? code0 : code1;
    }

    // -------------------------------------------------------------------------
    // Metodos auxiliares privados: rootTree, buildTree, encode
    // -------------------------------------------------------------------------

    /**
     * Constroi uma arvore enraizada ({@link TreeNode}) a partir do Graph algs4,
     * enraizando em {@code rootId}.
     *
     * Equivalente ao rootTree() da referencia.
     *
     * @param rootId vertice raiz
     * @return no raiz da arvore enraizada auxiliar
     */
    private TreeNode rootTree(int rootId) {
        TreeNode root = new TreeNode(rootId, /* parent= */ null);
        buildTree(root);
        return root;
    }

    /**
     * Constroi recursivamente a arvore enraizada via DFS, ignorando a aresta
     * de volta ao pai.
     *
     * Equivalente ao buildTree() da referencia — a verificacao de pai e feita
     * pelo id do no pai, nao por um booleano visited[], o que e correto para
     * arvores (sem ciclos).
     *
     * @param node no atual sendo expandido
     */
    private void buildTree(TreeNode node) {
        for (int neighbor : graph.adj(node.id())) {
            // Ignora aresta de retorno ao pai (arvore: sem ciclos)
            if (node.parent() != null && neighbor == node.parent().id()) continue;
            TreeNode child = new TreeNode(neighbor, node);
            node.children().add(child);
            buildTree(child);
        }
    }

    /**
     * Calcula a codificacao canonica da subarvore enraizada em {@code node}.
     *
     * Logica identica ao encode() da referencia (Fiset):
     *   1. Calcula recursivamente o codigo de cada filho.
     *   2. Ordena os codigos dos filhos lexicograficamente.
     *   3. Concatena e envolve em parenteses: "(" + filhos_ordenados + ")"
     *
     * A ordenacao lexicografica e o ponto-chave que garante que arvores
     * isomorfas sempre gerem o mesmo codigo, independente da ordem de
     * leitura das arestas no arquivo de entrada.
     *
     * @param node raiz da subarvore a codificar
     * @return string com a codificacao canonica da subarvore
     */
    public String encode(TreeNode node) {
        if (node == null) return "";

        List<String> labels = new ArrayList<>();
        for (TreeNode child : node.children()) {
            labels.add(encode(child));
        }
        Collections.sort(labels);

        StringBuilder sb = new StringBuilder("(");
        for (String label : labels) sb.append(label);
        return sb.append(")").toString();
    }

    // -------------------------------------------------------------------------
    // isIsomorphicTo
    // -------------------------------------------------------------------------

    /**
     * Retorna {@code true} se esta arvore e isomorfa a {@code other}.
     *
     * Implementa a mesma estrategia da referencia para dois centros:
     * enraiza ESTA arvore no seu primeiro centro e testa TODOS os centros
     * de {@code other} — se qualquer par de codigos for igual, as arvores
     * sao isomorfas. Isso cobre corretamente o caso em que uma arvore tem
     * 1 centro e a outra tem 2.
     *
     * @param other outra instancia de TreeIsomorphism
     * @return {@code true} se as arvores forem estruturalmente identicas
     * @throws IllegalStateException se qualquer entrada nao for arvore valida
     */
    public boolean isIsomorphicTo(TreeIsomorphism other) {
        if (!this.isTree())
            throw new IllegalStateException(
                "Esta entrada nao e uma arvore valida: " + this.getValidationMessage());
        if (!other.isTree())
            throw new IllegalStateException(
                "A outra entrada nao e uma arvore valida: " + other.getValidationMessage());

        int[] myCenters    = this.getCenters();
        int[] otherCenters = other.getCenters();

        // Codifica esta arvore a partir do seu primeiro centro
        String myCode = this.encode(this.rootTree(myCenters[0]));

        // Testa todos os centros da outra (1 ou 2) — igual a referencia
        for (int center : otherCenters) {
            if (myCode.equals(other.encode(other.rootTree(center)))) return true;
        }
        return false;
    }
}